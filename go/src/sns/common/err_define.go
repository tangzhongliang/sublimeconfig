package common

import (
	"sns/common/snsstruct"

	"github.com/astaxie/beego/config"
)

type ErrConfig struct {
	ErrCodeConf    config.Configer
	ErrMessageConf config.Configer
}

func NewErrConfig() ErrConfig {
	//	-------------------------init config file
	instance := ErrConfig{}
	errCodeConf, err := config.NewConfig("ini", "conf/err_code.conf")
	if err != nil {
		panic(err)
	}

	errMessageConf, err2 := config.NewConfig("ini", "conf/err_message.conf")
	if err2 != nil {
		panic(err2)
	}
	instance.ErrCodeConf = errCodeConf
	instance.ErrMessageConf = errMessageConf
	return instance
}
func (this ErrConfig) GetError(section, key string) (err snsstruct.ErrDefine) {
	err.ErrCode, _ = this.ErrCodeConf.Int(section + "::" + key)
	err.ErrMessage = this.ErrMessageConf.String(section + "::" + key)
	return
}
