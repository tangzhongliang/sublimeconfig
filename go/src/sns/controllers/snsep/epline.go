package snsep

import (
	"encoding/json"
	"fmt"
	"math/rand"

	"sns/models"
	// "strconv"

	"github.com/astaxie/beego"
	// "github.com/beego/i18n"
	"github.com/line/line-bot-sdk-go/linebot"
	"io/ioutil"
	"net/http"
	"net/url"
	"sns/common/snsglobal"
	"sns/common/snsstruct"
	"sns/util/snslog"
	"strings"
	// "sync"
)

var bot *linebot.Client

type Line struct {
}

func init() {
	snsglobal.SBeanFactory.Register("ep_line", &Line{})
	InitLine()
}
func (this *Line) GetSnsCheckLoginUrl(emailEncode string) string {
	return "https://access.line.me/dialog/oauth/weblogin?response_type=code&client_id=1527826496&redirect_uri=https://rocket.hezhensh.com:8443/webhook/ep/login/ep_line&state=" + emailEncode
}

func (this *Line) SnsCheckLoginResponse(controller *beego.Controller) (models.SnsEpAccount, bool) {
	var snsEpAccount models.SnsEpAccount
	code := controller.GetString("code")
	if len(code) == 0 {
		snslog.E("SnsCheckLoginResponse", controller.Ctx.Request.RequestURI)
		return snsEpAccount, false
	}
	//		------------------------------
	form := url.Values{}
	form.Add("grant_type", "authorization_code")
	form.Add("client_id", "1527826496")
	form.Add("client_secret", "1366ac6c8729f589a8e49412a9f253e2")
	form.Add("code", code)
	form.Add("redirect_uri", "https://rocket.hezhensh.com:8443/webhook/ep/login/ep_line")
	snslog.Df("SnsCheckLoginResponse0%s", form.Encode())
	req, _ := http.NewRequest("POST", "https://api.line.me/v2/oauth/accessToken", strings.NewReader(form.Encode()))
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	res, _ := http.DefaultClient.Do(req)
	body, _ := ioutil.ReadAll(res.Body)
	snslog.D("SnsCheckLoginResponse1", string(body))
	var lineAccessTokenResponse snsstruct.LineAccessTokenResponse
	json.Unmarshal(body, &lineAccessTokenResponse)

	//		------------------------------
	req, _ = http.NewRequest("GET", "https://api.line.me/v2/profile", nil)
	req.Header.Add("Authorization", "Bearer "+lineAccessTokenResponse.AccessToken)
	res, _ = http.DefaultClient.Do(req)
	body, _ = ioutil.ReadAll(res.Body)
	var lineProfile snsstruct.LineProfile
	snslog.D("SnsCheckLoginResponse2", string(body))
	json.Unmarshal(body, &lineProfile)
	snsEpAccount.AccountId = lineProfile.UserID
	snsEpAccount.AccountType = "line"
	return snsEpAccount, true
}

func (this *Line) ParseMessageFromJson(postJson string) snsstruct.EpToPluginMessage {
	return snsstruct.EpToPluginMessage{}
}
func (this *Line) SendAttachmentByUser(token string, userId string, msg snsstruct.PluginToEpMessageData) {
	// this.SendMessageOnly(userId, msg.Text)
	_, err := bot.PushMessage(userId, GenerateMessage(msg)...).Do()
	// this.SendConfirmMessage(userId, "", "")
	if err != nil {
		snslog.E(err)
	}
}

//var AutoLine Line
func GetLineBotInstance() (botInstance *linebot.Client, err error) {
	if bot == nil {
		err = InitLine()
	}
	return bot, err
}

func InitLine() (err error) {
	// pGlobal.LineCallbackId = pStruct.LineMutexMap{
	// 	Lock: new(sync.RWMutex),
	// 	Bm:   make(map[string]string),
	// }
	bot, err = linebot.New(
		beego.AppConfig.String("linebotsecret"),
		beego.AppConfig.String("linebotAccessToken"),
	)
	return err
}

type Data struct {
	CallbackId string
	Value      string
	Name       string
	Text       string
}

func GenerateMessage(msg snsstruct.PluginToEpMessageData) (messages []linebot.Message) {
	snslog.Df("%+v", msg)
	snslog.Df("%+v", msg.Attachments[0])

	if len(msg.Link.Url) > 0 {
		btns := linebot.NewButtonsTemplate("", "", msg.Text, linebot.NewURITemplateAction(msg.Link.Title, msg.Link.Url))
		messages = append(messages, linebot.NewTemplateMessage(msg.Text, btns))
	} else {
		messages = append(messages, linebot.NewTextMessage(msg.Text))
	}

	if len(msg.Attachments) > 0 && msg.Attachments[0].AttachmentType == "confirm" {
		//text + link + confirm
		data := Data{CallbackId: msg.Attachments[0].CallbackId, Value: msg.Attachments[0].Actions[0].Value, Name: msg.Attachments[0].Actions[0].Name, Text: msg.Attachments[0].Actions[0].Text}
		bs, _ := json.Marshal(data)
		left := linebot.NewPostbackTemplateAction(data.Text, string(bs), "")
		data2 := Data{CallbackId: msg.Attachments[0].CallbackId, Value: msg.Attachments[0].Actions[1].Value, Name: msg.Attachments[0].Actions[1].Name, Text: msg.Attachments[0].Actions[1].Text}
		bs2, _ := json.Marshal(data2)
		right := linebot.NewPostbackTemplateAction(data2.Text, string(bs2), "")
		template := linebot.NewConfirmTemplate("Yes?", left, right)
		message := linebot.NewTemplateMessage("http://www.baidu.com", template)
		messages = append(messages, message)
	}
	return
}

func GetUserDisplayName(UserID string) string {
	userPrefile, err := bot.GetProfile(UserID).Do()
	if err != nil {
		return ""
	}
	return userPrefile.DisplayName
}
func RandInt(min, max int) int {
	if min >= max || min == 0 || max == 0 {
		return max
	}
	return rand.Intn(max-min) + min
}

func (this *Line) ParseMessageFromWebhook(c *beego.Controller) (msg snsstruct.EpToPluginMessage) {
	// get Post callback data
	fmt.Println("LineMessageButtonCallback")
	bot, err := GetLineBotInstance()
	events, err := bot.ParseRequest(c.Ctx.Request)
	if err != nil {
		snslog.Ef("LineMessageButtonCallback ParseRequest err: %v ", err)
		if err == linebot.ErrInvalidSignature {
			c.Ctx.ResponseWriter.WriteHeader(400)
		} else {
			c.Ctx.ResponseWriter.WriteHeader(500)
		}
		return
	}
	c.Ctx.ResponseWriter.WriteHeader(200)
	fmt.Printf("events%s\n", events)
	for _, event := range events {
		if event.Source.UserID == "" &&
			event.Source.GroupID == "" &&
			event.Source.RoomID == "" {
			snslog.If("UserId or GroupId is empty")
			continue
		} else {
			snslog.If("find userId:", event.Source.UserID, "groupID:", event.Source.GroupID, "RoomID:", event.Source.RoomID)
		}
		if event.Type == linebot.EventTypeFollow { //FollowEvent（Botが友達追加された）
			var tmp []models.SnsEpAccount
			var user = models.SnsEpAccount{AccountId: event.Source.UserID, AccountType: "line"}
			models.QueryByKey(&tmp, &user)

			if len(tmp) != 0 {
				user = tmp[0]
			}
			user.Status = 1
			models.InsertOrUpdate(&user)
			snslog.I("linebot.EventTypeFollow insertorupdate", user)
			this.SendAttachmentByUser("", user.AccountId, snsstruct.PluginToEpMessageData{Text: "hello from sns"})
		} else if event.Type == linebot.EventTypeMessage { //Message Event
			switch event.Message.(type) {
			case *linebot.TextMessage:
				snslog.I("linebot.EventTypeMessage", event.Message)
			}
		} else if event.Type == linebot.EventTypePostback {
			dataStr := event.Postback.Data
			snslog.D("event.Type == linebot.EventTypePostback", dataStr)
			var data Data
			json.Unmarshal([]byte(dataStr), &data)
			msg.User = models.SnsEpAccount{AccountId: event.Source.UserID, AccountType: "line"}
			msg.Message = snsstruct.EpToPluginMessageData{
				SnsEpResponse: snsstruct.SnsEpResponse{
					CallbackId: data.CallbackId,
					Actions:    snsstruct.ActionResponse{Name: data.Name, Value: data.Value},
				},
			}
		}

	}
	return
}
