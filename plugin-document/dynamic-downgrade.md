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
| cache.condition       | 将缓存结果进行缓存的条件       |                  |              |    |
| cache.ttl        | 环境结果的失效时长 |                  |  |    |
| cache.cache_key           | 缓存key的生成策略   |                  |              |    |
| httpx | 接口降级 | | | |
| httpx.uri | 降级到uri | | | |
| httpx.remote | 降级到另一个后端cluster | | ||
缓存降级：
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
			"headers": [{
				"headerKey": "key",
				"match_type": "exact_match",
				"value": "va"
			}],
			"method": [
				"GET"
			]
		},
		"response": {
			"code": {
				"match_type": "exact_match",
				"value": "200"
			},
			"headers": []
		}
	},
	"kind": "dynamic-downgrade",
	"cache": {
		"condition": {
			"response": {
				"code": {
					"match_type": "safe_regex_match",
					"value": "2.."
				},
				"headers": [{
					"headerKey": "x-can-downgrade",
					"match_type": "exact_match",
					"value": "true"
				}]
			}
		},
		"ttl": {
			"default": 30000,
			"custom": [{
				"code": "200",
				"ttl": 50000
			}]
		},
		"cache_key": {
			"query_params": ["id"],
			"headers": ["comefrom"]
		}
	}
}

```
转换后crd
```
{
	"com.netease.dynamicdowngrade": {
		"downgrade_rpx": {
			"headers": [{
				"regex_match": "200|",
				"name": ":status"
			}]
		},
		"downgrade_rqx": {
			"headers": [{
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
		},
		"cache_rpx_rpx": {
			"headers": [{
					"name": ":status",
					"regex_match": "2.."
				},
				{
					"name": "x-can-downgrade",
					"exact_match": true
				}
			]
		},
		"cache_ttls": {
			"RedisHttpCache": {
				"default": 30000,
				"customs": {
					"200": 50000
				}
			}
		},
		"key_maker": {
			"query_params": [
				"id"
			],
			"headers_keys": [
				"comefrom"
			]
		}
	}
}
```
接口降级
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
      "headers": []
    }
  },
  "kind": "dynamic-downgrade",
  "httpx":{
    "uri":"http://httpbin.org/anything"
  }
}
```