package office

import (
	// "crypto/tls"
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"officeapp/models"
	"ricoh/common/cmutil"
	"strings"
)

type MessageResponse struct {
	OK        bool   `json:"ok"`
	ChannelId string `json:"channel_id"`
	UserId    string `json:"user_id"`
	TS        string `json:"ts"`
	Err       string `json:"error"`
	ErrCode   int    `json:"error_code"`
}

func createSlackRequest(token, channel, text, ts, attachments string) *http.Request {
	form := url.Values{}
	form.Add("token", token)
	form.Add("channel", channel)
	if text != "" {
		form.Add("text", ">>>"+text)
	}
	if ts != "" {
		form.Add("ts", ts)
	}
	if attachments != "" {
		form.Add("attachments", attachments)
	} else {
		form.Add("attachments", "[]")
	}
	cmutil.LogD("send message", channel, text, ts)
	var req *http.Request
	if ts != "" {
		req, _ = http.NewRequest("POST", "https://slack.com/api/chat.update", strings.NewReader(form.Encode()))
	} else {
		req, _ = http.NewRequest("POST", "https://slack.com/api/chat.postMessage", strings.NewReader(form.Encode()))
	}
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	return req
}
func parseResponse(response *http.Response, err error) (*MessageResponse, error) {
	defer response.Body.Close()
	if err != nil {
		return nil, err
	}
	mr := &MessageResponse{}
	if response.StatusCode == 200 {
		//		var body []byte
		body, _ := ioutil.ReadAll(response.Body)
		json.Unmarshal(body, mr)
		if mr.OK == false {
			cmutil.LogE(*mr, string(body))
			return nil, errors.New(mr.Err)
		}
	} else {
		cmutil.LogE(response.StatusCode, response.Body)
	}
	return mr, nil
}
func DeleteMeettingBook(token, eventId, userId string) (msgRes *MessageResponse, errRes error) {
	req, err := http.NewRequest("DELETE", fmt.Sprintf("https://graph.microsoft.com/v1.0/users/%s/calendar/events/%s", userId, eventId), nil)
	req.Header.Add("Authorization", "Bearer "+token)
	if err != nil {
		cmutil.LogE(err)
		msgRes, errRes = nil, err
	} else {
		res, err := client.Do(req)
		if err != nil {
			cmutil.LogE(err)
			msgRes, errRes = nil, err
		} else if res.StatusCode == 204 {
			msgRes, errRes = &MessageResponse{OK: true}, nil
		} else {
			bys, err := ioutil.ReadAll(res.Body)
			msgRes = &MessageResponse{OK: false, ErrCode: res.StatusCode}
			if err == nil {
				msgRes.Err = string(bys)
			}
			errRes = nil
		}
	}
	return
}

type User4Office struct {
	ID                string      `json:"id"`
	BusinessPhones    []string    `json:"BusinessPhones"`
	DisplayName       string      `json:"DisplayName"`
	GivenName         string      `json:"GivenName"`
	JobTitle          interface{} `json:"JobTitle"`
	Mail              interface{} `json:"Mail"`
	MobilePhone       string      `json:"MobilePhone"`
	OfficeLocation    interface{} `json:"OfficeLocation"`
	PreferredLanguage string      `json:"PreferredLanguage"`
	Surname           string      `json:"Surname"`
	UserPrincipalName string      `json:"UserPrincipalName"`
}
type UserList4Office struct {
	OdataContext string        `json:"@odata.context"`
	Value        []User4Office `json:"Value"`
}

func GetUser(office models.OfficeApp, userPrincipalName string) (users []models.User, errRes error) {
	//1.get from db
	//2.get from web
	users, errRes = GetUserListByFilter(office, fmt.Sprintf("$filter=userPrincipalName eq '%s'", userPrincipalName))
	return
}

func GetUserList(office models.OfficeApp) (users []models.User, errRes error) {
	users, errRes = GetUserListByFilter(office, "")
	return
}
func GetUserListByFilter(office models.OfficeApp, filter string) (users []models.User, errRes error) {
	req, err := http.NewRequest("GET", fmt.Sprintf("https://graph.microsoft.com/v1.0/users%s", filter), nil)
	req.Header.Add("Authorization", "Bearer "+office.AccessToken)
	if err != nil {
		cmutil.LogE(err)
		errRes = err
	} else {
		res, err := client.Do(req)
		if err != nil || res.StatusCode != 200 {
			cmutil.LogE(err)
			errRes = err
		} else {
			bys, _ := ioutil.ReadAll(res.Body)

			var user4office UserList4Office
			errRes = json.Unmarshal(bys, &user4office)

			if errRes == nil {
				for _, value := range user4office.Value {
					user := models.User{UserId: value.UserPrincipalName, PrincipalName: value.UserPrincipalName, OfficeId: office.Id}
					if len(value.GivenName) == 0 && len(value.Surname) == 0 {
						user.ResourceType = "room"
					} else {
						user.ResourceType = "user"
					}
					users = append(users, user)
				}
			}
		}
	}
	return
}
func GetRoomList(office models.OfficeApp) (rooms []models.User, errRes error) {
	users, err := GetUserList(office)
	if err != nil {
		errRes = err
		return
	}
	for _, value := range users {
		if value.ResourceType == "room" {
			rooms = append(rooms, value)
		}
	}
	return
}
