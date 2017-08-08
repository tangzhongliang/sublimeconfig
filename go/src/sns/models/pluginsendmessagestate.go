package models

type PluginSendMessageState struct {
	BaseModel
	SendMessageId      string `gorm:"primary_key"`
	SendedCount        int    ``
	SendedSuccessCount int
}
