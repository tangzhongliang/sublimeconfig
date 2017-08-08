package snsfactory

import (
	"reflect"
	"sns/util/snslog"
)

type RegisterStructMaps map[string]reflect.Type

//根据name初始化结构
//在这里根据结构的成员注解进行DI注入，这里没有实现，只是简单都初始化
func (rsm RegisterStructMaps) New(name string) (c interface{}) {
	if v, ok := rsm[name]; ok {
		c = reflect.New(v).Interface()
	} else {
		err := snslog.Errorf("not found %s struct", name)
		panic(err)
	}
	return
}

//根据名字注册实例
func (rsm RegisterStructMaps) Register(name string, c interface{}) {
	rsm[name] = reflect.TypeOf(c).Elem()
}

type Test struct {
	value string
}

func (test *Test) SetValue(value string) {
	test.value = value
}
func (test *Test) Print() {
	snslog.I(test.value)
}
func Test2() {
	rsm := RegisterStructMaps{}
	//注册test
	rsm.Register("test", &Test{})
	//获取新的test的interface
	test11 := rsm.New("test")
	test22 := rsm.New("test")
	//因为 test11 和 test22都是interface{},必须转换为*Test
	test1 := test11.(*Test)
	test2 := test22.(*Test)
	test1.SetValue("aaa")
	test2.SetValue("bbb")
	test1.Print()
	test2.Print()
}
