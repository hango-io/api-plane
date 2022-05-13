
| 字段            | 含义    | 范围                  | 备注 |    |
|:--------------|:------|:--------------------|:---|:---|
| kind          | 插件类型  | jsonp |    |    |
| callback | jsonp callback 的querystring的key |        |    |    |

```
场景1：callback querystring key为ddd
{
  "kind": "jsonp",
  "callback":"ddd"
}
```