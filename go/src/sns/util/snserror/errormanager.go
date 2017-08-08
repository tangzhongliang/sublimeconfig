package snserror

import (
	"sns/util/snslog"
)

func LogAndPanic(err error) bool {
	if err != nil {
		snslog.E(err)
		panic(err)
		return true
	}
	return false
}
