package snsplugin

import (
	"encoding/json"
	"errors"
	"sns/common"
	"sns/common/snsglobal"
	"sns/common/snsstruct"
	// "sns/controllers/snscommon"
	"sns/models"
	"sns/util/snserror"
	"sns/util/snslog"
	"strings"
)

func RegisterPlugin(token, pluginConfig string) {

}

func FindPluginByToken(token string) (plugin models.SnsPlugin, err error) {
	var accounts []models.SnsPlugin
	err = models.Query(&accounts, &models.SnsPlugin{PluginToken: token})
	if snserror.LogAndPanic(err) {
		return
	}
	plugin = accounts[0]
	return
}

func ParseFromPluginMessage(str string) (msg snsstruct.PluginToEpMessage, err error) {
	err = json.Unmarshal([]byte(str), &msg)
	snserror.LogAndPanic(err)

	// -----------------------------save plugin id into callback to unique and find plugin to response
	// -----------------------------attach must be ptr
	for _, attach := range msg.Message.Attachments {
		snslog.D("ParseFromPluginMessage", attach)
		attach.CallbackId = msg.PluginId + "_" + attach.CallbackId
	}

	//	-----------------------------unpack Userid:eptype_userid
	msg.TargetUsers = make([]models.SnsEpAccount, len(msg.TargetUserIds))
	for index, userId := range msg.TargetUserIds {
		splits := strings.SplitN(userId, "_", 2)
		if len(splits) != 2 {
			err = errors.New("ParseToPluginMessage/ userid is incorrect")
			if snserror.LogAndPanic(err) {
				return
			}
		}
		msg.TargetUsers[index].AccountType = splits[0]
		msg.TargetUsers[index].AccountId = splits[1]
	}
	return
}

func ParseToPluginMessage(input snsstruct.EpToPluginMessage) (msg snsstruct.EpToPluginMessage, err error) {
	// err = json.Unmarshal([]byte(str), &msg)
	// snserror.LogAndPanic(err)

	// -----------------------------recover plugin id and callbackid
	msg = input
	value := msg.Message.SnsEpResponse.CallbackId
	splits := strings.SplitN(value, "_", 2)
	if len(splits) != 2 {
		err = errors.New("ParseToPluginMessage/ callbackid don't have plugin id")
		// if snserror.LogAndPanic(err) {
		return
		// }
	}
	msg.PluginId = splits[0]
	msg.Message.SnsEpResponse.CallbackId = splits[1]

	//	----------------------------pack user to userid
	msg.UserId = msg.User.AccountType + "_" + msg.User.AccountId
	return
}

// create a new token for plugin
// save new token into db
// return response content
func RequestPluginToken(plugin models.SnsPlugin) (res snsstruct.PluginTokenResponse) {
	var existPlugin models.SnsPlugin
	for true {
		err := models.Query(&existPlugin, &models.SnsPlugin{PluginId: plugin.PluginId, PluginSecret: plugin.PluginSecret})
		if err != nil {
			res = snsstruct.PluginTokenResponse{
				ErrDefine: snsglobal.SErrConfig.GetError(snsglobal.CErrPluginToken, "not_found_plugin"),
			}
			return
		}

		token := common.CreateRandomString(512)
		existPlugin.PluginToken = token
		err = models.Update(&existPlugin)
		if err == nil {
			res = snsstruct.PluginTokenResponse{AccessToken: existPlugin.PluginToken}
			break
		} else {
			snslog.E(err)
			continue
		}
	}
	return
}
