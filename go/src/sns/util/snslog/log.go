package snslog

import (
	"fmt"
	"strings"

	"github.com/astaxie/beego"
	"github.com/astaxie/beego/logs"
	"github.com/astaxie/beego/orm"
)

func Init() {

	logLevel, _ := beego.AppConfig.Int("logLevel")
	logORM, _ := beego.AppConfig.Bool("logORM")

	//	logs.Async()
	logs.SetLevel(logLevel)
	logs.SetLogFuncCall(true)
	logs.EnableFuncCallDepth(true)
	orm.Debug = logORM

	// set http access log
	beego.BConfig.Log.AccessLogs = true

	// TODO add a new log instance and output Emergency log to slack
	//logs.SetLogger(logs.AdapterSlack, `{"webhookurl":"https://slack.com/xxx","level":1}`)
	// TODO log to file
	//logs.SetLogger(logs.AdapterConsole, `{"level":1}`)
	//logs.SetLogger(logs.AdapterFile,`{"filename":"persian.log","level":7,"maxlines":0,"maxsize":0,"daily":true,"maxdays":20}`)

}

func I(v ...interface{}) {
	logs.Info(generateFmtStr(len(v)), v...)
}
func If(format string, v ...interface{}) {
	logs.Info(format, v...)
}

func D(v ...interface{}) {
	logs.Debug(generateFmtStr(len(v)), v...)
}
func Df(format string, v ...interface{}) {
	logs.Debug(format, v...)
}

func E(v ...interface{}) {
	logs.Error(generateFmtStr(len(v)), v...)
}
func Ef(format string, v ...interface{}) {
	logs.Error(format, v...)
}

func W(v ...interface{}) {
	logs.Warning(generateFmtStr(len(v)), v...)
}

func Wf(format string, v ...interface{}) {
	logs.Warning(format, v...)
}

func F(v ...interface{}) {
	logs.Emergency(generateFmtStr(len(v)), v...)
}

func Ff(format string, v ...interface{}) {
	logs.Emergency(format, v...)
}

func generateFmtStr(n int) string {
	return strings.Repeat("%v ", n)
}
func Errorf(format string, v ...interface{}) error {
	return fmt.Errorf(format, v)
}
