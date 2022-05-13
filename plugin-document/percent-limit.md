
| 字段            | 含义    | 范围                  | 备注 |    |
|:--------------|:------|:--------------------|:---|:---|
| kind          | 插件类型  | ianus-percent-limit |    |    |
| limit_percent | 限流百分比 | 0-100               |    |    |

```
场景1：限流一半请求
{
  "kind": "ianus-percent-limit",
  "limit_percent": 50
}
```
