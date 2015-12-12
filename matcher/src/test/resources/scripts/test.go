package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
)

func main() {
	json_data := `{
    "username" : "user1",
    "email" : "user1@a.com",
    "password" : "user1pass"
  }`
	res, err := http.Post("http://127.0.0.1:8080/signup", "application/json", strings.NewReader(json_data))
	if err != nil {
		log.Fatal(err)
	}
	robots, err := ioutil.ReadAll(res.Body)
	defer res.Body.Close()
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("%s", robots)
}
