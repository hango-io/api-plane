package org.hango.cloud.core.slime;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/19
 **/
@Component
public class SlimeHttpClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${slimeUrl:#{null}}")
    String slimeUrl;

    @Value("${slimeNamespace:mesh-operator}")
    String slimeNamespace;

    @Value("${slimeNamespace:slime-metrics}")
    String slimeName;

    private static final String DELETE_NOTIFY = "/d";

    private String getSlimeUrl() {
        if (!StringUtils.isEmpty(slimeUrl)) return slimeUrl;
        //fixed port
        int port = 7777;
        String svcName = slimeName + "." + slimeNamespace;
        return String.format("http://%s:%d", svcName, port);
    }

    public void notifyDeletion(String type, String name, String namespace) {
        Map<String, String> body = ImmutableMap.of("name", name, "namespace", namespace, "resourceType", type.toLowerCase());
        restTemplate.exchange(new RequestEntity<>(body, HttpMethod.POST, URI.create(getSlimeUrl() + DELETE_NOTIFY)), String.class);
    }

}
