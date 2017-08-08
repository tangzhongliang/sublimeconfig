package cmmodels

import (
	_ "encoding/json"
)

type JobResultStruct struct {
	OK      bool   `json:"ok"`
	Err     Err    `json:"err"`
	Type    string `json:"type"`
	TeamID  string `json:"team_id"`
	Channel string `json:"channel"`
	UserId  string `json:"user_id"`
	Url     string `json:"url"`
}

type Err struct {
	ErrType string `json:"type"`
	ErrMsg  string `json:"message"`
}

var (
	JobResultObj = &JobResultStruct{}
	JobErrResult = &Err{}
)

type ResultResponse struct {
	OK  bool   `json:"ok"`
	Err string `json:"err"`
}
type ResultResponseOK struct {
	OK bool `json:"ok"`
}

func JobResultObjClear() {
	JobResultObj.OK = false
	JobResultObj.Type = ""
	JobResultObj.TeamID = ""
	JobResultObj.Channel = ""
}

func JobErrResultClear() {
	JobErrResult.ErrType = ""
	JobErrResult.ErrMsg = ""
}
