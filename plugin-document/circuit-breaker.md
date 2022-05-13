| 字段             | 含义         | 范围               | 备注           |
|:---------------|:-----------|:-----------------|:-------------|
| kind           | 插件类型       | circuit-breaker |              |
| response       | 熔断返回       |                  |              |    |
| response.code       | 熔断返回码       |   大于200且小于600               |              |    |
| response.body       | 熔断返回body      |                 |              |    |
| response.headers       | 熔断返回header       |               |              |    |
| config       | 熔断配置       |                  |              |    |
| config.consecutive_slow_requests       | 连续"慢"请求       |                  |              |    |
| config.average_response_time       | 平均返回时间,当统计时间内平均 RT 大于该值时触发熔断       |                  |              |    |
| config.min_request_amount       | 最小请求数，和 error_percent_threshold 共同作用。当 10s 内的请求数小于 min_request_amount 时不会触发熔断       |                  |              |    |
| config.error_percent_threshold       |   错误率阈值，当 5xx 错误数 / 请求总数大于该值时触发熔断       |                  |              |    |
| config.break_duration       | 熔断时间       |                  |              |    |
| config.lookback_duration       | 统计时间，默认值为10s       |  小于120s                |              |    |

```
{
  "kind": "circuit-breaker",
  "config": {
    "consecutive_slow_requests": "3",
    "average_response_time": "0.1",
    "min_request_amount": "3",
    "error_percent_threshold": "50",
    "break_duration": "50",
    "lookback_duration": "10"
  },
  "response": {
    "code": "200",
    "body": "{\"ba\":\"ba\"}",
    "headers": [
      {
        "key": "buhao",
        "value": "buhaoyabuhao"
      }
    ]
  }
}
```
转换后crd
```
{
  "com.netease.circuitbreaker": {
    "consecutive_slow_requests": 3,
    "average_response_time": "0.1s",
    "min_request_amount": 3,
    "error_percent_threshold": {
      "value": 50
    },
    "break_duration": "50s",
    "lookback_duration": "10s",
    "response": {
      "http_status": 200,
      "headers": [
        {
          "key": "buhao",
          "value": "buhaoyabuhao"
        }
      ],
      "body": {
        "inline_string": "{\"ba\":\"ba\"}"
      }
    }
  }
}
```