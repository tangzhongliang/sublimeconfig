package models

type SnsPluginAccount struct {
	BaseModel
	Name               string `gorm:"primary_key"`
	Email              string
	AccountId          string `gorm:"unique;not null"`
	AccountSecret      string `gorm:"not null"`
	Password           string `gorm:"not null"`
	PluginAccountToken string `gorm:"unique"`
}

type SnsPlugin struct {
	BaseModel
	PluginId         string `gorm:"primary_key"`
	AccountName      string `gorm:"not null"`
	PluginName       string `gorm:"not null"`
	PluginSecret     string `gorm:"not null"`
	PluginWebhookUrl string
	PluginButtonUrl  string
	PluginIcon       string
	PluginIconName   string
	PluginToken      string `gorm:"unique;size:512"`
}

type SnsPluginConfig struct {
	BaseModelWithId
	AccountName string
}

type SnsPluginEpAccount struct {
	BaseModel
	// EpAccountId   string `gorm:"unique_index:idx_epaccount_id_type_plugin_id"`
	// EpAccountType string `gorm:"unique_index:idx_epaccount_id_type_plugin_id"`
	Email    string `gorm:"unique_index:idx_email_plugin_id"`
	PluginId string `gorm:"unique_index:idx_email_plugin_id"`
}
