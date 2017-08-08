package office

import (
	"fmt"
	"officeapp/models"
	"time"
)

var (
	subcribeTime    = (12*3 - 1) * time.Hour
	ticker          = time.NewTicker((12*3 - 3) * time.Minute)
	quit            = make(chan bool)
	notificationURL = "https://rocket.hezhensh.com:5443/notify"
)

func SubcribeOfficeResource(office models.OfficeApp, resourceId string) {
	office2, err := GetOfficeToken(office.Domain)
	if err != nil {
		fmt.Errorf("%v", err)
		return
	}
	result, err := subscriptions(office2.AccessToken, resourceId, notificationURL)
	fmt.Println(result, err)
}

func UnSubcribeOfficeResource(office models.OfficeApp, resourceId string) {
	office2, err := GetOfficeToken(office.Domain)
	if err != nil {
		fmt.Errorf("%v", err)
		return
	}
	deleteSubscription(office2.AccessToken, resourceId)
}
func UpdateSubcribeOfficeResource(office models.OfficeApp, subcribe models.OfficeSubcribe) {
	updateSubscription(office.AccessToken, subcribe.ResourceId)
}
func SyncSubcribeOffice(office models.OfficeApp, subcribe models.OfficeSubcribe) {
	httpGet(office.AccessToken, subcribe.ResourceId, subcribe.DeltaLink)
}
func UpdateAllSubcribeOfficeResource() {
	list, _ := models.GetOfficeSubcribeList()
	for _, value := range list {
		office, err := models.GetOfficeToken(value.OfficeId)
		if err != nil {
			panic(err)
		}
		// todo thread serizal by resourceid
		go UpdateSubcribeOfficeResource(office, value)
	}
}
func SyncAllSubcribeOfficeResource() {
	list, _ := models.GetOfficeSubcribeList()
	for _, value := range list {
		office, err := models.GetOfficeToken(value.OfficeId)
		if err != nil {
			panic(err)
		}
		// todo thread serizal by resourceid
		go SyncSubcribeOffice(office, value)
	}
}

/*
	update subcribe time
	start update subcribe thread
*/
func DaemonSubcribe() {
	go func() {
		UpdateAllSubcribeOfficeResource()
		for {
			select {
			case <-ticker.C:
				UpdateAllSubcribeOfficeResource()
			case <-quit:
				ticker.Stop()
				return
			}
		}
	}()
}
