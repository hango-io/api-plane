| 字段             | 含义         | 范围               | 备注           |
|:---------------|:-----------|:-----------------|:-------------|
| kind           | 插件类型       | static-downgrade |              |
| condition      | 降级生效的条件    |                  |              |
| condition.request | 降级生效的条件-请求 |                  | 可以不填写             |
| condition.request.method | 降级生效的条件-请求method | GET、POST、PUT、DELETE、OPTIONS、TRACE、HEAD、CONNECT、PATCH | 传入数组 |
| condition.request.host   | 降级生效的条件-请求域名   |   | 支持正则、精确匹配 |
| condition.request.path   | 降级生效的条件-请求path   |   | 支持正则、精确匹配 |
| condition.request.headers | 降级生效的条件-请求headers |  | 数组，单个头也支持正则、精确匹配 |
| condition.response.code | 降级生效的条件-响应状态码 | 200~599 | 支持正则、精确匹配 |
| condition.response.headers | 降级生效的条件-响应headers |  | 数组，单个头也支持正则、精确匹配 |
| response       | 降级返回       |                  |              |    |
| header         | 降级返回header |                  | 存在则覆盖，不存在则增加 |    |
| body           | 降级返回body   |                  |              |    |
```
{
  "condition": {
    "request": {
      "requestSwitch": true,
      "path": {
        "match_type": "safe_regex_match",
        "value": "/anything/anythin."
      },
      "host": {
        "match_type": "safe_regex_match",
        "value": "103.196.65.17."
      },
      "headers": [
        {
          "headerKey": "key",
          "match_type": "exact_match",
          "value": "va"
        }
      ],
      "method": [
        "GET"
      ]
    },
    "response": {
      "code": {
        "match_type": "exact_match",
        "value": "200"
      },
      "headers": [
        
      ]
    }
  },
  "kind": "static-downgrade",
  "response": {
    "code": "200",
    "body": "{\"ba\":\"ba\"}"
  }
}

```
转换后crd
```
"com.netease.staticdowngrade": {
 "downgrade_rpx": {
  "headers": [
   {
    "regex_match": "200|",
    "name": ":status"
   }
  ]
 },
 "static_response": {
  "body": {
   "inline_string": "{\"ba\":\"ba\"}"
  },
  "http_status": 200
 },
 "downgrade_rqx": {
  "headers": [
   {
    "name": "key",
    "exact_match": "va"
   },
   {
    "name": ":authority",
    "regex_match": "103.196.65.17."
   },
   {
    "exact_match": "GET",
    "name": ":method"
   },
   {
    "regex_match": "/anything/anythin.",
    "name": ":path"
   }
  ]
 }
}

```

- 只有当condition满足降级条件后才能降级，且其中condition.response.code必填
- response降级的返回，code会返回状态码，header为返回附带的header,body既返回的body