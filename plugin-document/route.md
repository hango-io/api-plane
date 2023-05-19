| 字段                  | 含义   | 范围                                        | 备注 |    |
|:--------------------|:-----|:------------------------------------------|:---|:---|
| kind                | 插件类型 | ianus-router                              |    |    |
| matcher.source_type | 匹配目标 | 支持：Header、Args、Cookie、User-Agent、URI、Host |    |    |
| matcher.op          | 匹配方式 | 支持：=、!=、≈、!≈、startsWith、endsWith          | !≈与!=慎用，复杂表达式可能匹配不准确   |    |
```
场景1：使用路由插件的rewrite插件，regex和target为新用法
{
  "kind": "ianus-router",
  "rule": [
  {
    "name": "rewrite",
    "matcher": [
    {
      "source_type": "Header",
      "left_value": "plugin",
      "op": "=",
      "right_value": "rewrite"
    }
    ],
    "action": {
      "action_type": "rewrite",
      "rewrite_regex": "/rewrite/(.*)/(.*)",
      "target": "/anything/$2/$1"
    }
  }
  ]
}
```
```
场景2：使用路由插件的rewrite插件，regex和target兼容旧用法
{
  "kind": "ianus-router",
  "rule": [
  {
    "name": "rewrite",
    "matcher": [
    {
      "source_type": "Header",
      "left_value": "plugin",
      "op": "=",
      "right_value": "rewrite"
    }
    ],
    "action": {
      "action_type": "rewrite",
      "rewrite_regex": "/rewrite/{group1}/{group2}",
      "target": "/anything/{{group2}}/{{group1}}"
    }
  }
  ]
}
```
```
场景3：使用路由插件的pass_proxy插件
{
  "kind": "ianus-router",
  "rule": [
  {
    "name": "pass_proxy",
    "matcher": [
    {
      "source_type": "Header",
      "left_value": "plugin",
      "op": "=",
      "right_value": "pass_proxy"
    }
    ],
    "action": {
      "action_type": "pass_proxy",
      "pass_proxy_target": [
      {
        "url": "httpbin.default.svc.cluster.local",
        "weight": 50
      },
      {
        "url": "httpbin2.default.svc.cluster.local",
        "weight": 50
      }
      ]
    }
  }
  ]
}
```
```
场景4：使用路由插件的redirect插件
{
  "kind": "ianus-router",
  "rule": [
  {
    "name": "redirect",
    "matcher": [
    {
      "source_type": "Header",
      "left_value": "plugin",
      "op": "=",
      "right_value": "redirect"
    }
    ],
    "action": {
      "action_type": "redirect",
      "target": "/anything/redirect"
    }
  }
  ]
}
```
```
场景5：使用路由插件的return插件
{
  "kind": "ianus-router",
  "rule": [
  {
    "name": "return",
    "matcher": [
    {
      "source_type": "Header",
      "left_value": "plugin",
      "op": "=",
      "right_value": "return"
    }
    ],
    "action": {
      "action_type": "return",
      "return_target": {
        "code": 403,
        "header": [
        {
          "name":"Content-Type",
          "value": "application/json"
        }
        ],
        "body": "{\"abc\":\"def\"}"
      }
    }
  }
  ]
}
```