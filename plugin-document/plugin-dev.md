# 插件开发文档
这里的插件开发仅涉及到apiplane层，主要职责即**将插件schema转成crd片段**。后续apiplane会组装插件crd片段，完成由接口到crd转换的完整流程。

## 插件开发准备工作
1. 需要了解插件对应crd的格式
2. 定出对外暴露的schema格式

例如，开发ip-restriction插件，了解到对应的crd格式如下，ip-restriction对应的部分是crd中的userPlugin中的部分
```json
{
  "apiVersion" : "networking.istio.io/v1alpha3",
  "kind" : "VirtualService",
  "metadata" : {
    "creationTimestamp" : "2020-02-03T12:03:32Z",
    "generation" : 1,
    "labels" : {
      "api_service" : "httpbin"
    },
    "name" : "httpbin-demo",
    "namespace" : "gateway-system",
    "resourceVersion" : "15064998",
    "selfLink" : "/apis/networking.istio.io/v1alpha3/namespaces/gateway-system/virtualservices/httpbin-demo",
    "uid" : "330ef842-467d-11ea-9432-fa163e112972"
  },
  "spec" : {
    "gateways" : [ "demo" ],
    "hosts" : [ "*" ],
    "http" : [ {
      "api" : "httpbin",
      "match" : [ {
        "headers" : {
          ":authority" : {
            "regex" : "httpbin\\.com"
          }
        },
        "method" : {
          "regex" : "GET|POST"
        },
        "uri" : {
          "regex" : ".*"
        }
      } ],
      "meta" : {
        "qz_cluster_name" : "demo"
      },
      "priority" : 42,
      "retries" : {
        "attempts" : 5
      },
      "route" : [ {
        "destination" : {
          "host" : "httpbin.apiplane-test.svc.cluster.local",
          "port" : {
            "number" : 8000
          },
          "subset" : "httpbin-httpbin-demo"
        },
        "weight" : 100
      } ]
    } ],
    "plugins" : {
      "httpbin" : {
        "userPlugin" : [ {
          "ipRestriction" : {
            "list" : [ "127.0.0.1" ],
            "type" : "WHITE"
          }
        } ]
      }
    }
  }
}
```
定出转换前的插件schema，例如ip-restriction的例子
```json
{
  "kind": "ip-restriction",
  "type": 1,
  "list": [
    "127.0.0.1"
  ]
}
```
## 插件开发流程
熟悉了插件转换前的schema格式和插件准换后的crd格式就可以着手开始开发插件。

流程：
1. 实现SchemaProcessor接口。
2. 在resources/template/plugin下写插件schema。
3. 修改plugin-config.json，将schema与SchemaProcessor进行映射。

## SchemaProcessor

其中插件开发的80%工作在于SchemaProcessor的开发。SchemaProcessor接口如下:
```java
public interface SchemaProcessor<T> {
    String getName();

    FragmentHolder process(String plugin, T serviceInfo);

    // 默认不做合并
    default List<FragmentHolder> process(List<String> plugins, T serviceInfo) {
        return plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
    }
}
```
接口实际给上层使用的方法是List<FragmentHolder> process(List<String> plugins, T serviceInfo)，即复数形式的方法，该方法会接收该Processor映射的所有插件内容，方法默认实现方式是重复调用单数形式的process方法，但特殊插件需要自己另外实现该方法，例如需要将租户进行分类合并的场景。

Processor有两个特殊的实现类：1. AggregateExtensionProcessor(使用crd透传通道的聚合Processor) 2. AggregateGlobalProcessor(项目级插件用到的聚合Processor)

## FragmentHolder
fragmentHolder用来保存返回给上层拼接的多个crd片段，fragmentHolder现在可以保存三种片段
```java
// 用来保存VirtualService的片段
private FragmentWrapper virtualServiceFragment;
// 用来保存SharedConfig的片段
private FragmentWrapper sharedConfigFragment;
// 用来保存GatewayPlugins的片段
private FragmentWrapper gatewayPluginsFragment;
```
其中sharedConfigFragment只有ratelimit插件用到，gatewayPluginsFragment插件是新特性，目前是项目级插件需要用到。

FragmentWrapper对象用来保存单个crd片段
```java
// 保存的片段crd类型
private K8sResourceEnum resourceType;
// 保存的片段类型
private FragmentTypeEnum fragmentType;
// 保存的片段
private String content;
// 多租户的id
private String xUserId;
```

K8sResourceEnum是用来表示该片段是属于什么crd资源，大部分用到的都是VirutalSerice类型

FragmentTypeEnum用来表示片段是什么类型，大部分用到的都是VS_API类型，特殊的如路由插件会用到VS_MATCH类型,ratelimit插件会用到SHARECONFIG类型

xUserId是用来保存多租户的id，目前仅提供给严选的插件会用到

content用来保存crd片段，***content对片段格式有严格要求***，以上述ip-restriction的插件为例，要求content必须为
```yaml
ipRestriction:
  list:
    - 127.0.0.1
  type: white
```
即格式必须为userPlugin的下一级的yaml格式，SchemaProcessor上层调用也会按照严格的格式渲染模板，来保证渲染正确格式的crd

字段是使用驼峰式还是下划线式也有区别，如果使用的是透传通道则需要使用下划线式的字段，其他则用驼峰式

## 透传通道与非透传通道插件
所谓透传通道是为了减少链路，pilot层不做处理直接将apiplane的插件配置透出到envoy。对应到VirtualService proto中的ExtensionPlugin，因为envoy只支持下划线式的命名，所以使用透传通道的插件必须严格使用下划线式。

开发使用透传通道的插件时，除了开发Processor外，需要另外在AggregateExtensionProcessor里映射插件processor和插件名，并且在plugin-config.json中，映射processor时，也要使用AggregateExtensionProcessor。

## 项目级插件
目级插件与使用透传通道的插件一样，直接透传到envoy，项目级插件也用到了特殊的processor,即AggregateGlobalProcessor，AggregateGlobalProcessor会复用AggregateExtensionProcessor。

## 现有插件适配项目级插件的适配流程
1. 修改现有processor实现，驼峰式需要改为下划线式
2. 修改AggregateExtensionProcessor，增加processor映射关系
3. 修改global/plugin-config.json，增加kind到processor的映射关系
4. 修改portal或yx下的plugin-config.json，由原来直接映射到插件processor改为映射到修改AggregateExtensionProcessor

现有支持项目插件的有：jsonp、rewrite、transformer、static-downgrade可以作为参考