package web

import (
	"encoding/json"
	// "github.com/astaxie/beego"
	"io/ioutil"
	"sns/common/snsglobal"
	"sns/common/snsstruct"
	"sns/controllers/snscommon"
	// "sns/controllers/snsep"
	"net/url"
	"sns/controllers/snsinterface/snseper"
	"sns/controllers/snsplugin"
	"sns/models"
	"sns/util/snserror"
	"sns/util/snslog"
	"strings"
)

type SnsEpController struct {
	BaseController
}

func GetLastString(uri string) (snstype string) {
	u, err := url.Parse(uri)
	if err != nil {
		panic(err)
	}
	uri = u.Path
	snstypeIndex := strings.LastIndex(uri, "/")
	snstype = uri[snstypeIndex+1:]
	return
}

func (this *SnsEpController) Login() {
	if snscommon.SessionManger.IfRedirectToLogin(this.Controller) {
		return
	}
	snstype := GetLastString(this.Ctx.Request.RequestURI)
	snsEpAuther := snsglobal.SBeanFactory.New(snstype).(snseper.SNSEPAccounAuther)
	epAccount, ret := snsEpAuther.SnsCheckLoginResponse(&this.Controller)
	snslog.D("(this *SnsEpController) Login() ", epAccount)
	emailAccount := this.GetSessionString("emailAccount")
	if ret && len(emailAccount) > 0 {
		snsEpAccountEmail := models.SnsEpAccountEmail{AccountId: epAccount.AccountId, AccountEPType: epAccount.AccountType, Email: emailAccount}
		err := models.InsertOrUpdate(&snsEpAccountEmail)
		snserror.LogAndPanic(err)
	} else {
		// this.Redirect("user/ep/add?accountType="+snstype, 302)
	}
	this.Redirect("/user/ep/add?accountType="+snstype, 302)
}

// func (this *SnsEpController) Auth() {
// 	snstype := GetLastString(this.Ctx.Request.RequestURI)
// 	snsEpAuther := snsglobal.SBeanFactory.New(snstype).(snseper.SNSEPAccounAuther)
// 	snsEpAuther.SnsCheckAuthResponse(&this.Controller)
// }

func (this *SnsEpController) Notify() {
	snstype := GetLastString(this.Ctx.Request.RequestURI)
	snsEpSender := snsglobal.SBeanFactory.New(snstype).(snseper.SNSEPAccounAuther)
	epToPluginMessage := snsEpSender.ParseMessageFromWebhook(&this.Controller)
	snslog.Df("notify%s;%+v", snstype, epToPluginMessage)
	epToPluginMessage, err := snsplugin.ParseToPluginMessage(epToPluginMessage)
	if err != nil {
		snslog.E(err)
		return
	}
	snscommon.DispatchMessageToPlugin(epToPluginMessage)
}

func (this *SnsEpController) GetEpCheckUrl() {
	body, _ := ioutil.ReadAll(this.Ctx.Request.Body)
	snslog.D(string(body))
	ret := make(map[string]interface{})
	var data snsstruct.SnsEpEmailBindRequest
	err := json.Unmarshal(body, &data)
	data.Email = this.GetSessionString("emailAccount")
	if err != nil {
		ret["ok"] = false
	} else {
		ret["ok"] = true
		ret["url"] = snsglobal.SBeanFactory.New("ep_line").(snseper.SNSEPAccounAuther).GetSnsCheckLoginUrl(data.Email)
	}
	this.Data["json"] = ret
	this.ServeJSON()
}
