package org.hango.cloud.core.plugin.processor;

import io.micrometer.core.instrument.util.StringEscapeUtils;
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
@SuppressWarnings("java:S1192")
public class RedisCacheProcessor extends AbstractSchemaProcessor implements
    SchemaProcessor<ServiceInfo> {

  @Override
  public String getName() {
    return "RedisCache";
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
        String jsonHeaderValue = StringEscapeUtils.escapeJson(headerValue);
        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(safe_regex_string_match, headerKey, jsonHeaderValue));
        } else if ("present_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(present_match, headerKey));
        } else if ("present_match_invert".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(present_invert_match, headerKey));
        } else {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(exact_string_match, headerKey, jsonHeaderValue));
        }
      });
    }
    if (source.contain("$.condition.request.host")) {
      String matchType = source.getValue("$.condition.request.host.match_type", String.class);
      String host = source.getValue("$.condition.request.host.value", String.class);
      if (nonNull(matchType, host)) {
        String jsonHost = StringEscapeUtils.escapeJson(host);
        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(safe_regex_string_match, ":authority", jsonHost));
        } else {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(exact_string_match, ":authority", jsonHost));
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
        String jsonPath = StringEscapeUtils.escapeJson(path);
        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rqx.headers",
              String.format(safe_regex_string_match, ":path", jsonPath));
        } else {
          builder
              .addJsonElement("$.enable_rqx.headers", String.format(exact_string_match, ":path", jsonPath));
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
        String jsonHeaderValue = StringEscapeUtils.escapeJson(headerValue);
        if ("safe_regex_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(safe_regex_string_match, headerKey, jsonHeaderValue));
        } else if ("present_match".equals(matchType)) {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(present_match, headerKey));
        } else if ("present_match_invert".equals(matchType)) {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(present_invert_match, headerKey));
        } else {
          builder.addJsonElement("$.enable_rpx.headers",
              String.format(exact_string_match, headerKey, jsonHeaderValue));
        }
      });
    }
    if (source.contain("$.condition.response.code.value")) {
      String code = source.getValue("$.condition.response.code.value", String.class);
      if (nonNull(code)) {
        builder.addJsonElement("$.enable_rpx.headers",
            String.format(safe_regex_string_match, ":status", StringEscapeUtils.escapeJson(code) + "|"));
      }
    }

    // redis http cache ttl
    Integer redisDefaultTtl = source.getValue("$.ttl.redis.default", Integer.class);
    if (nonNull(redisDefaultTtl) && redisDefaultTtl != 0) {
      builder.createOrUpdateJson("$", "cache_ttls", "{\"RedisHttpCache\":{}}");
      builder.createOrUpdateValue("$.cache_ttls.RedisHttpCache", "default", redisDefaultTtl * 1000);
    }
    if (source.contain("$.ttl.redis.custom")) {
      List<Map<String, String>> customTtl = source.getValue("$.ttl.redis.custom", List.class);
        if (!CollectionUtils.isEmpty(customTtl)) {
            builder.createOrUpdateJson("$.cache_ttls.RedisHttpCache", "customs", "{}");
        }
      customTtl.forEach(item -> {
        String code = item.get("code");
        String value = item.get("value");
          if (haveNull(code, value)) {
              return;
          }
        builder.createOrUpdateValue("$.cache_ttls.RedisHttpCache.customs", code,
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
        .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
        .withContent(builder.yamlString())
        .build();
    fragmentHolder.setGatewayPluginsFragment(wrapper);
    return fragmentHolder;
  }
}
