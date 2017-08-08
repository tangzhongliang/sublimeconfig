package cmutil

import (
	"bufio"
	"io"
	"os"
	"path/filepath"
	"strings"
)

/*
[default]
path= c:/go
version = 1.44

[test]
num =	666
something  = wrong  #注释1
#fdfdfd = fdfdfd    注释整行
refer= refer       //注释3
*/

/*
myConfig := new(cf.Config)
	myConfig.InitConfig("c:/config.txt")
	fmt.Println(myConfig.Read("default", "path"))
	fmt.Printf("%v", myConfig.Mymap)
*/
const middle = "========="

type Config struct {
	Mymap  map[string]string
	strcet string
}

func DefaultConfig() *Config {
	basicConfig := new(Config)
	ex, err := os.Getwd()
	if err != nil {
		panic(err)
	}
	temp := ex + filepath.FromSlash("/../common/conf.txt")
	basicConfig.InitConfig(temp)
	return basicConfig
}
func (c *Config) InitConfig(path string) {
	c.Mymap = make(map[string]string)

	f, err := os.Open(path)
	if err != nil {
		panic(err)
	}
	defer f.Close()

	r := bufio.NewReader(f)
	for {
		b, _, err := r.ReadLine()
		if err != nil {
			if err == io.EOF {
				break
			}
			panic(err)
		}

		s := strings.TrimSpace(string(b))
		//fmt.Println(s)
		if strings.Index(s, "#") == 0 {
			continue
		}

		n1 := strings.Index(s, "[")
		n2 := strings.LastIndex(s, "]")
		if n1 > -1 && n2 > -1 && n2 > n1+1 {
			c.strcet = strings.TrimSpace(s[n1+1 : n2])
			continue
		}

		if len(c.strcet) == 0 {
			continue
		}
		index := strings.Index(s, "=")
		if index < 0 {
			continue
		}

		frist := strings.TrimSpace(s[:index])
		if len(frist) == 0 {
			continue
		}
		second := strings.TrimSpace(s[index+1:])

		pos := strings.Index(second, "\t#")
		if pos > -1 {
			second = second[0:pos]
		}

		pos = strings.Index(second, " #")
		if pos > -1 {
			second = second[0:pos]
		}

		pos = strings.Index(second, "\t//")
		if pos > -1 {
			second = second[0:pos]
		}

		pos = strings.Index(second, " //")
		if pos > -1 {
			second = second[0:pos]
		}

		if len(second) == 0 {
			continue
		}

		key := c.strcet + middle + frist
		c.Mymap[key] = strings.TrimSpace(second)
	}
}

func (c *Config) ReadByMode(node, key string) string {
	key = node + middle + key
	v, found := c.Mymap[key]
	if !found {
		LogE(node, key, " not found")
		if node == "default" {
			return ""
		}
		ret := c.ReadByMode("default", key)

		return ret
	}
	return strings.Replace(v, "\\n", "\n", -1)
}
func (c *Config) Read(key string) string {
	mode := c.ReadByMode("default", "runmode")
	LogI(mode)
	ret := c.ReadByMode(mode, key)
	if strings.EqualFold(ret, "") {
		return c.ReadByMode("default", key)
	} else {
		return ret
	}
}
