package snscommon

import (
	"errors"
	"net/http"
	"strings"
)

func GetBearerFromRequest(request *http.Request) (token string, err error) {
	authValue := request.Header.Get("Authentication")
	token, err = getBearer(authValue)
	return
}
func getBearer(authValue string) (token string, err error) {
	splits := strings.SplitN(authValue, "Bearer ", 2)
	if len(authValue) == 0 || len(splits) < 2 {
		err = errors.New("GetBearerFromRequest token invlaid")
	}
	token = splits[1]
	return
}
