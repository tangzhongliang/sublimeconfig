package dao

import (
	"fmt"
	_ "sns/common/snsglobal"
	"sns/models"
	"sns/util/snserror"
	"sns/util/snslog"
	"testing"
	"time"
)

var err error

func TestNew(t *testing.T) {
	err = models.InsertOrUpdate(&models.SnsEpmodels.models.d: "1111", EPType: "slack", Email: "asdfasdf"})
	snserror.LogAndPanic(err)
	err = models.InsertOrUpdate(&models.SnsEpmodels.models.d: "2222", EPType: "slack2", Email: "asdfasdf"})
	snserror.LogAndPanic(err)
	err = models.InsertOrUpdate(&models.SnsEpmodels.models.d: "3333", EPType: "slack", Email: "asdfasdf"})
	snserror.LogAndPanic(err)
	now := time.Now()
	for i := 0; i < 1000; i++ {
		err = models.InsertOrUpdate(&models.SnsEpmodels.models.d: fmt.Sprintf("1111%d", i), EPType: "slack", Email: fmt.Sprintf("aaaaa%d", i)})
		snserror.LogAndPanic(err)
	}
	snslog.I(time.Now().Sub(now).Nanoseconds())
}

func TestFind(t *testing.T) {
	var accounts []models.SnsEpAccount
	var account models.SnsEpAccount
	err = models.Query(&accounts, &models.SnsEpmodels.models.d: "1111", EPType: "slack"})
	snslog.I(models.)
	snserror.LogAndPanic(err)
	err = models.QueryByKey(&account, &models.SnsEpmodels.models.d: "1111", EPType: "slack", Email: "vbvvvv"})
	snserror.LogAndPanic(err)
	snslog.I(accounts)
}

func TestUpdate(t *testing.T) {
	//	for i := 0; i < 10000; i++ {
	err = models.InsertOrUpdate(&models.SnsEpmodels.models.d: "1111", EPType: "slack", Email: "dfdfdfdfdf"})
	snserror.LogAndPanic(err)
	//	}

}

// func TestExist(t *testing.T) {
// 	var models.models.SnsEpAccount
// 	ret := models.Exist(&models.SnsEpmodels.models.d: "1111", EPType: "slack", Email: "asdfasdfasdfa"})
// 	if !ret {
// 		panic(nil)
// 	}

// }

func TestDelete(t *testing.T) {
	err = models.DeleteByStruct(&models.SnsEpmodels.models.d: "3333", EPType: "slack"})
	snserror.LogAndPanic(err)
	// models.GetDB().Exec("delete from sns_ep_models. where models.id = ?  and ep_type = ? ", "1111", "slack")
}
