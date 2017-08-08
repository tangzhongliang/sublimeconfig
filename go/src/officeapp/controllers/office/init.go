package office

import (
	"crypto/tls"
	"net/http"
	"net/url"
)

var (
	//	host = "172.25.78.77:8080"
	proxy = func(req *http.Request) (*url.URL, error) {
		var useProxy = true
		if useProxy {
			//			cmutil.LogD("proxy", req.URL.String())
			return url.Parse("http://y375i0108:rabbit12@proxy.jp.ricoh.com:8080")
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

func init() {

	//folow steps must be executed by office key
	//1.start token update thread
	StartUpdateAllTokenTask()
	//2.get changes data
	SyncAllSubcribeOfficeResource()
	//3.update subcribe time
	UpdateAllSubcribeOfficeResource()
	//4.start update subcribe thread
	DaemonSubcribe()
}
