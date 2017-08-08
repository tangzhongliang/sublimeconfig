package main

import (
	"bytes"
	"crypto/tls"
	"fmt"
	"io/ioutil"
	"mime/multipart"
	"net/http"
	"net/url"
	"os"
)

var (
	proxy = func(req *http.Request) (*url.URL, error) {
		fmt.Println(req.URL.String())
		if true {
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
	userid := "asdfasdf"
	str := `{"msgtype": "text", "text": {"content":` + userid + `"测试！"}}`
	fmt.Println(str)
	buf := new(bytes.Buffer)
	w := multipart.NewWriter(buf)
	//	err := w.WriteField("comment", "我的世界！！！")
	//	if err != nil {

	//	}
	fw, err := w.CreateFormFile("file", "Koala.jpg")
	if err != nil {

	}
	fImg, err := os.Open("D://Koala.jpg")

	if err != nil {
	}
	defer fImg.Close()
	bix, _ := ioutil.ReadAll(fImg)
	_, io_err := fw.Write(bix)
	if io_err != nil {

	}
	fmt.Println(len(bix), w.FormDataContentType())
	w.Close()

	reqest, err := http.NewRequest("POST", "https://oapi.dingtalk.com/file/upload/single?access_token=1ee7bf8312623ca296af9e77b9d4928d&agent_id=110479463&file_size=780831", buf)
	reqest.Header.Set("Content-Type", w.FormDataContentType())

	response, err := client.Do(reqest)
	body, _ := ioutil.ReadAll(response.Body)
	fmt.Printf(string(body))
}
