package snscommon

import (
	"bytes"
	"net/smtp"
	"sns/common"
	"sns/common/snsglobal"
	"sns/models"
	"sns/util/snslog"
)

func SendActiveEmail(emailAccount models.EmailAccount) {
	snsglobal.SEmailAuthIdSyncMap.Lock.Lock()
	res := ExecUntilSuccess(func() (res interface{}, ok bool) {
		res = common.CreateRandomString(20)
		_, ok = snsglobal.SEmailAuthIdSyncMap.Get(res.(string))
		ok = !ok
		return
	})
	snsglobal.SEmailAuthIdSyncMap.Set(res.(string), emailAccount)
	snsglobal.SEmailAuthIdSyncMap.Lock.Unlock()

	//	------------------------send active url to email
	url := "http://localhost:8080/user/active/" + res.(string)
	SendContentToEmail(url, emailAccount.Email)
}

func SendContentToEmail(content, email string) {
	// Connect to the remote SMTP server.
	c, err := smtp.Dial("rocket.hezhensh.com:25")
	if err != nil {
		snslog.E(err)
	}
	defer c.Close()
	// Set the sender and recipient.
	c.Mail("sender@example.org")
	c.Rcpt(email)
	// Send the email body.
	wc, err := c.Data()
	if err != nil {
		snslog.E(err)
	}
	defer wc.Close()
	buf := bytes.NewBufferString(content)
	if _, err = buf.WriteTo(wc); err != nil {
		snslog.E(err)
	}
}
