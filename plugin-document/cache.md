| 字段             | 含义         | 范围               | 备注           |
|:---------------|:-----------|:-----------------|:-------------|
| kind           | 插件类型       | cache |              |
| condition      | 缓存生效的条件    |                  |              |
| condition.request | 缓存生效的条件-请求 |                  | 可以不填写             |
| condition.request.method | 缓存生效的条件-请求method | GET、POST、PUT、DELETE、OPTIONS、TRACE、HEAD、CONNECT、PATCH | 传入数组 |
| condition.request.host   | 缓存生效的条件-请求域名   |   | 支持正则、精确匹配 |
| condition.request.path   | 缓存生效的条件-请求path   |   | 支持正则、精确匹配 |
| condition.request.headers | 缓存生效的条件-请求headers |  | 数组，单个头也支持正则、精确匹配 |
| condition.response.code | 缓存生效的条件-响应状态码 | 200~599 | 支持正则、精确匹配 |
| condition.response.headers | 缓存生效的条件-响应headers |  | 数组，单个头也支持正则、精确匹配 |
| ttl.local      | 本地缓存失效的时间       |                  |              |    |
| ttl.redis        | redis缓存失效的时间 |                  |  |    |
| keyMaker           | 缓存key的生成策略   |                  |              |    |
```json
{
  "kind": "cache",
  "lowLevelFill": true,
  "condition": {
    "request": {
      "path": {
        "match_type": "exact_match",
        "value": "aa"
      },
      "host": {
        "match_type": "safe_regex_match",
        "value": "www.*baidu\\.com"
      },
      "headers": [
        {
          "headerKey": "hit",
          "match_type": "exact_match",
          "value": "1"
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
        {
          "headerKey": "test",
          "match_type": "exact_match",
          "value": "200"
        }
      ]
    }
  },
  "ttl": {
    "local": {
      "custom": [
        {
          "code": "200",
          "value": "500000"
        }
      ],
      "default": "200000"
    },
    "redis": {
      "custom": [
        {
          "code": "200",
          "value": "500000"
        }
      ],
      "default": "200000"
    }
  },
  "keyMaker": {
    "excludeHost": true,
    "ignoreCase": true,
    "queryString": [
      "a"
    ],
    "headers": [
      "a"
    ]
  }
}
```