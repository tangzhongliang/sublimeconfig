package snscommon

import (
	"encoding/json"
	"io/ioutil"
	"sns/common"
	"sns/common/snsstruct"
	"sns/controllers/snsep"
	"sns/controllers/snsinterface/snseper"
	// "sns/controllers/snsplugin"
	"bytes"
	"net/http"
	"sns/common/snsglobal"
	"sns/models"
	"sns/util/snserror"
	"sns/util/snslog"
	"sync"
)

var (
	ConversationSateMap     = make(map[models.SnsEpAccount]models.SnsPlugin)
	ConversationSateMapLock = &sync.RWMutex{}
)

func GetConversationPlugin(account models.SnsEpAccount) (plugin models.SnsPlugin, ok bool) {
	ConversationSateMapLock.RLock()
	defer ConversationSateMapLock.RUnlock()
	plugin, ok = ConversationSateMap[models.SnsEpAccount{AccountId: account.AccountId, AccountType: account.AccountType}]
	return
}

func SetConversationPlugin(account models.SnsEpAccount, plugin models.SnsPlugin) (ok bool) {
	ConversationSateMapLock.Lock()
	defer ConversationSateMapLock.Unlock()
	ConversationSateMap[models.SnsEpAccount{AccountId: account.AccountId, AccountType: account.AccountType}] = plugin
	return
}

func SendMessageToPluginByPost(url string, msg snsstruct.EpToPluginMessage) (err error) {
	var req *http.Request
	var res *http.Response
	var msgBytes []byte
	msgBytes, err = json.Marshal(msg)
	snserror.LogAndPanic(err)
	req, err = http.NewRequest("POST", url, bytes.NewReader(msgBytes))
	req.Header.Add("Content-Type", "application/json")
	res, err = http.DefaultClient.Do(req)
	snslog.D("SendMessageToPluginByPost", url, msg, res.Status)
	if err != nil {
		// snslog.E(err)
	} else {
		msgBytes, err = ioutil.ReadAll(res.Body)
		// snslog.If("SendMessageToPluginByPost result:%s", string(msgBytes))
	}
	return
}

func SendMessageToEp(accounts []models.SnsEpAccount, msg snsstruct.PluginToEpMessage) (sendMessageId string) {
	snslog.If("SendMessageToEp/ send to %d;%+v", len(accounts), accounts)
	res := ExecUntilSuccess(func() (key interface{}, ok bool) {
		key = common.CreateRandomString(255)
		err := models.Insert(&models.PluginSendMessageState{SendMessageId: key.(string)})
		ok = err == nil
		return
	})
	sendMessageId = res.(string)
	for _, account := range accounts {
		sender := snsglobal.SBeanFactory.New("ep_" + account.AccountType).(snseper.SnsEPMessageSender)
		sender.SendAttachmentByUser(account.ForeverToken, account.AccountId, msg.Message)
	}
	return
}

func DispatchMessageToEP(msg snsstruct.PluginToEpMessage, token string) (ret snsstruct.ServiceMessageResponse) {
	// -------------------------check token
	var plugin models.SnsPlugin
	// err := models.QueryByKey(&plugin, &models.SnsPlugin{PluginId: msg.PluginId})
	err := models.Query(&plugin, &models.SnsPlugin{PluginToken: token})
	if err != nil || len(token) == 0 {
		// ret = CreateServiceMessageResponse(1001, "plugin id is invalid")
		ret = CreateServiceMessageResponse(1002, "token is invalid")
		return
	}
	//	-------------------------reset Plugin id
	msg.PluginId = plugin.PluginId

	//	---------------------------check user null
	if !msg.Message.IsToAll && len(msg.TargetEmails.TargetUserEmail) == 0 && len(msg.TargetUsers) == 0 {
		ret = CreateServiceMessageResponse(1003, "no user to send")
		return
	}

	// -----------------------------send msg to ep users
	accounts := msg.TargetUsers
	if !msg.Message.IsToAll {
		emailaccounts := snsep.GetSnsEpByEmail(msg.TargetEmails.TargetUserEmail, msg.TargetEmails.Platforms)
		//	------------------------- Check SNSEpAuth For Plugin
		// CheckSNSEpAuthForPlugin
		snslog.I("DispatchMessageToEP/ email accounts", emailaccounts)
		accounts = append(accounts, emailaccounts...)
		snslog.I("DispatchMessageToEP/ IsToAll true all users", accounts)
	} else {
		accounts = snsep.GetSnsEpByPluginId(msg.PluginId)
		snslog.I("DispatchMessageToEP/ IsToAll false all users", accounts)
	}
	SendMessageToEp(accounts, msg)
	if len(accounts) == 0 {
		return CreateServiceMessageResponse(1000, "accounts null")
	}
	return CreateServiceMessageResponse(1000, "")
}

func DispatchMessageToPlugin(msg snsstruct.EpToPluginMessage) {
	var plugin models.SnsPlugin
	err := models.Query(&plugin, &models.SnsPlugin{PluginId: msg.PluginId})
	snserror.LogAndPanic(err)
	SendMessageToPluginByPost(plugin.PluginWebhookUrl, msg)
}

func CreateServiceMessageResponse(code int, errMsg string) snsstruct.ServiceMessageResponse {

	if code == 1000 {
		return snsstruct.ServiceMessageResponse{
			Ok:        true,
			ErrDefine: snsstruct.ErrDefine{code, errMsg},
		}
	} else {
		return snsstruct.ServiceMessageResponse{
			Ok:        false,
			ErrDefine: snsstruct.ErrDefine{code, errMsg},
		}
	}
}
