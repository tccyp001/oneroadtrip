request="{
  \"token\": \"abc\",
  \"destination\": \"NY\",
  \"adults\": 10,
  \"need_vip\": true,
  \"interests\": [ \"travel\", \"soccer\" ]
}"
curl -i \
    -H "Content-Type: application/json" \
    -X POST \
    -d "$request" \
    http://127.0.0.1:8080/api/travelrequest
