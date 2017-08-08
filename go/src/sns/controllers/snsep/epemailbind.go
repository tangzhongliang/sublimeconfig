package snsep

import (
	"sns/common/snsglobal"
	"sns/common/snsstruct"
	"sns/controllers/snsinterface/snseper"
)

func GetEpCheckUrl(req snsstruct.SnsEpEmailBindRequest) (url string) {

	switch req.AccountType {
	case "line":
		return snsglobal.SBeanFactory.New("ep_slack").(snseper.SNSEPAccounAuther).GetSnsCheckLoginUrl(req.Email)
	case "slack":
		return snsglobal.SBeanFactory.New("ep_slack").(snseper.SNSEPAccounAuther).GetSnsCheckLoginUrl(req.Email)
	case "teams":
		return snsglobal.SBeanFactory.New("ep_team").(snseper.SNSEPAccounAuther).GetSnsCheckLoginUrl(req.Email)
	default:
		return ""
	}
}
