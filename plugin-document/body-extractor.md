| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| kind | 插件类型 | body-extractor |    |    |
| src.from | 提取来源 | 支持：HEADER、QUERY、BODY | | |
| des.to | 注入变量 | 支持：HEADER、QUERY、BODY | | |
```
场景1：提取Header
{
  "kind": "body-extractor",
  "extractors": [
  {
    "src": {
      "name":"from",
      "from":"HEADER"
    },
    "des": {
      "rename":"to",
      "to":"QUERY"
    }
  }
  ]
}
```
```
场景2：提取Querystring
{
  "kind": "body-extractor",
  "extractors": [
  {
    "src": {
      "name":"from",
      "from":"QUERY"
    },
    "des": {
      "rename":"to",
      "to":"HEADER"
    }
  }
  ]
}
```
```
场景3：提取Body参数
{
  "kind": "body-extractor",
  "extractors": [
  {
    "src": {
      "name":"from",
      "from":"BODY"
    },
    "des": {
      "rename":"to",
      "to":"BODY"
    }
  }
  ]
}
```