package office

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"officeapp/models"
	"strings"

	"errors"

	"github.com/astaxie/beego"
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
	c.TplName = "index.tpl"
}
func (this *AuthController) Notify() {
	result := this.Ctx.Request.FormValue("payload")
	fmt.Println(result)
	this.Ctx.ResponseWriter.WriteHeader(http.StatusOK)
}

func (this *AuthController) Auth() {
	fmt.Println(this.Ctx.Request.RequestURI)
	if this.GetString("code") != "" {
		hasuser(this)
	} else {
		nouser(this)
	}
}

func hasuser(this *AuthController) {
	fmt.Println("hasuser")
	result := this.GetString("code", "")
	fmt.Println(result)

	form := url.Values{}
	form.Add("client_id", "5e43d117-f0cd-481e-a047-1498850f77ca")
	form.Add("scope", scope)
	// form.Add("scope", "https://graph.microsoft.com/.default")
	form.Add("code", result)
	form.Add("redirect_uri", "https://rocket.hezhensh.com:4443/auth")
	form.Add("client_secret", "qZe9KdkYmG7sLxercXKdfQV")
	form.Add("grant_type", "authorization_code")
	req, err := http.NewRequest("POST", "https://login.microsoftonline.com/common/oauth2/v2.0/token", strings.NewReader(form.Encode()))
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	app := models.OfficeApp{}
	if err != nil {
		panic(err)
	} else {
		res, err := http.DefaultClient.Do(req)
		if err != nil {
			panic(err)
		} else {
			bts, _ := ioutil.ReadAll(res.Body)
			fmt.Println(string(bts))

			json.Unmarshal(bts, &app)
		}
	}
	this.Data["json"] = app
	this.ServeJSON()
	// this.Ctx.ResponseWriter.WriteHeader(http.StatusOK)
}
func refreshtoken(app models.OfficeApp) models.OfficeApp {
	form := url.Values{}
	form.Add("client_id", client_id)
	form.Add("scope", scope)
	// form.Add("scope", "https://graph.microsoft.com/.default")
	// form.Add("code", result)
	form.Add("redirect_uri", redirect)
	form.Add("client_secret", secret)
	form.Add("grant_type", "refresh_token")
	req, err := http.NewRequest("POST", "https://login.microsoftonline.com/common/oauth2/v2.0/token", strings.NewReader(form.Encode()))
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	if err != nil {
		panic(err)
	} else {
		res, err := http.DefaultClient.Do(req)
		if err != nil {
			panic(err)
		} else {
			bts, _ := ioutil.ReadAll(res.Body)
			fmt.Println(string(bts))
			app := models.OfficeApp{}
			json.Unmarshal(bts, &app)
		}
	}
	return app
}
func nouser(this *AuthController) {
	fmt.Println("nouser")
	result := this.GetString("tenant", "")
	if result != "" {
		tenant = result
	}
	fmt.Println("tenant", result)
	this.Data["json"], _ = getTokenByTenant(tenant)
	this.ServeJSON()
}
func (this *AuthController) GetToken() {
	this.Data["json"], _ = getTokenByTenant(tenant)
	this.ServeJSON()
}

func getTokenByTenant(tenant string) (app models.OfficeApp, errRes error) {
	form := url.Values{}
	form.Add("client_id", "31627eaa-d8a8-4229-a0c3-106b9db2b29c")
	// form.Add("scope", "user.read mail.read")
	form.Add("scope", "https://graph.microsoft.com/.default")
	// form.Add("code", result)
	// form.Add("redirect_uri", "https%3A%2F%2Frocket.hezhensh.com%3A4443")
	form.Add("client_secret", "inPZCP6p0oq/ReghhXiMiAgrOOqCV6q/hEE2dQof054=")
	// form.Add("grant_type", "authorization_code")
	form.Add("grant_type", "client_credentials")
	req, err := http.NewRequest("POST", "https://login.microsoftonline.com/"+tenant+"/oauth2/v2.0/token", strings.NewReader(form.Encode()))
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	if err != nil {
		errRes = err
		fmt.Println(err)
	} else {
		res, err := http.DefaultClient.Do(req)
		if err != nil {
			errRes = err
			fmt.Println("response", err)
		} else {
			bts, _ := ioutil.ReadAll(res.Body)
			fmt.Println(string(bts))
			errRes = json.Unmarshal(bts, &app)
			if app.AccessToken == "" {
				errRes = errors.New(string(bts))
			}
		}
	}
	return
}
