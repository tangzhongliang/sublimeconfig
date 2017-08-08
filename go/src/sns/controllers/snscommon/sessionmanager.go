package snscommon

import (
	"fmt"
	"github.com/astaxie/beego"
	"github.com/astaxie/beego/cache"
	"sns/util/snslog"
	"strings"
	"time"
)

var (
	bm      cache.Cache
	timeout = 3 * time.Hour
)

type session_manager struct {
}

func init() {
	bm, _ = cache.NewCache("memory", `{"interval":600}`)
}

var SessionManger session_manager

func (this session_manager) UserLogin(controller beego.Controller, name string) {
	controller.SetSession("emailAccount", name)

	//	----------------------save session id ;update time
	sid := GetSessionId(controller)
	fmt.Println("sid:", sid)
	if len(sid) > 0 {
		bm.Put(name, sid, timeout)
	}
}

func (this session_manager) UserLogout(controller beego.Controller, name string) {
	controller.DelSession("emailAccount")
	bm.Delete(name)
}

func GetSessionId(controller beego.Controller) string {
	//	------------------------------get session id
	splits := strings.Split(controller.Ctx.Request.Header.Get("Cookie"), ";")
	var sid string
	for _, split := range splits {
		item := strings.SplitN(split, "=", 2)

		if len(item) == 2 {
			key := strings.Trim(item[0], " ")
			value := strings.Trim(item[1], " ")
			if key == beego.AppConfig.String("sessionname") {
				sid = value
			}
		}
	}
	return sid
}

func (this session_manager) IfRedirectToLogin(controller beego.Controller) bool {
	ret := this.SessionNoUser(controller)
	if ret {
		if controller.Ctx.Request.RequestURI != "/user/login" {
			controller.Redirect("/user/login", 302)
		}
	}
	return ret
}

func (this session_manager) SessionNoUser(controller beego.Controller) bool {
	if controller.Ctx.Request.RequestURI == "/user/login" {
		return true
	}
	var emailAccount string
	tmp := controller.GetSession("emailAccount")
	if tmp != nil {
		emailAccount, _ = tmp.(string)
	}

	if len(emailAccount) == 0 {
		return true
	}

	if bm.IsExist(emailAccount) {
		snslog.D("UpdateSession:", emailAccount)
		saveSId := GetSessionId(controller)
		sid := bm.Get(emailAccount).(string)
		if sid == saveSId {
			bm.Put(emailAccount, sid, timeout)
		} else {
			controller.DelSession("emailAccount")
			return true
		}
	}
	return false
}
