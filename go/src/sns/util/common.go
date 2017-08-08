package util

import (
	"encoding/base64"
	"math"
	"math/rand"
	"strconv"
	"time"
)

func CreateRandomString(len int) string {
	var str string
	if len < 0 {
		return str
	}
	if len > 512 {
		len = 512
	}
	now := time.Now()
	second := now.UnixNano()
	randNum := rand.New(rand.NewSource(second))
	for i := 0; i < 512; i++ {
		str += strconv.Itoa(randNum.Intn(math.MaxInt32))
	}
	// h := md5.New() h.Write([]byte(str)
	sha := base64.URLEncoding.EncodeToString([]byte(now.String() + str))
	return sha[:len]
}
