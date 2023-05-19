| 字段            | 含义    | 范围                  | 备注 |    |
|:--------------|:------|:--------------------|:---|:---|
| kind          | 插件类型  | rewrite |    |    |
| action.rewrite_regex | 提取group | | | |
| action.target | 重写目标路径 | | |
| conditions | 插件匹配条件 | | |
| conditions.headers | headers匹配条件 | | |
| conditions.querystrings | querystrings匹配条件 | | |
| conditions.url | url匹配条件 | | |
| conditions.host | host匹配条件 | | |
| conditions.method | method匹配条件 | | |
| conditions.op | 匹配方式 | 支持exact、prefix、regex | |
```
场景1：重写path
{
  "kind": "rewrite",
  "action": {
    "rewrite_regex": "/rewrite(.*)",
    "target": "/anything$1"
  }
}
```
```
场景2：配置condition
{
  "kind": "rewrite",
  "conditions": {
    "headers": [
      {
        "key": "condition1",
        "text": "aaa",
        "op": "prefix"
      }
    ],
    "querystrings": [
      {
        "key": "condition1",
        "text": ".*",
        "op": "regex"
      }
    ],
    "url": [
      {
        "key": "condition1",
        "text": "abc",
        "op": "exact"
      }
    ],
    "host": [
      {
        "key": "condition1",
        "text": ".*",
        "op": "regex"
      }
    ],
    "method": [
      {
        "key": "condition1",
        "text": ".*",
        "op": "regex"
      }
    ]
  },
  "action": {
    "rewrite_regex": "/rewrite(.*)",
    "target": "/anything$1"
  }
}
```