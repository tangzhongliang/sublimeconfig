package snsep

import (
	// "fmt"
	"sns/models"
	"sns/util/snserror"
	// "strings"
)

func GetSnsEpByEmail(emails []string, types []string) (epAccounts []models.SnsEpAccount) {
	for _, email := range emails {

		var epAccountEmails []models.SnsEpAccountEmail
		// models.Query(&epAccountEmails, "email = ? and account_ep_type in (?)", email, types)
		models.GetDB().Where("email = ? and account_ep_type in (?)", email, types).Find(&epAccountEmails)

		for _, value := range epAccountEmails {
			epAccounts = append(epAccounts, models.SnsEpAccount{AccountId: value.AccountId, AccountType: value.AccountEPType})
		}
	}
	return
}

func GetSnsEpByPluginId(pluginId string) (accounts []models.SnsEpAccount) {
	//	---------------------------------find account and plugin
	var snsPluginEpAccounts []models.SnsPluginEpAccount
	err := models.Query(&snsPluginEpAccounts, &models.SnsPluginEpAccount{PluginId: pluginId})
	snserror.LogAndPanic(err)

	//	---------------------------------- pack snsPluginEpAccounts into account array
	var emails []string
	for _, value := range snsPluginEpAccounts {
		emails = append(emails, value.Email)
	}
	var snsEpAccountEmails []models.SnsEpAccountEmail
	// models.Query(&snsEpAccountEmails, , emails)
	models.GetDB().Where("email in (?)", emails).Find(&snsEpAccountEmails)
	for _, value := range snsEpAccountEmails {
		accounts = append(accounts, models.SnsEpAccount{AccountId: value.AccountId, AccountType: value.AccountEPType})
	}
	return
}
func GetSNSToken(string) string {
	return ""
}
