package office

import (
	"errors"
	"fmt"
	"officeapp/models"
	"sync"
)

var (
	UpdateAllTokenTaskLock    = sync.RWMutex{}
	UpdateAllTokenTaskRunning = false
)

func StartUpdateAllTokenTask() {
	if UpdateAllTokenTaskRunning {
		panic("err")
	}

	UpdateAllTokenTaskRunning = true
	go func() {

		//a map key:token domain,value:list req chan
		result := make(chan models.OfficeApp, 1000)
		req_list := make(map[string][](chan models.OfficeApp))
		for {
			select {
			case office := <-result:
				//dispatch token result
				fmt.Println("result", office)
				if value, ok := req_list[office.Domain]; ok {
					for _, req_chan := range value {
						req_chan <- office
					}
				}
				delete(req_list, office.Domain)
			case req := <-TokenHandleChan:
				// do stuff
				value, ok := req_list[req.office.Domain]
				if ok {
					//subcribe my req chan
					value = append(value, req.Req)
					req_list[req.office.Domain] = value
				} else {
					var list [](chan models.OfficeApp)
					//subcribe my req chan
					req_list[req.office.Domain] = append(list, req.Req)
					go func() {
						//get token
						//todo serizable by domain
						office, err := getTokenByTenant(req.office.Tenant)
						if err != nil {
							// app tenant invalid when app remove or add again
							//remove all subcribe
							fmt.Errorf("aaaaa%v", err)

							office = req.office
							office.Status = models.DELETED
						}
						fmt.Println(office)
						models.InsertOrUpdateOffice(office)
						result <- office
					}()
				}
			case <-quit:
				return
			}
		}
	}()
	fmt.Println("start token task")
}
func StopFileServer() {
	quit <- true
}
func TokenIsValide(office models.OfficeApp) bool {
	return false
}

var (
	TokenHandleChan = make(chan *TokenUpdateRequest)
)

type TokenUpdateRequest struct {
	Req    chan models.OfficeApp
	office models.OfficeApp
}

/*
get office token
token is avaible for 5 min at least
this is maybe slow if toke need to be updated
*/
func GetOfficeToken(domain string) (office models.OfficeApp, err error) {
	office, err = models.GetOfficeTokenByDomain(domain)
	if err != nil {

	}
	fmt.Println(office)
	if !TokenIsValide(office) {
		//send update request which contains token'info,result chan
		req := &TokenUpdateRequest{make(chan models.OfficeApp), office}
		TokenHandleChan <- req
		//wait result
		office = <-req.Req
		if office.Status == models.DELETED {
			//fail to get token because
			err = errors.New("account not avaibale")
		}
	}
	office.AccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IjlGWERwYmZNRlQyU3ZRdVhoODQ2WVR3RUlCdyIsImtpZCI6IjlGWERwYmZNRlQyU3ZRdVhoODQ2WVR3RUlCdyJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lZjg0NDNkMS04NzYxLTRjZDctYmZlNy1iMTMwZTY3MDBjNDEvIiwiaWF0IjoxNDk4NDc2MTc2LCJuYmYiOjE0OTg0NzYxNzYsImV4cCI6MTQ5ODQ4MDA3NiwiYWlvIjoiWTJaZ1lFai9hT2piKzhGUHVuZmJuaWVzMjFlZUFBQT0iLCJhcHBfZGlzcGxheW5hbWUiOiJyaWNvaGFwcCIsImFwcGlkIjoiNWU0M2QxMTctZjBjZC00ODFlLWEwNDctMTQ5ODg1MGY3N2NhIiwiYXBwaWRhY3IiOiIxIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZWY4NDQzZDEtODc2MS00Y2Q3LWJmZTctYjEzMGU2NzAwYzQxLyIsIm9pZCI6ImQ2Nzk2MmQ5LWM0NTYtNDY4Yi1hZWNlLWI0ZWQ1YTllNWU2YyIsInJvbGVzIjpbIlVzZXIuUmVhZFdyaXRlLkFsbCIsIkdyb3VwLlJlYWRXcml0ZS5BbGwiLCJDYWxlbmRhcnMuUmVhZFdyaXRlIl0sInN1YiI6ImQ2Nzk2MmQ5LWM0NTYtNDY4Yi1hZWNlLWI0ZWQ1YTllNWU2YyIsInRpZCI6ImVmODQ0M2QxLTg3NjEtNGNkNy1iZmU3LWIxMzBlNjcwMGM0MSIsInV0aSI6IlcxM0tJeW94Y2tXMTl6dWVHRjRvQUEiLCJ2ZXIiOiIxLjAifQ.BPciGr-l0jfAQIOT9AKljQ9Z0PE_rk_FUVlK5HcbicK5-KhWBMSs-XBbaUpP3IIqsSUAEPgjCT0Mr5oBz6VbMbNfuJtNjYF6RGT3UaHzIBsgFPXxEmuXn0tOeLb3H1PeZEuo4FoCMMIb5YmCcCT4VelAY4soB5qcupA-Kry0am72LTw7S2u2vSyOXeyKNklOmn27LQRRPOPm_JAjqlBxKVbAG_zoIW85PPnPl37yj6dG70uF_gZSjbZ8U3T-iiRNZT-g519yMzftC_F511OpoXkx8db_6HqtMdkOyOwiyBCaHiVfTecHkF8eBkY8xx-S-1k4GADZYbpw3APiBahI4w"
	err = nil
	return
}
