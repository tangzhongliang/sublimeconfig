package office

import (
	"fmt"
	"github.com/astaxie/beego"
)

type TestController struct {
	beego.Controller
}

func (this *TestController) GetRoomTest() {
	officeapp, err := GetOfficeToken("onmicrosoft.com")
	fmt.Printf("office%+v", officeapp, err)
	this.Data["json"], _ = GetRoomList(officeapp)
	this.ServeJSON()
}
