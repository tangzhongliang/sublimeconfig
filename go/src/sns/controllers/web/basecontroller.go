package web

import (
	"html/template"
	// "strings"

	"github.com/astaxie/beego"
	"github.com/beego/i18n"
)

type BaseController struct {
	beego.Controller
	i18n.Locale // For i18n usage when process data and render template.
}

// //多国语部分内容
// var langTypes []string // Languages that are supported.

// func init() {
// 	// Initialize language type list.
// 	langTypes = strings.Split(beego.AppConfig.String("lang_types"), "|")

// 	// Load locale files according to language types.
// 	for _, lang := range langTypes {
// 		beego.Trace("Loading language: " + lang)
// 		if err := i18n.SetMessage(lang, "language/"+"locale_"+lang+".ini"); err != nil {
// 			beego.Error("Fail to set message file:", err)
// 			return
// 		}
// 	}
// }

// // Prepare implemented Prepare() method for baseController.
// // It's used for language option check and setting.
// func (this *BaseController) Prepare() {
// 	// Reset language option.
// 	this.Lang = "" // This field is from i18n.Locale.

// 	// 1. Get language information from 'Accept-Language'.
// 	al := this.Ctx.Request.Header.Get("Accept-Language")
// 	if len(al) > 4 {
// 		al = al[:5] // Only compare first 5 letters.
// 		if i18n.IsExist(al) {
// 			this.Lang = al
// 		}
// 	}

// 	// 从session中获取多国语设定
// 	if this.GetSession("language") != "" && this.GetSession("language") != nil {
// 		this.Lang = this.GetSession("language").(string)
// 	}

// 	// 2. Default language is English.
// 	if len(this.Lang) == 0 {
// 		this.Lang = "ja-JP"
// 	}

// 	// Set template level language option.
// 	this.Data["Lang"] = this.Lang
// }

func (c *BaseController) showErrorWithTitle(title string, msg string) {
	t, _ := template.New("error.html").ParseFiles(beego.AppConfig.String("viewspath") + "/error.html")
	data := make(map[string]interface{})
	data["Title"] = title
	data["Content"] = msg
	t.Execute(c.Ctx.ResponseWriter, data)

	panic(beego.ErrAbort)
}

func (c *BaseController) showError(msg string) {
	c.showErrorWithTitle("Internal Error", msg)
}

func (c *BaseController) GetSessionString(key string) string {
	defer func() {
		if err := recover(); err != nil {
			// snslog.E(err)
		}
	}()
	ret := c.Controller.GetSession(key)
	if ret != nil {
		return ret.(string)
	} else {
		return ""
	}
}
