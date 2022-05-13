| 字段   | 含义   | 范围   | 备注 |    |
|:-----|:-----|:-----|:---|:---|
| kind | 插件类型 | cors |    |    |
| allowOrigin | 更够共享的域 |必填，可以是具体的域名或者*表示所有域 |
| allowMethods | 允许请求的方法 | 支持GET/POST/PUT/PATCH/DELETE/OPTIONS/HEAD |
| allowHeaders | 可以使用的请求头 | 个数不限，长度限制200 |
| maxAge | 预请求的结果能被缓存多久 | 单位ms |
| exposeHeaders | 哪些http的响应头能够在响应列出 | |
| allowCredentials | 当请求的凭证标记为true时，是否响应请求 | |
```
{
  "kind": "cors",
  "corsPolicy": {
    "allowOrigin": ["www.baidu.com", "google.com"],
    "allowOriginRegex": ["a.*","b.*"],
    "allowMethods": ["get","post"],
    "allowHeaders": [":authority",":method"],
    "exposeHeaders": ["host","user-agent"],
    "maxAge": "30s",
    "allowCredentials": true
  }
}
```