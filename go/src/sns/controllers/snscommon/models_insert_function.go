package snscommon

func ExecUntilSuccess(f func() (interface{}, bool)) (res interface{}) {
	for true {
		var ok bool
		res, ok = f()
		if ok {
			break
		}
	}
	return
}
