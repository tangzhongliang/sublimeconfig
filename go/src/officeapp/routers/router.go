package routers

import (
	"officeapp/controllers/office"

	"github.com/astaxie/beego"
)

func Init() {
	beego.Router("/", &office.AuthController{})
	beego.Router("/notify", &office.MainController{}, "POST:Notify")
	beego.Router("/auth", &office.AuthController{}, "GET:Auth")
	beego.Router("/getToken", &office.AuthController{}, "GET:GetToken")
	beego.Router("/getRoomTest", &office.TestController{}, "GET:GetRoomTest")
}
