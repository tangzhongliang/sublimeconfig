package routers

import (
	"sns/controllers/web"

	"github.com/astaxie/beego"
	"github.com/astaxie/beego/context"
)

func init() {
	beego.Router("/", &web.MainController{})
	nsWebhook := beego.NewNamespace("/webhook",
		beego.NSCond(func(ctx *context.Context) bool {
			return CheckUrlRequest(ctx)
		}),
		beego.NSRouter("/plugin-list", &web.MainController{}, "get:Get"),
		beego.NSRouter("/epuser/plugin-list", &web.MainController{}, "get:Get"),
		// -----------------------sns ep auth
		beego.NSRouter("/ep/login/ep_line", &web.SnsEpController{}, "get:Login"),
		beego.NSRouter("/ep/login/ep_slack", &web.SnsEpController{}, "get:Login"),
		beego.NSRouter("/ep/login/ep_office", &web.SnsEpController{}, "get:Login"),
		// beego.NSRouter("/ep/auth/ep_line", &web.SnsEpController{}, "get:Auth"),
		// beego.NSRouter("/ep/auth/ep_slack", &web.SnsEpController{}, "get:Auth"),
		// beego.NSRouter("/ep/auth/ep_office", &web.SnsEpController{}, "get:Auth"),

		// -----------------------sns ep webhook
		beego.NSRouter("/ep/notify/ep_line", &web.SnsEpController{}, "post:Notify"),
		beego.NSRouter("/ep/notify/ep_slack", &web.SnsEpController{}, "post:Notify"),
		beego.NSRouter("/ep/notify/ep_office", &web.SnsEpController{}, "post:Notify"),
	)

	nsPage := beego.NewNamespace("/user", beego.NSCond(func(ctx *context.Context) bool {
		return CheckUrlRequest(ctx)
	}),
		beego.NSRouter("/ep/add", &web.UserPageController{}, "get:UserBindEmail"),
		beego.NSRouter("/active/:activeid([\\w-]+)", &web.UserManagerController{}, "get:UserActive"),
		beego.NSRouter("/login", &web.UserManagerController{}, "post:UserLogin"),
		beego.NSRouter("/login", &web.UserPageController{}, "get:UserLoginPage"),
		beego.NSRouter("/register", &web.UserManagerController{}, "post:UserRegister"),
		beego.NSRouter("/services", &web.UserPageController{}, "get:UserServicesPage"),
		beego.NSRouter("/logout", &web.UserPageController{}, "get:UserLogout"),
		beego.NSRouter("/info", &web.UserPageController{}, "get:UserInfoPage"),
		beego.NSRouter("/plugin/:plugin_id([\\w-_.]+)/add", &web.UserPageController{}, "get:UserAddPlugin"),
		beego.NSRouter("/plugin/:plugin_id([\\w-_.]+)/remove", &web.UserPageController{}, "get:UserRemovePlugin"),
		beego.NSRouter("/plugin/info", &web.UserPageController{}, "get:PluginInfoPage"),

		beego.NSRouter("/", &web.UserPageController{}, "get:UserPage"),
	)
	nsIndexPage := beego.NewNamespace("/index", beego.NSCond(func(ctx *context.Context) bool {
		return CheckUrlRequest(ctx)
	}),
		beego.NSRouter("/wenjuan", &web.IndexPageController{}, "get:WenjuanPage"),
		beego.NSRouter("/services", &web.UserPageController{}, "get:ServicesPage"),
	)
	nsApi := beego.NewNamespace("/api", beego.NSCond(func(ctx *context.Context) bool {
		return CheckUrlRequest(ctx)
	}),
		beego.NSRouter("/plugin/gettoken", &web.SnsPluginController{}, "post:RequestPluginToken"),
		beego.NSRouter("/ep/url/check/get", &web.SnsEpController{}, "post:GetEpCheckUrl"),
		beego.NSRouter("/plugin/message", &web.SnsPluginController{}, "post:PluginToEpMessage"),
		beego.NSRouter("/plugin/moni", &web.SnsPluginController{}, "post:Moni"),
	)

	nsFile := beego.NewNamespace("/file", beego.NSCond(func(ctx *context.Context) bool {
		return CheckUrlRequest(ctx)
	}),
		beego.NSRouter("/download/:file([\\w-_.]+)", &web.FileController{}, "get:Download"),
		beego.NSRouter("/upload", &web.FileController{}, "post:Upload"),
	)

	beego.AddNamespace(nsWebhook, nsIndexPage, nsPage, nsApi, nsFile)
}
func CheckUrlRequest(ctx *context.Context) bool {
	//		enable domain check
	//		if ctx.Input.Domain() == "api.beego.me" {
	//			return true
	//		}
	return true
}
