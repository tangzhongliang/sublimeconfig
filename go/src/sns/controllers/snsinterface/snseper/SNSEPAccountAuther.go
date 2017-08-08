package snseper

import (
	// "sns/models"

	"github.com/astaxie/beego"
	"sns/common/snsstruct"
	"sns/models"
)

type SNSEPAccounAuther interface {
	//send message struct json

	GetSnsCheckLoginUrl(emailEncode string) string
	SnsCheckLoginResponse(controller *beego.Controller) (models.SnsEpAccount, bool)

	ParseMessageFromWebhook(controller *beego.Controller) snsstruct.EpToPluginMessage
	ParseMessageFromJson(postJson string) snsstruct.EpToPluginMessage

	//send file only
	//	SendFileByChannel(token, message, url, channelId string)
	//	SendFileByUser(token, message, url, userId string)
}
