package models

//EPUserRole:user group team
// related SNSEPAccountEmail one-one
type SnsEpAccount struct {
	BaseModel
	Name         string `gorm:"not null;index"`
	Email        string
	AccountId    string `gorm:"primary_key"` //create id when account is team
	EPType       string
	AccountType  string `gorm:"primary_key"`
	ForeverToken string `gorm:"type:varchar(2000)"` //check is granted account which is granted
	Status       int
}

type EmailAccount struct {
	Email    string `gorm:"primary_key"`
	Password string `gorm:"not null"`
}

type SnsEpAccountEmail struct {
	BaseModel
	AccountId     string
	AccountEPType string `gorm:"primary_key"`
	Email         string `gorm:"primary_key"`
}
