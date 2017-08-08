package main

import (
	"fmt"
	// "github.com/astaxie/beego"
	// "officeapp/routers"
	"time"
)

func main() {

	layout := "2006-01-02T15:04:05Z07:00"
	ts, err := time.Parse(layout, "2017-06-27T15:10:10+08:00")
	fmt.Println(time.Now().Format(layout))
	if err != nil {
		fmt.Println(err)
		// return false
	} else {
		sub := int(time.Now().UTC().Sub(ts).Minutes())
		fmt.Println("sub", sub)
		if sub > (3600 - 10*60) {
			// fmt.Errorf("%v", err)
			// return false
		} else {
			// return true
		}
	}
	fmt.Println("start")
	// routers.Init()
	// beego.Run()
}
