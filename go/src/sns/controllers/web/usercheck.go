package web

import (
	"sns/controllers/snscommon"
)

type UserCheckController struct {
	BaseController
}

func (this *UserCheckController) Prepare() {
	this.BaseController.Prepare()

	//	-----------------update session fail
	logined := !snscommon.SessionManger.IfRedirectToLogin(this.Controller)
	this.Data["logined"] = logined
	if logined {

		this.Data["logined"] = this.GetSessionString("emailAccount")
	}
}
