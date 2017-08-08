package snseper

import (
	"sns/common/snsstruct"
)

type SnsEPMessageSender interface {
	//send message struct json
	// SendAttachmentByChannel(token string, userId string, msg snsstruct.PluginToEpMessageData)
	SendAttachmentByUser(token string, userId string, msg snsstruct.PluginToEpMessageData)

	//send file only
	//	SendFileByChannel(token, message, url, channelId string)
	//	SendFileByUser(token, message, url, userId string)
}
