package web

import (
	"encoding/json"
	"io/ioutil"
	"sns/common/snsglobal"
	"sns/common/snsstruct"
	"sns/controllers/snscommon"
	"sns/controllers/snsplugin"
	"sns/models"

	"github.com/astaxie/beego"
	// "strings"
	"sns/util/snslog"
)

type SnsPluginController struct {
	beego.Controller
}

func (this *SnsPluginController) PluginToEpMessage() {
	body, _ := ioutil.ReadAll(this.Ctx.Request.Body)
	snslog.Df("PluginToEpMessage;%s", string(body))
	pluginToEpMessage, err := snsplugin.ParseFromPluginMessage(string(body))
	snslog.Df("PluginToEpMessage;%s", pluginToEpMessage)
	var res snsstruct.ServiceMessageResponse
	if err != nil {
		res = snsstruct.ServiceMessageResponse{ErrDefine: snsglobal.SErrConfig.GetError(snsglobal.CErrCommon, "message_format_err"), Context: string(this.Ctx.Input.RequestBody)}
	} else {
		token, err2 := snscommon.GetBearerFromRequest(this.Ctx.Request)
		if err2 != nil {
			res = snsstruct.ServiceMessageResponse{ErrDefine: snsglobal.SErrConfig.GetError(snsglobal.CErrCommon, "message_format_err"), Context: string(this.Ctx.Input.RequestBody)}
		}
		res = snscommon.DispatchMessageToEP(pluginToEpMessage, token)
	}
	this.Data["json"] = &res
	this.ServeJSON()
}

func (this *SnsPluginController) RegisterPluginAccount() {

}

func (this *SnsPluginController) LoginPluginAccount() {

}

func (this *SnsPluginController) AddAndEditPlugin() {

}

func (this *SnsPluginController) RequestPluginToken() {
	body, _ := ioutil.ReadAll(this.Ctx.Request.Body)
	var plugin models.SnsPlugin
	err := json.Unmarshal([]byte(body), &plugin)
	//	---------------------check message format
	var res snsstruct.PluginTokenResponse
	if err != nil {
		res = snsstruct.PluginTokenResponse{
			ErrDefine: snsglobal.SErrConfig.GetError(snsglobal.CErrCommon, "message_format_err"),
		}
	} else {
		res = snsplugin.RequestPluginToken(plugin)
	}
	this.Data["json"] = res
	this.ServeJSON()
}

func (this *SnsPluginController) Moni() {
	body, _ := ioutil.ReadAll(this.Ctx.Request.Body)
	snslog.I("(this *SnsPluginController) Moni()", string(body))
	this.Ctx.ResponseWriter.WriteHeader(200)
	this.ServeJSON()
}
