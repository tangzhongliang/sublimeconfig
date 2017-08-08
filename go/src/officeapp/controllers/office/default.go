package office

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	// "net/url"
	"strings"
	"time"

	"officeapp/models"

	"github.com/astaxie/beego"
)

// qZe9KdkYmG7sLxercXKdfQV
//https://login.microsoftonline.com/common/adminconsent?client_id=5e43d117-f0cd-481e-a047-1498850f77ca&redirect_uri=http%3A%2F%2Frocket.hezhensh.com%2Fauth&state=12345
//https://login.microsoftonline.com/common/adminconsent?client_id=5e43d117-f0cd-481e-a047-1498850f77ca&redirect_uri=https://rocket.hezhensh.com:4443/auth&state=12345
type MainController struct {
	beego.Controller
}

type MyNotify struct {
	Value []struct {
		SubscriptionID                 string `json:"subscriptionId"`
		SubscriptionExpirationDateTime string `json:"subscriptionExpirationDateTime"`
		ChangeType                     string `json:"changeType"`
		Resource                       string `json:"resource"`
		ResourceData                   struct {
			_odata_etag string `json:"@odata.etag"`
			_odata_id   string `json:"@odata.id"`
			_odata_type string `json:"@odata.type"`
			ID          string `json:"id"`
		} `json:"resourceData"`
		ClientState interface{} `json:"clientState"`
	} `json:"value"`
}
type MyJsonTrackValue struct {
	Aodata_etag string `json:"@odata.etag"`
	Aodata_type string `json:"@odata.type"`
	Attendees   []struct {
		EmailAddress struct {
			Address string `json:"address"`
			Name    string `json:"name"`
		} `json:"emailAddress"`
		Status struct {
			Response string `json:"response"`
			Time     string `json:"time"`
		} `json:"status"`
		Type string `json:"type"`
	} `json:"attendees"`
	Body struct {
		Content     string `json:"content"`
		ContentType string `json:"contentType"`
	} `json:"body"`
	BodyPreview     string        `json:"bodyPreview"`
	Categories      []interface{} `json:"categories"`
	ChangeKey       string        `json:"changeKey"`
	CreatedDateTime string        `json:"createdDateTime"`
	End             struct {
		DateTime string `json:"dateTime"`
		TimeZone string `json:"timeZone"`
	} `json:"end"`
	HasAttachments       bool   `json:"hasAttachments"`
	ICalUID              string `json:"iCalUId"`
	ID                   string `json:"id"`
	Importance           string `json:"importance"`
	IsAllDay             bool   `json:"isAllDay"`
	IsCancelled          bool   `json:"isCancelled"`
	IsOrganizer          bool   `json:"isOrganizer"`
	IsReminderOn         bool   `json:"isReminderOn"`
	LastModifiedDateTime string `json:"lastModifiedDateTime"`
	Location             struct {
		Address struct {
			City            string `json:"city"`
			CountryOrRegion string `json:"countryOrRegion"`
			PostalCode      string `json:"postalCode"`
			State           string `json:"state"`
			Street          string `json:"street"`
		} `json:"address"`
		DisplayName string `json:"displayName"`
	} `json:"location"`
	OnlineMeetingURL interface{} `json:"onlineMeetingUrl"`
	Organizer        struct {
		EmailAddress struct {
			Address string `json:"address"`
			Name    string `json:"name"`
		} `json:"emailAddress"`
	} `json:"organizer"`
	OriginalEndTimeZone        string      `json:"originalEndTimeZone"`
	OriginalStartTimeZone      string      `json:"originalStartTimeZone"`
	Recurrence                 interface{} `json:"recurrence"`
	ReminderMinutesBeforeStart int         `json:"reminderMinutesBeforeStart"`
	ResponseRequested          bool        `json:"responseRequested"`
	ResponseStatus             struct {
		Response string `json:"response"`
		Time     string `json:"time"`
	} `json:"responseStatus"`
	Sensitivity    string      `json:"sensitivity"`
	SeriesMasterID interface{} `json:"seriesMasterId"`
	ShowAs         string      `json:"showAs"`
	Start          struct {
		DateTime string `json:"dateTime"`
		TimeZone string `json:"timeZone"`
	} `json:"start"`
	Subject string `json:"subject"`
	Type    string `json:"type"`
	WebLink string `json:"webLink"`
}
type MyJsonTrack struct {
	Aodata_context   string             `json:"@odata.context"`
	Aodata_nextLink  string             `json:"@odata.nextLink"`
	Aodata_deltaLink string             `json:"@odata.deltaLink"`
	Value            []MyJsonTrackValue `json:"value"`
}

var nextLink string
var httpGetCount int
var apiUrl string
var arrUserId []string

func (c *MainController) Get() {
	c.Data["Website"] = "beego.me"
	c.Data["Email"] = "astaxie@gmail.com"
	c.TplName = "index.tpl"
}
func (this *MainController) Notify() {
	//	result := this.Ctx.Request.FormValue("payload")
	//	fmt.Println(result)
	//	this.Ctx.ResponseWriter.WriteHeader(http.StatusOK)

	token := this.GetString("validationToken", "")
	if token != "" {
		this.Ctx.ResponseWriter.WriteHeader(http.StatusOK)
		//	this.Ctx.ResponseWriter.Write([]byte("Content-type: "))
		this.Ctx.ResponseWriter.Header().Set("Content-length", "7")
		this.Ctx.ResponseWriter.Header().Set("Content-type", "text/plain")
		this.Ctx.ResponseWriter.Write([]byte(token))
	}
	var mynotify MyNotify
	var str = "/Events"
	// authToken := "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFCbmZpRy1tQTZOVGFlN0NkV1c3UWZkcVhUbzlTcEwzX2VzMW5JdGRoNTByUURERDRNSFg3RjA5MFNhcFRyVk9kNG1jRmJYdENFdlA3VGgwY2EwQlR1czlhdmw5bzNPR1lZLWFLMGZxb1VtU3lBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiOUZYRHBiZk1GVDJTdlF1WGg4NDZZVHdFSUJ3Iiwia2lkIjoiOUZYRHBiZk1GVDJTdlF1WGg4NDZZVHdFSUJ3In0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lZjg0NDNkMS04NzYxLTRjZDctYmZlNy1iMTMwZTY3MDBjNDEvIiwiaWF0IjoxNDk4MTIxMTkwLCJuYmYiOjE0OTgxMjExOTAsImV4cCI6MTQ5ODEyNTA5MCwiYWNyIjoiMSIsImFpbyI6IlkyWmdZSEIrRlpkdzhvNlBVaVhQN3Q0bjlsbFI2aHYvTzVkdG5CN0ZzMDVWcEM4M1RCa0EiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6InJpY29oYXBwIiwiYXBwaWQiOiI1ZTQzZDExNy1mMGNkLTQ4MWUtYTA0Ny0xNDk4ODUwZjc3Y2EiLCJhcHBpZGFjciI6IjEiLCJpcGFkZHIiOiIxMzMuMTM5LjcwLjIxNiIsIm5hbWUiOiJEZXZSb29tMSIsIm9pZCI6IjE5ZWVhMjVmLWY4MzgtNDZhZi04ZGMxLTc4NzYzYTNjMGQ1ZiIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzM0ZGRkEyNjBGMEM3Iiwic2NwIjoiQ2FsZW5kYXJzLlJlYWRXcml0ZSBHcm91cC5SZWFkV3JpdGUuQWxsIE1haWwuUmVhZCBVc2VyLlJlYWQiLCJzdWIiOiJiTUptei1WaXJkUFQxQkJOcjdscW94Z2NscXQ1N3NYZ051Szk1T3Jhd284IiwidGlkIjoiZWY4NDQzZDEtODc2MS00Y2Q3LWJmZTctYjEzMGU2NzAwYzQxIiwidW5pcXVlX25hbWUiOiJkZXZyb29tMUBpdzMub25taWNyb3NvZnQuY29tIiwidXBuIjoiZGV2cm9vbTFAaXczLm9ubWljcm9zb2Z0LmNvbSIsInV0aSI6IkRHUmF2T1BTUEVhdVNoaTk2WjBGQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbIjYyZTkwMzk0LTY5ZjUtNDIzNy05MTkwLTAxMjE3NzE0NWUxMCJdfQ.czbt9v5tlcWGkHcfAsVV6i9SI8QIPdJy66EKm0q1STDkG9Cyrx2ucLboLZ82iVUXVAJFfsMFMYexdGJemf8k6GSxPNxhU0FQc_wvq-6A7Eefi6XKGfngfVzogR58iveWPD2ilC9hVVbFn5_OLEcYaFdzwf4Kk06_8v6nwJ1ML9X3P8Ppzejvk-IUs1rAOZCLgbg8VtdEyKPeD0qi3eKxGCOKlKrGn75Kz5itWAktr7BDufEAilabtCo-hJlKvOQsNE4jdRgoMUOoNS5pVxffFUj2it92n8-XPxBNe28uhf4PDBnU7GdRc3SL-nyrSEG2ivstrisl9tUd5bTxRrt1pA"

	content, _ := ioutil.ReadAll(this.Ctx.Request.Body)
	fmt.Println("*******************************\n", string(content))
	err := json.Unmarshal(content, &mynotify)
	if err != nil {
		fmt.Printf("err was %v", err)
	}
	resource := mynotify.Value[0].Resource
	user_id := SubString(resource, 0, UnicodeIndex(resource, str))
	httpGet(mynotify.Value[0].SubscriptionID, user_id, "")

}

type OfficeApp struct {
	TokenType    string `json:"token_type"`
	Scope        string `json:"scope"`
	ExpiresIn    int    `json:"expires_in"`
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
}

func (this *MainController) Auth() {
	// hasuser(this)

}

// 获得指定字符在字符串中的位置
func UnicodeIndex(str, substr string) int {
	result := strings.Index(str, substr)
	if result >= 0 {
		prefix := []byte(str)[0:result]
		rs := []rune(string(prefix))
		result = len(rs)
	}
	return result
}

//截取字符串
func SubString(str string, begin, length int) (substr string) {
	rs := []rune(str)
	lth := len(rs)
	if begin < 0 {
		begin = 0
	}
	if begin >= lth {
		begin = lth
	}
	end := begin + length
	if end > lth {
		end = lth
	}
	return string(rs[begin:end])
}

/*
	1.get changes data
	2.save data
	3.notify service data changes
*/
func httpGet(authToken string, user_id string, apiUrl string) (deltaLink string, errRes error) {
	var track MyJsonTrack
	if len(apiUrl) == 0 {
		timeNow := time.Now().Format("2006-01-02T15:04:05.000Z")
		apiUrl = "https://graph.microsoft.com/v1.0/" + user_id + "/calendarview/delta?startdatetime=" + timeNow + "&enddatetime=2099-12-30T00:00:00Z"
	}
	bt, err := Get(user_id, apiUrl)

	if err != nil {
		fmt.Printf("err was %v", err)
		errRes = err
		return
	}
	err = json.Unmarshal(bt, &track)
	if err != nil {
		fmt.Printf("err was %v", err)
		errRes = err
		return
	}
	var values []MyJsonTrackValue
	if track.Aodata_nextLink != "" {
		apiUrl = track.Aodata_nextLink
		for {
			bt, err = Get(user_id, apiUrl)
			if bt == nil {
				break
			}
			err := json.Unmarshal(bt, &track)
			if err != nil {
				fmt.Printf("err was %v", err)
			}
			values = append(values, track.Value...)
			if track.Aodata_nextLink != "" {
				apiUrl = track.Aodata_nextLink
			}
			if track.Aodata_deltaLink != "" {
				fmt.Println(string(track.Aodata_deltaLink))
				deltaLink = string(track.Aodata_deltaLink)
				break
			}
		}
	}
	//save values
	// 2.save data
	// 3.notify service data changes
	return
}

/*
	domain:office domain or user email or resource id
*/
func Get(identify string, apiUrl string) ([]byte, error) {
	for {
		//1.get token
		//return if token is disable
		var domain string
		//identify is resource subcribe id
		if strings.Contains(identify, ".") {
			officeSubcribe, err := models.GetOfficeSubcribe(identify)
			if err != nil {
				fmt.Errorf("%v", err)
				return nil, err
			} else {
				officeToken, err := models.GetOfficeToken(officeSubcribe.OfficeId)
				if err != nil {
					fmt.Errorf("%v", err)
					return nil, err
				}
				domain = officeToken.Domain
			}

		} else {
			//indentify is user email
			index := strings.LastIndex(identify, "@")
			if index < 0 {
				//indentify is domain
				index = 0
			}
			domain = identify[index:]
		}

		//2.request url
		// go into for only when 401
		officeApp, err := GetOfficeToken(domain)
		if err != nil {
			return nil, err
		} else {
			bys, err := get(officeApp.AccessToken, apiUrl)
			if err != nil && err.Error() == "401" {
				continue
			} else {
				return bys, err
			}
		}

	}
}

func get(token string, apiUrl string) ([]byte, error) {
	resp, err2 := http.NewRequest("GET", apiUrl, nil)
	resp.Header.Set("Authorization", "Bearer "+token)
	if err2 != nil {
		fmt.Errorf("%v", err2)
		return nil, err2
	} else {
		res, err := http.DefaultClient.Do(resp)
		if err != nil || res.StatusCode == 401 {
			fmt.Errorf("%v", err)

			return nil, errors.New("401")
		} else {
			bts, _ := ioutil.ReadAll(res.Body)
			return []byte(bts), nil
		}
	}
}
