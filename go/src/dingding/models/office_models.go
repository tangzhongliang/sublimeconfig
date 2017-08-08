package models

import (
	"fmt"

	"github.com/astaxie/beego/orm"
	_ "github.com/go-sql-driver/mysql"
)

func init() {
	orm.RegisterDriver("mysql", orm.DRMySQL)
	orm.RegisterModel(new(OfficeApp), new(OfficeSubcribe))
	//	orm.RegisterDataBase("default", "mysql", "user:user@tcp(47.52.33.241:20110)/office365?charset=utf8", maxIdle, maxConn)
	orm.RegisterDataBase("default", "mysql", "user:iser@tcp(172.25.78.80:3306)/ribot?charset=utf8", maxIdle, maxConn)
	orm.RunSyncdb("default", false, true)
	o = orm.NewOrm()
}

var (
	o orm.Ormer
	// 参数4(可选)  设置最大空闲连接
	// 参数5(可选)  设置最大数据库连接 (go >= 1.2)
	maxIdle = 30
	maxConn = 30
)

const (
	CREATED = iota + 1
	UPDATING
	DELETED
)

type OfficeApp struct {
	Id           int    `orm:"auto;pk;column(id)" json:"id"`
	TokenType    string `orm:"column(token_type)" json:"token_type"`
	Scope        string `orm:"column(scope)" json:"scope"`
	ExpiresIn    int    `orm:"column(expires_in)" json:"expires_in"`
	AccessToken  string `orm:"column(access_token)" json:"access_token"`
	RefreshToken string `orm:"column(refresh_token)" json:"refresh_token"`
	Tenant       string `orm:"column(tenant)" json:"tenant"`
	Domain       string `orm:"unique;column(domain)" json:"domain"`
	Ts           int    `orm:"column(ts)" json:"ts"`
	Status       int    `orm:"column(status)" json:"status"` //office status:created/updating/deleted
}
type OfficeSubcribe struct {
	Id            int    `orm:"auto;pk;column(id)" json:"id"`
	SubcribeId    string `orm:"unique;column(subcribe_id)" json:"subcribe_id"`
	ResourceId    string `orm:"column(resource_id)" json:"resource_id"`
	StartTime     string `orm:"column(start_time)" json:"start_time"`
	StartTimeZone string `orm:"column(start_time_zone)" json:"start_time_zone"`
	EndTime       string `orm:"column(end_time)" json:"end_time"`
	EndTimeZone   string `orm:"column(end_time_zone)" json:"end_time_zone"`
	DeltaLink     string `orm:"column(delta_link)" json:"delta_link"`
	OfficeId      int    `orm:"column(office_id)" json:"office_id"`
}
type User struct {
	Id            string `orm:"pk;column(id)" json:"id"`
	UserId        string `orm:"unique;column(user_id)" json:"user_id"`
	PrincipalName string `orm:"column(principal_name)" json:"principal_name"`
	ResourceType  string `orm:"column(resource_type)" json:"resource_type"`
	OfficeId      int    `orm:"column(office_id)" json:"office_id"`
}

func InsertOrUpdateOffice(office OfficeApp) (errRes error) {
	var last OfficeApp
	err := o.Raw("select * from office_app where domain=?", office.Domain).QueryRow(&last)
	if err == nil {
		o.Delete(&last)
	}
	_, errRes = o.InsertOrUpdate(&office)
	return
}
func GetOfficeTokenList() (list []OfficeApp, err error) {

	_, err = o.Raw("select * from office_app").QueryRows(&list)
	if err == nil {
		panic(err)
	}
	return
}
func GetOfficeTokenByDomain(domain string) (office OfficeApp, err error) {
	err = o.Raw("select * from office_app where domain=?", domain).QueryRow(&office)
	return
}
func GetOfficeToken(id int) (office OfficeApp, err error) {
	err = o.Raw("select * from office_app where id=?", id).QueryRow(&office)
	return
}
func InsertOrUpdateSubcribe(office OfficeSubcribe) (errRes error) {
	var last OfficeSubcribe
	err := o.Raw("select * from office_subcribe where subcribe_id=?", office.SubcribeId).QueryRow(&last)
	if err == nil {
		o.Delete(&last)
	}
	_, errRes = o.InsertOrUpdate(&office)
	return
}
func GetOfficeSubcribeList() (list []OfficeSubcribe, err error) {
	_, err = o.Raw("select * from office_subcribe").QueryRows(&list)
	if err != nil {
		fmt.Errorf("%v", err)
	}
	return
}
func GetOfficeSubcribe(id string) (office OfficeSubcribe, err error) {
	err = o.Raw("select * from office_subcribe where subcribe_id=?", id).QueryRow(&office)
	return
}
