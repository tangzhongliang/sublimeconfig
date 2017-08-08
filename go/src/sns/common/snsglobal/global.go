package snsglobal

import (
	// "github.com/astaxie/beego/cache"
	"sns/common"
	"sns/common/snsfactory"
	// "sns/models"
	//	"sns/util/snserror"
)

var (
	SBeanFactory = snsfactory.RegisterStructMaps{}
	// SDBEngine    = New()
	SErrConfig          = common.NewErrConfig()
	SEmailAuthIdSyncMap = common.NewSyncMap()
)

func init() {

	//	----------------------register interface implement object
	SBeanFactory.Register("snsfactory.Test", &snsfactory.Test{})
}
