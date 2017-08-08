package office

import (
	// "encoding/json"
	"fmt"
	// "io/ioutil"
	// "net/http"
	// "net/url"
	// "officeapp/models"
	// "strings"

	// "errors"

	"github.com/astaxie/beego"
	"os"
)

var (
	// 5e43d117-f0cd-481e-a047-1498850f77ca
	//qZe9KdkYmG7sLxercXKdfQV

	// 31627eaa-d8a8-4229-a0c3-106b9db2b29c
	// inPZCP6p0oq/ReghhXiMiAgrOOqCV6q/hEE2dQof054=
	client_id = "31627eaa-d8a8-4229-a0c3-106b9db2b29c"
	secret    = "inPZCP6p0oq/ReghhXiMiAgrOOqCV6q/hEE2dQof054="
	scope     = "offline_access user.read mail.read Calendars.ReadWrite"
	redirect  = "https://rocket.hezhensh.com:4443/auth"
	tenant    = "ef8443d1-8761-4cd7-bfe7-b130e6700c41"
)

// qZe9KdkYmG7sLxercXKdfQV
//https://login.microsoftonline.com/common/adminconsent?client_id=5e43d117-f0cd-481e-a047-1498850f77ca&redirect_uri=http%3A%2F%2Frocket.hezhensh.com%2Fauth&state=12345
//https://login.microsoftonline.com/common/adminconsent?client_id=5e43d117-f0cd-481e-a047-1498850f77ca&redirect_uri=https://rocket.hezhensh.com:4443/auth&state=12345
type AuthController struct {
	beego.Controller
}

func (c *AuthController) Get() {
	c.Data["Website"] = "beego.me"
	c.Data["Email"] = "astaxie@gmail.com"
	fmt.Println(c.Ctx.Request.RequestURI)
	c.TplName = "index.html"
}
