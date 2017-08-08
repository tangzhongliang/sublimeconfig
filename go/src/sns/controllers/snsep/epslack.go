package snsep

import (
	"github.com/astaxie/beego"
	"sns/common/snsstruct"
	"sns/models"
)

type Slack struct {
}

func (this *Slack) GetSnsCheckLoginUrl(emailEncode string) string {
	return ""
}

func (this *Slack) SnsCheckLoginResponse(controller *beego.Controller) (models.SnsEpAccount, bool) {
	var ret models.SnsEpAccount
	return ret, true
}

func (this *Slack) GetAuthUrl() string {
	return ""
}

func (this *Slack) SnsCheckAuthResponse(controller *beego.Controller) (models.SnsEpAccount, bool) {
	var ret models.SnsEpAccount
	return ret, true
}

func (this *Slack) ParseMessageFromWebhook(controller *beego.Controller) snsstruct.EpToPluginMessage {
	var ret snsstruct.EpToPluginMessage
	return ret
}

func (this *Slack) ParseMessageFromJson(postJson string) snsstruct.EpToPluginMessage {
	var ret snsstruct.EpToPluginMessage
	return ret
}
