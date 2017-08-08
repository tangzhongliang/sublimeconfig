package main

import (
	"fmt"
	"officeapp/controllers/office"
	"officeapp/models"
)

var (
	token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IjlGWERwYmZNRlQyU3ZRdVhoODQ2WVR3RUlCdyIsImtpZCI6IjlGWERwYmZNRlQyU3ZRdVhoODQ2WVR3RUlCdyJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lZjg0NDNkMS04NzYxLTRjZDctYmZlNy1iMTMwZTY3MDBjNDEvIiwiaWF0IjoxNDk4MjA2OTA5LCJuYmYiOjE0OTgyMDY5MDksImV4cCI6MTQ5ODIxMDgwOSwiYWlvIjoiWTJaZ1lBaTh2YXc0WnMzaHBPQTMrOEpudSszMUF3QT0iLCJhcHBfZGlzcGxheW5hbWUiOiJyaWNvaGFwcCIsImFwcGlkIjoiNWU0M2QxMTctZjBjZC00ODFlLWEwNDctMTQ5ODg1MGY3N2NhIiwiYXBwaWRhY3IiOiIxIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZWY4NDQzZDEtODc2MS00Y2Q3LWJmZTctYjEzMGU2NzAwYzQxLyIsIm9pZCI6ImQ2Nzk2MmQ5LWM0NTYtNDY4Yi1hZWNlLWI0ZWQ1YTllNWU2YyIsInJvbGVzIjpbIlVzZXIuUmVhZFdyaXRlLkFsbCIsIkdyb3VwLlJlYWRXcml0ZS5BbGwiLCJDYWxlbmRhcnMuUmVhZFdyaXRlIl0sInN1YiI6ImQ2Nzk2MmQ5LWM0NTYtNDY4Yi1hZWNlLWI0ZWQ1YTllNWU2YyIsInRpZCI6ImVmODQ0M2QxLTg3NjEtNGNkNy1iZmU3LWIxMzBlNjcwMGM0MSIsInV0aSI6InBpOHJnNk84djBDaWVZWWxxY2NJQUEiLCJ2ZXIiOiIxLjAifQ.Nem0lu2zEZ8bSmFfEAK6zHDcDBdZwqxDpJvMueKg4JzLM176kRpe3508RLq4cbkJSZSZ4H9AXlDYZ93-r_l3494L7-3nKLblVm3-bYmYVYwJKgsGq__xdei8eJ-rvEmi1W95xUKl4luaCfzrnsRmMycDu4By1-lcWzkiPs4BWf7Tyk7AVidDmHPDS_TzId-65V-p01O7AdN25sP4cWjKmgGi6-cOgwVdwY4dktUjFEfhXsdAW473kz6GRFTrgAquVn3YI16DnGVW8y9Ij1L5vrbkIQLREYGA77lO_TQVFKt8cmQIfHEhSs0a8gJlEwQV4ePkqDs4ON_aNNEmbNoRfA"
)

func maintest() {
	fmt.Println("test")
	for i := 0; i < 5; i++ {
		go GetRoomTest()
	}

	wait := make(chan bool)
	<-wait
	fmt.Println("test")
}
func deleteMeetting() {
	fmt.Println("test")

	msgRes, err := office.DeleteMeettingBook(token, "AQMkAGVjNWE4OWEyLWU3NmEtNDk0ZC1hZmUyLTJmM2ZkZThhNWY2ZgBGAAAEicPO7f1UQrUkvnALDO5vBwCy02EmYlSHS4z2C5V1qZhGAAACAQ0AAACy02EmYlSHS4z2C5V1qZhGAAAABITmagAAAA==", "")
	if err != nil {
		fmt.Println(err)
	} else {
		fmt.Printf("%+v\n", msgRes)
	}
}
func GetUserTest() {
	users, err := office.GetUserList(models.OfficeApp{AccessToken: token})
	if err != nil {
		panic(err)
	}
	fmt.Println("users", users)
}
func GetRoomTest() {

	officeapp, err := office.GetOfficeToken("onmicrosoft.com")
	fmt.Printf("office%+v\n", officeapp, err)
	users, err := office.GetRoomList(officeapp)
	if err != nil {
		panic(err)
	}
	fmt.Println("users", users)

}
