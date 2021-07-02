| 字段             | 含义         | 范围               | 备注           |
|:---------------|:-----------|:-----------------|:-------------|
| kind           | 插件类型       | local-limiting |              |
| headers      | 限流headers配置    |                  |              |
| headers.headerKey | 限流匹配header |                  |              |
| headers.match_type | 限流header match type | 精确匹配，正则匹配 |  |
| headers.value   | 限流header 匹配的value   |   |  |
| day   | 限流时间窗口，天 |   |  |
| hour | 限流时间窗口，小时 |  |  |
| minute | 限流时间窗口，分钟 |  |  |
| second | 限流时间窗口，秒 |  |  |
```
{
    "limit_by_list":[
        {
            "headers":[
                {
                    "headerKey":"header1",
                    "match_type":"exact_match",
                    "value":"header"
                }
            ],
            "second":2
        }
    ],
    "kind":"local-limiting",
    "name":"local-limiting"
}

```
