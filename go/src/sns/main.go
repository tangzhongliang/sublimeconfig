package main

import (
	"github.com/astaxie/beego"
	_ "sns/common/snsglobal"
	// "sns/common/snsstruct"
	"sns/models"
	_ "sns/routers"
	"sns/util/snserror"
)

func main() {

	beego.BConfig.WebConfig.Session.SessionOn = true
	InitDbData()
	beego.Run()
}
func InitDbData() {
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
	plugin1 := models.SnsPlugin{AccountName: pluginAccount.Name, PluginName: "Plugin1", PluginId: "bbbplugin1", PluginSecret: "bbbplugin1", PluginWebhookUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginButtonUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginToken: "token"}
	err = models.InsertOrUpdate(&plugin1)
	snserror.LogAndPanic(err)

	plugin2 := models.SnsPlugin{AccountName: pluginAccount.Name, PluginName: "Plugin2", PluginId: "bbbplugin2", PluginSecret: "bbbplugin1", PluginWebhookUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginButtonUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginToken: "token2"}
	err = models.InsertOrUpdate(&plugin2)
	snserror.LogAndPanic(err)
	plugin3 := models.SnsPlugin{AccountName: pluginAccount.Name, PluginName: "Plugin3", PluginId: "bbbplugin3", PluginSecret: "bbbplugin1", PluginWebhookUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginButtonUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginToken: "token3"}
	err = models.InsertOrUpdate(&plugin3)
	snserror.LogAndPanic(err)
	plugin4 := models.SnsPlugin{AccountName: pluginAccount.Name, PluginName: "Plugin4", PluginId: "bbbplugin4", PluginSecret: "bbbplugin1", PluginWebhookUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginButtonUrl: "http://rocket.hezhensh.com:8080/api/plugin/moni", PluginToken: "token4"}
	err = models.InsertOrUpdate(&plugin4)
	snserror.LogAndPanic(err)
	// ----------------------------insert snsPluginEpAccounts
	snsPluginEpAccount := models.SnsPluginEpAccount{PluginId: "bbbplugin1", Email: "tangzhongliang@rst.ricoh.com"}
	err = models.InsertOrUpdate(&snsPluginEpAccount)
	snserror.LogAndPanic(err)

	emailAccount := models.EmailAccount{Email: "tangzhongliang@rst.ricoh.com", Password: "123"}
	models.InsertOrUpdate(&emailAccount)
}
