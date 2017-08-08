package snsplugin

import (
	"sns/controllers/snsplugin"
	"testing"
)

func TestCreateToken(t *testing.T) {
	token := snsplugin.CreateToken(2)
	t.Log("TestCreateToken", token, len(token))
	token1 := snsplugin.CreateToken(-1)
	t.Log("TestCreateToken", token1, len(token1))
	token2 := snsplugin.CreateToken(2222)
	t.Log("TestCreateToken", token2, len(token2))
}
