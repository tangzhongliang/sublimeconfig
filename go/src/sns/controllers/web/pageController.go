package web

import (
	// "github.com/astaxie/beego"
	"sns/common/snsstruct"
	"sns/controllers/snscommon"
	// "sns/controllers/snsinterface/snseper"
	// "strings"
	"sns/models"
	"sns/util/snslog"
)

type UserPageController struct {
	PageController
}

func (this *UserPageController) UserBindEmail() {
	//set default
	this.Data["accountType"] = "ep_slack"
	this.Data["accountTypeText"] = "slack"
	this.Data["userId"] = ""
	//get session value
	SetValue(this, "accountType", this.GetSessionString("bindAccountType"))
	SetValue(this, "accountTypeText", "line")
	SetValue(this, "userId", this.GetSessionString("bindAccountId"))

	//use url param
	SetValue(this, "accountType", this.GetString("bindAccountType"))
	SetValue(this, "accountTypeText", "line")
	SetValue(this, "userId", this.GetString("bindAccountId"))

	this.TplName = "user_bind_email.html"
}
func SetValue(this *UserPageController, key, value string) {
	if value == "" {
		return
	}
	this.Data[key] = value
}
func (this *UserPageController) UserPage() {
	// this.Layout = "user_layout.html"
	// this.TplName = "user.html"
	this.Redirect("/user/services", 302)
}
func (this *UserPageController) UserLoginPage() {
	this.TplName = "login.html"
	this.LayoutSections["Scripts"] = "tpl/userlogin.tpl"
}
func (this *UserPageController) ServicesPage() {
	var plugins []models.SnsPlugin
	models.Query(&plugins, &models.SnsPlugin{})
	snslog.I("asdfasdfa")
	this.Data["Plugins"] = plugins
	this.TplName = "services.html"
}

func (this *UserPageController) UserServicesPage() {
	var plugins []models.SnsPlugin
	var snsPluginEpAccounts []models.SnsPluginEpAccount
	emailAccount := this.GetSessionString("emailAccount")
	models.Query(&snsPluginEpAccounts, &models.SnsPluginEpAccount{Email: emailAccount})
	var pluginIds []string
	for _, value := range snsPluginEpAccounts {
		pluginIds = append(pluginIds, value.PluginId)
	}
	// models.Query(&plugins, "plugin_id in(?)", pluginIds)
	models.GetDB().Where("plugin_id in(?)", pluginIds).Find(&plugins)
	snslog.If("UserServicesPage%s", plugins)
	this.Data["Plugins"] = plugins
	this.TplName = "services.html"
}

func (this *UserPageController) UserLogout() {
	snscommon.SessionManger.UserLogout(this.Controller, this.GetSessionString("emailAccount"))
	this.Redirect("/user/login", 302)
}
func (this *UserPageController) UserAddPlugin() {
	pluginId := this.GetString(":plugin_id", "")
	res := snsstruct.WebResponse{Ok: true}
	emailAccount := this.GetSessionString("emailAccount")
	err := models.Insert(&models.SnsPluginEpAccount{Email: emailAccount, PluginId: pluginId})
	if err != nil {
		res.Ok = false
		res.Message = err.Error()
	}
	this.Data["json"] = res
	this.ServeJSON()
}
func (this *UserPageController) UserRemovePlugin() {
	pluginId := this.GetString(":plugin_id", "")
	res := snsstruct.WebResponse{Ok: true}
	emailAccount := this.GetSessionString("emailAccount")
	models.Delete(&models.SnsPluginEpAccount{}, "email=?Andplugin_id=?", emailAccount, pluginId)
	this.Data["json"] = res
	this.ServeJSON()
}

func (this *UserPageController) UserInfoPage() {
	emailAccount := this.GetSessionString("emailAccount")
	var snsEpAccountEmails []models.SnsEpAccountEmail
	models.Query(&snsEpAccountEmails, &models.SnsEpAccountEmail{Email: emailAccount})
	this.Data["EpAccounts"] = snsEpAccountEmails
	this.TplName = "user_info.html"
}
func (this *UserPageController) PluginInfoPage() {
	pluginId := this.GetString(":plugin_id", "")
	var snsPlugins []models.SnsPlugin
	models.Query(&snsPlugins, &models.SnsPlugin{PluginId: pluginId})
	this.Data["Plugin"] = snsPlugins[0]
	this.TplName = "service_info.html"
}
