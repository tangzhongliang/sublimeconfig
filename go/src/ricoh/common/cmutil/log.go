package cmutil

import (
	"fmt"
	"log"

	"runtime"

	"github.com/astaxie/beego"
	"github.com/astaxie/beego/logs"
)

//save to file
var TAG = "ribot/cloud:"

func Init() {
	logs.SetLogger(logs.AdapterFile, `{"filename":"project.log"}`)
	logs.EnableFuncCallDepth(true)
	beego.SetLogger(logs.AdapterFile, `{"filename":"project.log"}`)
}

func I(v ...interface{}) {
	_, file, no, ok := runtime.Caller(1)
	if ok {
		beego.Info("called from %s#%d", file, no)
	}
	beego.Info(v)
}
func If(format string, v ...interface{}) {
	beego.Info(fmt.Sprintf(format, v...))
}

func D(v ...interface{}) {
	_, file, no, ok := runtime.Caller(1)
	if ok {
		beego.Debug("called from %s#%d", file, no)
	}
	beego.Debug(v)
}
func Df(format string, v ...interface{}) {
	beego.Debug(fmt.Sprintf(format, v...))
}

func E(v ...interface{}) {
	beego.Error(v)
}
func Ef(format string, v ...interface{}) {
	beego.Error(fmt.Sprintf(format, v...))
}

func W(v ...interface{}) {
	beego.Warning(v)
}

func Wf(format string, v ...interface{}) {
	beego.Warning(fmt.Sprintf(format, v...))
}
func Logf(v ...interface{}) {
	log.Fatal(TAG, v)
}
func LogI(v ...interface{}) {
	_, file, no, ok := runtime.Caller(1)
	if ok {
		beego.Info("called from %s#%d", file, no)
		_, file, no, ok := runtime.Caller(2)
		if ok {
			beego.Debug("called from %s#%d", file, no)
		}
	}
	beego.Info(TAG, v)
}
func LogPrintln(v ...interface{}) {
	log.Println(TAG, v)
}
func Println(v ...interface{}) {
	log.Println(TAG, v)
}
func LogD(v ...interface{}) {
	_, file, no, ok := runtime.Caller(1)
	if ok {
		beego.Debug("called1 from %s#%d", file, no)
		_, file, no, ok := runtime.Caller(2)
		if ok {
			beego.Debug("called2 from %s#%d", file, no)
		}
	}
	beego.Debug(TAG, v)
}
func LogE(v ...interface{}) {
	_, file, no, ok := runtime.Caller(1)
	if ok {
		beego.Debug("called1 from %s#%d", file, no)
		_, file, no, ok := runtime.Caller(2)
		if ok {
			beego.Debug("called2 from %s#%d", file, no)
			_, file, no, ok := runtime.Caller(3)
			if ok {
				beego.Debug("called3 from %s#%d", file, no)
			}
		}

	}
	beego.Error(TAG, v)
}
func LogW(v ...interface{}) {
	_, file, no, ok := runtime.Caller(1)
	if ok {
		beego.Warn("called from %s#%d", file, no)
	}
	beego.Warn(TAG, v)
}
