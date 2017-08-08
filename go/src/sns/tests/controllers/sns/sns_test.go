package snstest

import (
	_ "sns/common/snsglobal"
	"sns/common/snsstruct"
	"sns/controllers/snscommon"
	"sns/controllers/snsep"
	"sns/models"
	"sns/util/snserror"
	"testing"
)

func init() {
	var err error

	//	---------------------------insert ep account
	err = models.InsertOrUpdate(&models.SnsEpAccount{AccountId: "snstest111", AccountType: "slack"})
	snserror.LogAndPanic(err)
	err = models.InsertOrUpdate(&models.SnsEpAccount{AccountId: "snstest222", AccountType: "slack"})
	snserror.LogAndPanic(err)
	err = models.InsertOrUpdate(&models.SnsEpAccount{AccountId: "snstest333", AccountType: "line"})
	snserror.LogAndPanic(err)

	//	--------------------------insert account email combine
	err = models.InsertOrUpdate(&models.SnsEpAccountEmail{AccountEPType: "slack", AccountId: "snstest111", Email: "1@qq.com"})
	snserror.LogAndPanic(err)
	err = models.InsertOrUpdate(&models.SnsEpAccountEmail{AccountEPType: "line", AccountId: "snstest333", Email: "1@qq.com"})
	snserror.LogAndPanic(err)

	// ---------------------------insert plugin account
	pluginAccount := models.SnsPluginAccount{Name: "plugin1", Password: "pwd", AccountId: "bbb", AccountSecret: "ccc"}
	err = models.InsertOrUpdate(&pluginAccount)
	snserror.LogAndPanic(err)
	plugin := models.SnsPlugin{AccountName: pluginAccount.Name, PluginId: "bbbplugin1", PluginWebhookUrl: "PluginWebhookUrl", PluginButtonUrl: "PluginButtonUrl", PluginToken: "token"}
	err = models.InsertOrUpdate(&plugin)
	snserror.LogAndPanic(err)

	// ----------------------------insert snsPluginEpAccounts
	snsPluginEpAccount := models.SnsPluginEpAccount{PluginId: "bbbplugin1", EpAccountId: "snstest111", EpAccountType: "slack"}
	err = models.InsertOrUpdate(&snsPluginEpAccount)
	snserror.LogAndPanic(err)
}

func TestSendMessageToEp(t *testing.T) {
	accounts := []models.SnsEpAccount{models.SnsEpAccount{AccountId: "snstest111", EPType: "slack"},
		models.SnsEpAccount{AccountId: "snstest222", EPType: "slack"},
		models.SnsEpAccount{AccountId: "snstest333", EPType: "line"},
	}
	snscommon.SendMessageToEp(accounts)
}

func TestFindByEmail(t *testing.T) {
	// var out []models.SnsEpAccountEmail
	// err := models.GetDB().Where("email=? and account_ep_type in (?)", "1@qq.com", []string{"slack", "line"}).Find(&out).Error
	// snserror.LogAndPanic(err)
	// snslog.I("result", out)
	accounts := snsep.GetSnsEpByEmail([]string{"1@qq.com"}, []string{"slack", "line"})
	if len(accounts) == 0 {
		t.Fail()
	} else {
		t.Logf("TestFindByEmail%+v", accounts)
	}
}

func TestDispatchMessageToEP(t *testing.T) {
	msg := snsstruct.PluginToEpMessage{PluginId: "bbbplugin1",
		TargetUsers: []models.SnsEpAccount{models.SnsEpAccount{AccountId: "snstest111", AccountType: "slack"}},
		Message:     snsstruct.PluginToEpMessageData{Text: "welcom to sns"}}
	response := snscommon.DispatchMessageToEP(msg, "token")
	if !response.Ok {
		t.Logf("TestDispatchMessageToEP%+v", response)
		t.Fail()
	}

	msg = snsstruct.PluginToEpMessage{PluginId: "bbbplugin1",
		TargetEmails: snsstruct.PluginToEpMessageEmail{TargetUserEmail: []string{"1@qq.com"}, Platforms: []string{"line", "slack"}},
		Message:      snsstruct.PluginToEpMessageData{Text: "welcom to sns"}}
	response = snscommon.DispatchMessageToEP(msg, "token")
	if !response.Ok {
		t.Logf("TestDispatchMessageToEP%+v", response)
		t.Fail()
	}
}
