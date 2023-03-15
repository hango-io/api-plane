package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Component
public class LocalCacheProcessor extends AbstractSchemaProcessor implements
    SchemaProcessor<ServiceInfo> {

  @Override
  public String getName() {
    return "LocalCache";
  }

  @Override
  public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
    PluginGenerator source = PluginGenerator.newInstance(plugin);
    PluginGenerator builder = PluginGenerator
        .newInstance("{\"key_maker\":{\"exclude_host\":\"false\", \"ignore_case\":\"true\"}}");
    // condition request
    if (source.contain("$.condition.request.requestSwitch") && source
        .getValue("$.condition.request.requestSwitch", Boolean.class)) {
      builder.createOrUpdateJson("$", "enable_rqx", "{\"headers\":[]}");
    }
    if (source.contain("$.condition.request.headers")) {
      List<Map<String, String>> headers = source
          .getValue("$.condition.request.headers", List.class);
      headers.forEach(item -> {
        String matchType = item.get("match_type");
        String headerKey = item.get("headerKey");
        String headerValue = item.get("value");
        if (haveNull(matchType, headerKey)) {
          return;
        }

        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(safe_regex_string_match, headerKey, headerValue));
        } else if ("present_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(present_match, headerKey));
        } else if ("present_match_invert".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(present_invert_match, headerKey));
        } else {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(exact_string_match, headerKey, headerValue));
        }
      });
    }
    if (source.contain("$.condition.request.host")) {
      String matchType = source.getValue("$.condition.request.host.match_type", String.class);
      String host = source.getValue("$.condition.request.host.value", String.class);
      if (nonNull(matchType, host)) {
        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(safe_regex_string_match, ":authority", host));
        } else {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(exact_string_match, ":authority", host));
        }
      }
    }
    if (source.contain("$.condition.request.method")) {
      List<String> method = source.getValue("$.condition.request.method", List.class);
      if (nonNull(method)) {
        if (method.size() == 1) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(exact_string_match, ":method", method.get(0)));
        } else if (method.size() > 1) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(safe_regex_string_match, ":method", String.join("|", method)));
        }
      }
    }
    if (source.contain("$.condition.request.path")) {
      String matchType = source.getValue("$.condition.request.path.match_type", String.class);
      String path = source.getValue("$.condition.request.path.value", String.class);
      if (nonNull(matchType, path)) {
        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(safe_regex_string_match, ":path", path));
        } else {
          builder
              .addJsonElement("$.enable_rqx.headers", String.format(exact_string_match, ":path", path));
        }
      }
    }
    // condition response
    if (source.contain("$.condition.response")) {
      builder.createOrUpdateJson("$", "enable_rpx", "{\"headers\":[]}");
    }
    if (source.contain("$.condition.response.headers")) {
      List<Map<String, String>> headers = source
          .getValue("$.condition.response.headers", List.class);
      headers.forEach(item -> {
        String matchType = item.get("match_type");
        String headerKey = item.get("headerKey");
        String headerValue = item.get("value");
        if (haveNull(matchType, headerKey)) {
          return;
        }
        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(safe_regex_string_match, headerKey, headerValue));
        } else if ("present_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(present_match, headerKey));
        } else if ("present_match_invert".equals(matchType)) {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(present_invert_match, headerKey));
        } else {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(exact_string_match, headerKey, headerValue));
        }
      });
    }
    if (source.contain("$.condition.response.code.value")) {
      String code = source.getValue("$.condition.response.code.value", String.class);
      if (nonNull(code)) {
        builder.addJsonElement("$.enable_rpx.headers",
            String.format(safe_regex_string_match, ":status", code + "|"));
      }
    }

    // local http cache ttl
    Integer localDefaultTtl = source.getValue("$.ttl.local.default", Integer.class);
    if (nonNull(localDefaultTtl) && localDefaultTtl != 0) {
      builder.createOrUpdateJson("$", "cache_ttls", "{\"LocalHttpCache\":{}}");
      builder.createOrUpdateValue("$.cache_ttls.LocalHttpCache", "default", localDefaultTtl * 1000);
    }
    if (source.contain("$.ttl.local.custom")) {
      List<Map<String, String>> customTtl = source.getValue("$.ttl.local.custom", List.class);
      if (!CollectionUtils.isEmpty(customTtl)) {
        builder.createOrUpdateJson("$.cache_ttls.LocalHttpCache", "customs", "{}");
      }
      customTtl.forEach(item -> {
        String code = item.get("code");
        String value = item.get("value");
        if (haveNull(code, value)) {
          return;
        }
        builder.createOrUpdateValue("$.cache_ttls.LocalHttpCache.customs", code,
            Integer.parseInt(value) * 1000);
      });
    }

    // key maker
    Boolean excludeHost = source.getValue("$.keyMaker.excludeHost", Boolean.class);
    if (nonNull(excludeHost)) {
      builder.updateValue("$.key_maker.exclude_host", excludeHost);
    }
    Boolean ignoreCase = source.getValue("$.keyMaker.ignoreCase", Boolean.class);
    if (nonNull(ignoreCase)) {
      builder.updateValue("$.key_maker.ignore_case", ignoreCase);
    }
    if (source.contain("$.keyMaker.headers")) {
      builder.createOrUpdateJson("$.key_maker", "headers_keys", "[]");
      List<String> headers = source.getValue("$.keyMaker.headers", List.class);
      headers.forEach(item -> builder.addElement("$.key_maker.headers_keys", item));
    }
    if (source.contain("$.keyMaker.queryString")) {
      builder.createOrUpdateJson("$.key_maker", "query_params", "[]");
      List<String> queryStrings = source.getValue("$.keyMaker.queryString", List.class);
      queryStrings.forEach(item -> builder.addElement("$.key_maker.query_params", item));
    }

    FragmentHolder fragmentHolder = new FragmentHolder();
    FragmentWrapper wrapper = new FragmentWrapper.Builder()
        .withXUserId(getAndDeleteXUserId(source))
        .withFragmentType(FragmentTypeEnum.VS_API)
        .withResourceType(K8sResourceEnum.VirtualService)
        .withContent(builder.yamlString())
        .build();
    fragmentHolder.setVirtualServiceFragment(wrapper);
    return fragmentHolder;
  }
}
