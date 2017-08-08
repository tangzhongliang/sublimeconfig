// test
package office

import (
	"bytes"
	// "crypto/tls"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	// "net/url"
	"time"
)

type Server struct {
	ChangeType         string `json:"changeType"`
	ExpirationDateTime string `json:"expirationDateTime"`
	NotificationURL    string `json:"notificationUrl"`
	Resource           string `json:"resource"`
}

type RespBody struct {
	Aodata_context     string      `json:"@odata.context"`
	ChangeType         string      `json:"changeType"`
	ClientState        interface{} `json:"clientState"`
	ExpirationDateTime string      `json:"expirationDateTime"`
	ID                 string      `json:"id"`
	NotificationURL    string      `json:"notificationUrl"`
	Resource           string      `json:"resource"`
}

// type MyNotify struct {
// 	Value []struct {
// 		SubscriptionID                 string `json:"subscriptionId"`
// 		SubscriptionExpirationDateTime string `json:"subscriptionExpirationDateTime"`
// 		ChangeType                     string `json:"changeType"`
// 		Resource                       string `json:"resource"`
// 		ResourceData                   struct {
// 			_odata_etag string `json:"@odata.etag"`
// 			_odata_id   string `json:"@odata.id"`
// 			_odata_type string `json:"@odata.type"`
// 			ID          string `json:"id"`
// 		} `json:"resourceData"`
// 		ClientState interface{} `json:"clientState"`
// 	} `json:"value"`
// }

func main() {
	authToken := "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFCbmZpRy1tQTZOVGFlN0NkV1c3UWZkcVhUbzlTcEwzX2VzMW5JdGRoNTByUURERDRNSFg3RjA5MFNhcFRyVk9kNG1jRmJYdENFdlA3VGgwY2EwQlR1czlhdmw5bzNPR1lZLWFLMGZxb1VtU3lBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiOUZYRHBiZk1GVDJTdlF1WGg4NDZZVHdFSUJ3Iiwia2lkIjoiOUZYRHBiZk1GVDJTdlF1WGg4NDZZVHdFSUJ3In0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lZjg0NDNkMS04NzYxLTRjZDctYmZlNy1iMTMwZTY3MDBjNDEvIiwiaWF0IjoxNDk4MTIxMTkwLCJuYmYiOjE0OTgxMjExOTAsImV4cCI6MTQ5ODEyNTA5MCwiYWNyIjoiMSIsImFpbyI6IlkyWmdZSEIrRlpkdzhvNlBVaVhQN3Q0bjlsbFI2aHYvTzVkdG5CN0ZzMDVWcEM4M1RCa0EiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6InJpY29oYXBwIiwiYXBwaWQiOiI1ZTQzZDExNy1mMGNkLTQ4MWUtYTA0Ny0xNDk4ODUwZjc3Y2EiLCJhcHBpZGFjciI6IjEiLCJpcGFkZHIiOiIxMzMuMTM5LjcwLjIxNiIsIm5hbWUiOiJEZXZSb29tMSIsIm9pZCI6IjE5ZWVhMjVmLWY4MzgtNDZhZi04ZGMxLTc4NzYzYTNjMGQ1ZiIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzM0ZGRkEyNjBGMEM3Iiwic2NwIjoiQ2FsZW5kYXJzLlJlYWRXcml0ZSBHcm91cC5SZWFkV3JpdGUuQWxsIE1haWwuUmVhZCBVc2VyLlJlYWQiLCJzdWIiOiJiTUptei1WaXJkUFQxQkJOcjdscW94Z2NscXQ1N3NYZ051Szk1T3Jhd284IiwidGlkIjoiZWY4NDQzZDEtODc2MS00Y2Q3LWJmZTctYjEzMGU2NzAwYzQxIiwidW5pcXVlX25hbWUiOiJkZXZyb29tMUBpdzMub25taWNyb3NvZnQuY29tIiwidXBuIjoiZGV2cm9vbTFAaXczLm9ubWljcm9zb2Z0LmNvbSIsInV0aSI6IkRHUmF2T1BTUEVhdVNoaTk2WjBGQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbIjYyZTkwMzk0LTY5ZjUtNDIzNy05MTkwLTAxMjE3NzE0NWUxMCJdfQ.czbt9v5tlcWGkHcfAsVV6i9SI8QIPdJy66EKm0q1STDkG9Cyrx2ucLboLZ82iVUXVAJFfsMFMYexdGJemf8k6GSxPNxhU0FQc_wvq-6A7Eefi6XKGfngfVzogR58iveWPD2ilC9hVVbFn5_OLEcYaFdzwf4Kk06_8v6nwJ1ML9X3P8Ppzejvk-IUs1rAOZCLgbg8VtdEyKPeD0qi3eKxGCOKlKrGn75Kz5itWAktr7BDufEAilabtCo-hJlKvOQsNE4jdRgoMUOoNS5pVxffFUj2it92n8-XPxBNe28uhf4PDBnU7GdRc3SL-nyrSEG2ivstrisl9tUd5bTxRrt1pA"
	resource := "devroom1@iw3.onmicrosoft.com"
	notificationURL := "https://rocket.hezhensh.com:5443/notify"
	result, _ := subscriptions(authToken, resource, notificationURL)
	fmt.Println(result)
	//	deleteSubscription(authToken, "81561da7-8da1-46a3-9e9c-378aeca5b4e0")
}

//Create subscription
func subscriptions(authToken string, subResource string, subNotificationURL string) (rBody RespBody, errRes error) {
	var ser Server

	//TODO 4250是根据服务器时区
	subTime := time.Now().Add(3750 * time.Minute)
	ser.ChangeType = "created,updated,deleted"
	ser.ExpirationDateTime = subTime.Format("2006-01-02T15:04:05Z")
	fmt.Println(subTime.Format("2006-01-02T15:04:05Z"))
	ser.NotificationURL = subNotificationURL
	ser.Resource = "users/" + subResource + "/events"
	b, err := json.Marshal(ser)
	if err != nil {
		fmt.Println("json err:", err)
	}
	body := bytes.NewBuffer([]byte(b))
	res, err := http.NewRequest("POST", "https://graph.microsoft.com/v1.0/subscriptions", body)
	if err != nil {
		panic(err)
		return
	}
	res.Header.Set("Content-Type", "application/json")
	res.Header.Set("Authorization", "Bearer "+authToken)
	resp, err := client.Do(res)
	if err != nil {
		panic(err)
		return
	}
	defer resp.Body.Close()
	fmt.Println(resp.Status)
	if resp.Status == "201 Created" {
		rst, _ := ioutil.ReadAll(resp.Body)
		err := json.Unmarshal(rst, &rBody)
		if err != nil {
			fmt.Printf("err was %v", err)
		}
		return
	}
	return
}

//Delete subscription
func deleteSubscription(authToken string, subId string) {
	apiUrl := "https://graph.microsoft.com/v1.0/subscriptions/" + subId
	res, err := http.NewRequest("DELETE", apiUrl, nil)
	if err != nil {
		panic(err)
		return
	}
	res.Header.Set("Authorization", "Bearer "+authToken)
	resp, err := client.Do(res)
	fmt.Println(resp.Status)
	if err != nil {
		panic(err)
		return
	}
	defer resp.Body.Close()
}

//Update subscription
func updateSubscription(authToken string, subId string) {
	apiUrl := "https://graph.microsoft.com/v1.0/subscriptions/" + subId
	var ser Server
	//TODO 4250是根据服务器时区
	subTime := time.Now().Add(3750 * time.Minute)
	ser.ExpirationDateTime = subTime.Format("2006-01-02T15:04:05Z")
	b, err := json.Marshal(ser)
	if err != nil {
		fmt.Println("json err:", err)
	}
	body := bytes.NewBuffer([]byte(b))
	res, err := http.NewRequest("PATCH", apiUrl, body)
	if err != nil {
		fmt.Println(nil)
		return
	}
	res.Header.Set("Content-Type", "application/json")
	res.Header.Set("Authorization", "Bearer "+authToken)
	resp, err := client.Do(res)
	fmt.Println(resp.Status)
	if err != nil {
		fmt.Println(nil)
		return
	}
	//执行成功返回200 OK
	defer resp.Body.Close()

}
