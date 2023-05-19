| 字段     | 含义              | 范围                                                       | 备注 |
|:-------|:----------------|:---------------------------------------------------------|:---|
| kind   | 插件类型            | transformer                                              |    |
|        | conditions      | 插件生效条件                                                   |    |
| op     | conditions的匹配方法 | 支持：regex、prefix、exact                                    |    |
|        | headers         | 对headers进行转换                                             |    |
|        | querystrings    | 对querystring进行转换                                         |    |
| action | 转换方式            | 支持：Action_Default、Action_Add、Action_Update、Action_Delete |    |

```
场景1：提取headers中的abc,def，并加入到新header:addHeaders
插件:
{
  "kind":"transformer",
  "headers": [
  {
    "key": "addHeaders",
    "text": "abc:{{headers[abc]}},def:{{headers[def]}}",
    "action": "Action_Default"
  }
  ]
}
```
```
场景2: 提取querystring中的abc,def,并加入到新querystring:addQuerystrings
插件:
{
  "kind":"transformer",
  "querystrings": [
  {
    "key": "addQuerystrings",
    "text": "abc:{{querystrings[abc]}},def:{{querystrings[def]}}",
    "action": "Action_Default"
  }
  ]
}
```
```
场景3：提取url中的路径参数，并加入到新header：addHeaders
插件：
{
  "kind":"transformer",
  "headers": [
  {
    "key": "addHeaders",
    "text": "group1:{{url[0]}},group2:{{url[1]}}",
    "action": "Action_Default"
  }
  ]
}
```
```
场景4：不提取参数，添加header,querystring，action=Action_Default
插件：
{
  "kind":"transformer",
  "headers": [
  {
    "key": "addHeaders",
    "text": "addHeaders",
    "action": "Action_Default"
  }
  ],
  "querystrings": [
  {
    "key": "addQuerystrings",
    "text": "addQuerystrings",
    "action": "Action_Default"
  }
  ]
}
```

```
场景5：不提取参数，添加header,querystring，action=Action_Add
插件：
{
  "kind":"transformer",
  "headers": [
  {
    "key": "addHeaders",
    "text": "addHeaders",
    "action": "Action_Add"
  }
  ],
  "querystrings": [
  {
    "key": "addQuerystrings",
    "text": "addQuerystrings",
    "action": "Action_Add"
  }
  ]
}
```
```
场景6：不提取参数，添加header,querystring，action=Action_Update
插件：
{
  "kind":"transformer",
  "headers": [
  {
    "key": "updateHeaders",
    "text": "updateHeaders",
    "action": "Action_Update"
  }
  ],
  "querystrings": [
  {
    "key": "updateQuerystrings",
    "text": "updateQuerystrings",
    "action": "Action_Update"
  }
  ]
}
```
```
场景7：不提取参数，添加header,querystring，action=Action_Delete
对应用例：testTransform_DeleteAction
插件：
{
  "kind":"transformer",
  "headers": [
  {
    "key": "deleteHeaders",
    "action": "Action_Delete"
  }
  ]
}
```
```
场景8：不提取参数，添加header,querystring，action=Action_Delete
插件：
{
  "kind":"transformer",
  "headers": [
  {
    "key": "deleteHeaders",
    "action": "Action_Delete"
  }
  ]
}
```
```
场景9：转换url（注意：包含query部分）
说明：转换url只需填写text,key不填写，action不填写（默认为Update）
插件：
{
  "kind":"transformer",
  "url":[
  {
    "text":"/anything/{{headers[abc]}}"
  }
  ]
}
```
```
场景10：转换path（不包含query部分，原样保留）
说明：转换path只需填写text,key不填写，action不填写（默认为Update）
插件：
{
  "kind":"transformer",
  "path":[
  {
    "text":"/anything/{{path[0]}}"
  }
  ]
}
```
```
场景11：配置插件condition，匹配成功才执行转换
{
  "kind":"ianus-request-transformer",
  "conditions":{
    "headers":[
    {
      "key":"condition1",
      "text":"aaa",
      "op":"prefix"
    }
    ],
    "querystrings":[
    {
      "key":"condition1",
      "text":".*",
      "op":"regex"
    }
    ]
  },
  "headers": [
  {
    "key": "addHeaders",
    "text": "addHeaders",
    "action": "Action_Default"
  }
  ],
  "querystrings": [
  {
    "key": "addQuerystrings",
    "text": "addQuerystrings",
    "action": "Action_Default"
  }
  ]
}
```

- conditions支持headers与querystrings两种匹配字段，匹配op支持regex、prefix、exact
- conditions下的条件均为与的关系
- 插件支持4种变量提取方法：分别是
    1. headers[$header] 提取headers的value
    2. querystrings[$query] 提取querystring的value
    3. url[0]、url[1] 仅当配置api的路径为例如: /api/(.*)/(.*) 即带正则匹配group可以使用
    4. path[0]、path[1] 提取路径参数，与url[0]、url[1]的区别是不会提取query部分
- Action_Default处理方式为如果匹配目标存在则覆盖，不存在则增加
- Action_Add处理方式为如果目标存在则不处理，不存在则增加
- Action_Update处理方式为如果目标存在则更新，不存在则不处理
- Action_Delete处理方式为删除，不存在则不处理