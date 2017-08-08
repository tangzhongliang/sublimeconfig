package main

import (
	"fmt"

	//	"dingding/routers"

	//	"github.com/astaxie/beego"
	//	"time"
	"crypto/tls"
	"io/ioutil"
	"net/http"
	"net/url"
)

var (
	proxy = func(req *http.Request) (*url.URL, error) {
		fmt.Println(req.URL.String())
		useProxy := true
		if useProxy {
			//			cmutil.LogD("proxy", req.URL.String())
			return url.Parse("http://zhangchen:rits2019@proxy.jp.ricoh.com:8080")
		} else {
			//			cmutil.LogD("no proxy", req.URL.String())
			return nil, nil
		}
	}
	tlsVerify = &tls.Config{
		InsecureSkipVerify: true,
	}

	transport = &http.Transport{Proxy: proxy, TLSClientConfig: tlsVerify}
	client    = &http.Client{Transport: transport}
)

func main() {

	routers.Init()
	beego.Run()
	// req, _ := http.NewRequest("GET", "https://oapi.dingtalk.com/cspace/get_custom_space?access_token=f745c6bf11f830d9831ed67d4971ee5e&agent_id=110479463&domain=DOMAIN", nil)
	// res, _ := client.Do(req)
	// data, _ := ioutil.ReadAll(res.Body)
	// fmt.Println(string(data))
}
