package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"io/ioutil"
	"log"
)

type mytype []map[string]string

func main() {
	log.Println("Start...")
	var data mytype
	file, err := ioutil.ReadFile("test.json")
	if err != nil {
		log.Fatal(err)
	}
	err = json.Unmarshal(file, &data)
	if err != nil {
		log.Fatal(err)
	}

	for i := 0; i < len(data); i+=2 {
		req := data[i]
		resp := data[i+1]

		tt,ok := req["request"]
//		request_type, ok := req["request"]
		if !ok {
			log.Println("xfguo: not fonud request type")
			continue
		}
		delete(req, "request")
		req_json, err := json.Marshal(req)
		if err != nil {
			log.Fatal(err)
		}
		log.Printf("xfguo: req = %v, resp = %v", req_json, resp)

		res, err := http.Post("http://127.0.0.1:8080/" + tt, "application/json", bytes.NewReader(req_json))
		if err != nil {
			log.Fatal(err)
		}
		robots, err := ioutil.ReadAll(res.Body)
		defer res.Body.Close()
		if err != nil {
			log.Fatal(err)
		}
		fmt.Printf("%s\n\n", robots)
	}
}
