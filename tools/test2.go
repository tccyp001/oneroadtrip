package main

import (
	"bytes"
	"encoding/json"
	"flag"
	"fmt"
	"net/http"
	"io/ioutil"
	"log"
)

type mytype []map[string] interface{}

func main() {
	json_file := flag.String("json_file", "test.json", "json file for reading request")
	host := flag.String("host", "127.0.0.1", "oneroadtrip service host")
	port := flag.Int("port", 8080, "oneroadtrip service port")

	flag.Parse();

	log.Println("Start...")
	var data mytype
	file, err := ioutil.ReadFile(*json_file)
	if err != nil {
		log.Fatal(err)
	}

  d := json.NewDecoder(bytes.NewReader(file))
  d.UseNumber()
	if err := d.Decode(&data); err != nil {
		log.Fatal(err)
	}

	for i := 0; i < len(data); i+=2 {
		req := data[i]
		resp := data[i+1]

		request_type, ok := req["request"]
		if !ok {
			log.Println("xfguo: not fonud request type")
			continue
		}
		delete(req, "request")
		req_json, err := json.Marshal(req)
		if err != nil {
			log.Fatal(err)
		}

		url := fmt.Sprintf("http://%v:%v/api/%v", *host, *port, request_type)
		log.Printf("xfguo: url = %v, req = %v, resp = %v, req_json = %v",
			url, req, resp, string(req_json))
		res, err := http.Post(url, "application/json; charset=utf-8", bytes.NewReader(req_json))
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
