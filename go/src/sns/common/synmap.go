package common

import (
	"sync"
)

type SyncMap struct {
	Map  map[string]interface{}
	Lock sync.RWMutex
}

func NewSyncMap() SyncMap {
	return SyncMap{make(map[string]interface{}), sync.RWMutex{}}
}

func (this SyncMap) Get(key string) (value interface{}, ok bool) {
	this.Lock.RLock()
	value, ok = this.Map[key]
	this.Lock.RUnlock()
	return
}

func (this SyncMap) Set(key string, value interface{}) {
	this.Lock.Lock()
	this.Map[key] = value
	this.Lock.Unlock()
	return
}

func (this SyncMap) GetString(key string) (value string, ok bool) {
	this.Lock.RLock()
	var tmp interface{}
	tmp, ok = this.Map[key]
	if ok {
		value = tmp.(string)
	}
	this.Lock.RUnlock()
	return
}

func (this SyncMap) SetString(key string, value string) {
	this.Lock.Lock()
	this.Map[key] = value
	this.Lock.Unlock()
	return
}
