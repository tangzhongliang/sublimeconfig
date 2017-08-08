package web

import (
	"sns/controllers/web"
	"testing"
)

func TestErrConfig(t *testing.T) {
	name := web.GetLastString("http://rocket.hezhensh.com:12080/webhook/ep/login/ep_line?code=C6XAstRxgk3OhHFeEj6Z&state=tangzhongliang%40rst.ricoh.com")
	t.Log(name)
}
