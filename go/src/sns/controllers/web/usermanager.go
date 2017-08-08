package web

import (
	"encoding/json"
	"github.com/astaxie/beego"
	"io/ioutil"
	"sns/common/snsglobal"
	"sns/common/snsstruct"
	"sns/controllers/snscommon"
	"sns/models"
	"sns/util/snslog"
)

type UserManagerController struct {
	beego.Controller
}

func (this *UserManagerController) UserLogin() {
	body, err := ioutil.ReadAll(this.Ctx.Request.Body)
	var emailAccount models.EmailAccount
	json.Unmarshal(body, &emailAccount)
	var emailAccounts []models.EmailAccount
	err = models.Query(&emailAccounts, &emailAccount)
	if err != nil {
		panic(err)
	}
	snslog.Df("%s", string(body))
	if len(emailAccounts) == 1 {
		snscommon.SessionManger.UserLogin(this.Controller, emailAccount.Email)
		this.Data["json"] = snsstruct.WebResponse{Ok: true}
	} else {
		this.Data["json"] = snsstruct.WebResponse{Ok: false}
	}
	this.ServeJSON()
}

func (this *UserManagerController) UserRegister() {
	emailAccount := this.Ctx.Request.FormValue("emailAccount")
	password := this.Ctx.Request.FormValue("password")
	snscommon.SendActiveEmail(models.EmailAccount{Email: emailAccount, Password: password})
	this.Redirect("user/active/wait", 302)
}

func (this *UserManagerController) UserActive() {
	activeId := this.GetString(":activeid")
	email, ok := snsglobal.SEmailAuthIdSyncMap.Get(activeId)
	if ok {
		//	--------------------------email active success;save email to db
		snslog.Df("(this *SnsEpController) Active()/ activeId%s;email%s", activeId, email)
		emailAccount := email.(models.EmailAccount)
		models.InsertOrUpdate(&emailAccount)
		this.SetSession("emailAccount", emailAccount.Email)
		this.Redirect("/user", 302)
	} else {
		//	--------------------------email active failed;redirect to bind email
		snslog.Df("(this *SnsEpController) Active()/ %s", activeId)
		this.Redirect("/user/login", 302)
	}
}
