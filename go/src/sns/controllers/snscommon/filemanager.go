package snscommon

import (
	"github.com/astaxie/beego"
	"os"
	"path/filepath"
)

func GetFileUrl(fileName string) string {
	return beego.AppConfig.String("host") + "/file/download/" + fileName
}

func GetFilePath(fileName string) string {
	work, _ := os.Getwd()
	if fileName == "/" || fileName == "" {
		return filepath.Join(work, "files")
	} else {
		return filepath.Join(work, "files", fileName)
	}
}
func Join(elem ...string) string {
	return filepath.Join(elem...)
}
