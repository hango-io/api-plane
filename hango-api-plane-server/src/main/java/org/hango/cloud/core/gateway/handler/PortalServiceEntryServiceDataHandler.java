package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.Service;
import org.hango.cloud.meta.ServiceSubset;
import org.hango.cloud.util.CommonUtil;
import org.hango.cloud.util.Const;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hango.cloud.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/10/8
 **/
public class PortalServiceEntryServiceDataHandler extends ServiceDataHandler {

    private static int DEFAULT_PORT = 80;

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, Service service) {

        TemplateParams params = TemplateParams.instance()
                .put(NAMESPACE, service.getNamespace());

        // host由服务类型决定，
        // 当为静态时，后端地址为IP或域名，使用服务的code作为host

        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {

            String host = decorateHost(service.getCode());
            String backendService = service.getBackendService();

            List<String> addrs = new ArrayList<>();
            List<Endpoint> endpoints = new ArrayList<>();
            if (backendService.contains(",")) {
                addrs.addAll(Arrays.asList(backendService.split(",")));
            } else {
                addrs.add(backendService);
            }
            List<ServiceSubset> subsets = CollectionUtils.isEmpty(service.getSubsets()) ? Collections.EMPTY_LIST : service.getSubsets();
            addrs.stream()
                    .forEach(addr -> {
                        Endpoint e = new Endpoint();
                        if (CommonUtil.isValidIPPortAddr(addr) || addr.contains(":")) {
                            String[] ipPort = addr.split(":");
                            e.setAddress(ipPort[0]);
                            e.setPort(Integer.valueOf(ipPort[1]));
                        } else {
                            e.setAddress(addr);
                        }

                        for (ServiceSubset subset : subsets) {
                            if (!CollectionUtils.isEmpty(subset.getStaticAddrs())
                                    && subset.getStaticAddrs().contains(addr)) {
                                e.setLabels(subset.getLabels());
                                break;
                            }
                        }

                        endpoints.add(e);
                    });

            String protocol = protocolMapping(service.getProtocol());
            String protocolName = service.getProtocol().toLowerCase();
            int protocolPort = DEFAULT_PORT;

            params.put(SERVICE_ENTRY_PROTOCOL, protocol);
            params.put(SERVICE_ENTRY_PROTOCOL_NAME, protocolName);
            params.put(SERVICE_ENTRY_PROTOCOL_PORT, protocolPort);
            params.put(GATEWAY_GW_CLUSTER, service.getGateway());

            params.put("endpoints", endpoints)
                    .put(SERVICE_ENTRY_NAME, service.getCode().toLowerCase())
                    .put(SERVICE_ENTRY_HOST, host);
        }
        return Arrays.asList(params);
    }

    private static String protocolMapping(String protocol) {

        switch (protocol) {
            case "https":
                return "TLS";
            case "grpc":
                return "GRPC";
            default:
                return "HTTP";
        }
    }
}
