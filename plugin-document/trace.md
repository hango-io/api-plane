| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| app_name (required) | gateway的app name | string, 满足正则 [a-zA-Z-_\d]+ , 长度不超过 128 byte |    |    |
| cluster_name (required)| 网关集群的 cluster name | string, 满足正则 [a-zA-Z-_\d]+ , 长度不超过 128 byte | | |
| sample_rate (optional) | 当网关为第一跳时，对 200 请求按比例采样 | 浮点数类型, 取值范围 [0, 1.0] 默认为 0.1 | | |
```json
{
	"kind": "trace",
	"config": {
		"app_name": "gateway",
		"cluster_name": "gateway-cluster",
		"sample_rate": "0.5"
	}
}
```