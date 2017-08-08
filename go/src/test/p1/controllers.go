package p1

import (
	"fmt"
)

type Human struct {
	name   string
	age    int
	weight int
}

type Student struct {
	Human      // 匿名字段，那么默认Student就包含了Human的所有字段
	speciality string
}

func Test() {
	mark := Student{Human{"Mark", 25, 120}, "Computer Science"}
	TestImpl(mark.Human)
}
func TestImpl(man Human) {
	fmt.Printf("result%+v", man)
}
