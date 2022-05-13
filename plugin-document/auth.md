| 字段   | 含义   | 范围   | 备注 |    |
|:-----|:-----|:-----|:---|:---|
| kind | 插件类型 | super-auth |    |    |
| authnType | 鉴权方式|jwt_authn_type或aksk_authn_type | | |
| failureAuthAllow| 是否允许匿名访问 | | | |
| userAuthz | 是否开启鉴权 | | | |

```
场景1：启用jwt认证的方式，同时允许匿名访问
{
  "useAuthz": false,
  "kind": "super-auth",
  "authnType": "jwt_authn_type",
  "failureAuthAllow": true
}
```
```
场景2：启用jwt认证的方式，同时开启鉴权并不允许匿名访问
{
  "useAuthz": true,
  "kind": "super-auth",
  "authnType": "jwt_authn_type"
}
```
```
场景3：启用网关鉴权方式，同时允许匿名访问
{
  "useAuthz": false,
  "kind": "super-auth",
  "authnType": "aksk_authn_type",
  "bufferSetting": {
    "maxRequestBytes": "4096",
    "allowPartialMessage": true
  },
  "failureAuthAllow": true
}
```
```
场景4：启用网关鉴权方式，同时开启鉴权并不允许匿名访问
{
  "useAuthz": true,
  "kind": "super-auth",
  "authnType": "aksk_authn_type",
  "bufferSetting": {
    "maxRequestBytes": "4096",
    "allowPartialMessage": true
  }
}
```