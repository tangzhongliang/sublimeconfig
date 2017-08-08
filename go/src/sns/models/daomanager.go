package models

import (
	"time"

	"database/sql"

	"sns/util/snserror"

	"sns/util/snslog"

	"fmt"
	"github.com/astaxie/beego"
	_ "github.com/go-sql-driver/mysql"
	"github.com/jinzhu/gorm"
	"sync"
)

type User struct {
	gorm.Model
	Birthday time.Time
	Age      int
	Name     string `gorm:"size:255"` // Default size for string is 255, reset it with this tag
	Num      int    `gorm:"AUTO_INCREMENT"`

	CreditCard CreditCard // One-To-One relationship (has one - use CreditCard's UserID as foreign key)
	Emails     []Email    `gorm:"one2many:email;"` // One-To-Many relationship (has many - use Email's UserID as foreign key)

	BillingAddress   Address // One-To-One relationship (belongs to - use BillingAddressID as foreign key)
	BillingAddressID sql.NullInt64

	ShippingAddress   Address // One-To-One relationship (belongs to - use ShippingAddressID as foreign key)
	ShippingAddressID int

	IgnoreMe  int        `gorm:"-"`                        // Ignore this field
	Languages []Language `gorm:"many2many:user_language;"` // Many-To-Many relationship, 'user_languages' is join table
}
type Email struct {
	ID         int
	UserID     int    `gorm:"index";`                         // Foreign key (belongs to), tag `index` will create index for this column
	Email      string `gorm:"type:varchar(100);unique_index"` // `type` set sql type, `unique_index` will create unique index for this column
	Subscribed bool
}

func (Email) TableName() string {
	return "email"
}

type Address struct {
	ID       int
	Address1 string `gorm:"not null;unique"` // Set field as not nullable and unique
	Address2 string `gorm:"type:varchar(100);unique"`
}

type Language struct {
	ID   int
	Name string `gorm:"index:idx_name_code"` // Create index with name, and will create combined index if find other fields defined same name
	Code string `gorm:"index:idx_name_code"` // `unique_index` also works
}

type CreditCard struct {
	gorm.Model
	UserID uint
	Number string
}

var (
	DB   *gorm.DB
	once sync.Once
)

func init() {
	var err error
	dbuser := beego.AppConfig.String("dbuser")
	dbpassword := beego.AppConfig.String("dbpassword")
	dbip := beego.AppConfig.String("dbip")
	dbport := beego.AppConfig.String("dbport")
	dbname := beego.AppConfig.String("dbname")
	charset := beego.AppConfig.String("charset")
	constr := fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?charset=%s", dbuser, dbpassword, dbip, dbport, dbname, charset)
	DB, err = gorm.Open("mysql", "root:root@/sns?charset=utf8&parseTime=True&loc=Local")
	// DB, err = gorm.Open("mysql", constr+"&parseTime=True&loc=Local")
	fmt.Println(constr)
	snserror.LogAndPanic(err)
	DB.LogMode(true)
	var tables = []interface{}{&SnsEpAccount{},
		&SnsEpAccountEmail{},
		&SnsPluginAccount{},
		&SnsPlugin{},
		&SnsPluginConfig{},
		&SnsPluginEpAccount{},
		&EmailAccount{},
		&PluginSendMessageState{}}
	for _, value := range tables {
		// DB.DropTable(value)
		if !DB.HasTable(value) {
			DB.CreateTable(value)
		}
	}
}
func GetDB() *gorm.DB {
	return DB
}
func TestGorm() {
	//===================================
	//just test
	var err error
	err = GetDB().Create(&SnsEpAccount{AccountId: "aaaa", AccountType: "slack"}).Error
	snserror.LogAndPanic(err)
	var accounts SnsEpAccount
	err = GetDB().Model(&SnsEpAccount{AccountId: "aaaa2", AccountType: "slack2", Email: "sadfsdf"}).Find(&accounts).Error
	// err = db.Update(&SnsEpAccount{AccountId: "aaaa", EPType: "slack"}).Error
	snserror.LogAndPanic(err)
	snslog.I(accounts)
}
