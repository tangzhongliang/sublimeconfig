package models

import (
	// "github.com/jinzhu/gorm"
	// "reflect"
	"time"
)

type BaseModel struct {
	ID        uint `gorm:"AUTO_INCREMENT"`
	CreatedAt time.Time
	UpdatedAt time.Time
	DeletedAt *time.Time `sql:"index"`
}
type BaseModelWithId struct {
	BaseModel
	ID uint `gorm:"primary_key"`
}

func InsertOrUpdate(v interface{}) (err error) {
	err = GetDB().Create(v).Error
	if err != nil {
		err = nil
		err = GetDB().Model(v).Where(GetPrimaryKey(v)).Update(v).Error
	}
	return
}
func Insert(v interface{}) (err error) {
	err = GetDB().Create(v).Error
	return
}
func Update(v interface{}) (err error) {
	err = GetDB().Model(v).Where(GetPrimaryKey(v)).Update(v).Error
	return
}
func All(v interface{}) (err error) {
	err = GetDB().Find(v).Error
	return
}
func DeleteByStruct(user interface{}) (err error) {
	keys := GetPrimaryKey(user)
	query := "delete from " + GetTableName(user) + " where" + map2query(keys)
	err = GetDB().Exec(query).Error
	return
}
func Delete(user interface{}, query string, args ...interface{}) (err error) {
	query = "delete from " + GetTableName(user) + " where" + query
	err = GetDB().Exec("delete from "+GetTableName(user)+" where"+query, args).Error
	return
}
func Query(users interface{}, query interface{}, args ...interface{}) (err error) {
	err = GetDB().Where(query, args).Find(users).Error
	return
}

func QueryByKey(out interface{}, query interface{}, args ...interface{}) (err error) {
	keys := GetPrimaryKey(query)
	if len(keys) == 0 {
		err = GetDB().First(out, query, args).Error
	} else {
		err = GetDB().Where(keys).First(out).Error
	}
	return
}
func GetTableName(v interface{}) string {
	scope := GetDB().NewScope(v)
	name := scope.TableName()
	return name
}
func GetPrimaryKey(v interface{}) map[string]interface{} {
	keys := make(map[string]interface{})
	scope := GetDB().NewScope(v)
	for _, value := range scope.Fields() {
		if value.StructField.IsPrimaryKey {
			keys[value.StructField.DBName] = value.Field.Interface()
		}
	}
	return keys
}
func map2query(m map[string]interface{}) (query string) {
	for key, value := range m {
		query += " " + key + " = \"" + value.(string) + "\" and"
	}
	if len(query) > 0 {
		query = query[:len(query)-4]
	}
	return
}

// func (this BaseModel) Exist(v interface{}) bool {
// 	var *value = *v
// 	err2 := GetDB().Where(v).Rows().Next()
// 	if err2 != nil {
// 		return false
// 	}
// 	return true
// }
