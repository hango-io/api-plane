package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.gateway.processor.ModelProcessor;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.API;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.Service;
import org.hango.cloud.util.Const;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.hango.cloud.core.template.TemplateConst.VIRTUAL_SERVICE_DESTINATIONS;

public class PortalVirtualServiceAPIDataHandler extends BaseVirtualServiceAPIDataHandler {

    public PortalVirtualServiceAPIDataHandler(ModelProcessor modelProcessor){
        super(modelProcessor);
    };

    public PortalVirtualServiceAPIDataHandler(ModelProcessor subModelProcessor, List<FragmentWrapper> fragments, boolean simple) {
        super(subModelProcessor, fragments, Collections.emptyList(), simple);
    }

    @Override
    String produceRoute(API api, List<Endpoint> endpoints, String subset) {

        if (simple) return "";
        List<Map<String, Object>> destinations = new ArrayList<>();

        //only one gateway
        List<String> gateways = api.getGateways();
        String gateway = gateways.get(0);

        for (Service service : api.getProxyServices()) {

            Map<String, Object> param = new HashMap<>();
            param.put("weight", service.getWeight());
            param.put("subset", StringUtils.isEmpty(service.getSubset()) ? service.getCode() + "-" + gateway : service.getSubset());

            Integer port = -1;
            String host = decorateHost(service.getCode());

            if (Const.PROXY_SERVICE_TYPE_DYNAMIC.equals(service.getType())) {
                host = service.getBackendService();
                port = service.getPort() == null ? 80 : service.getPort();
            } else if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
                port = 80;
            }

            if (port == -1) throw new ApiPlaneException(String.format("%s:%s", ExceptionConst.TARGET_SERVICE_NON_EXIST, service.getBackendService()));

            param.put("port", port);
            param.put("host", host);
            destinations.add(param);
        }

        String destinationStr = subModelProcessor.process(apiVirtualServiceRoute, TemplateParams.instance().put(VIRTUAL_SERVICE_DESTINATIONS, destinations));
        return destinationStr;
    }


    @Override
    String produceHostHeaders(API api) {
        return "";
    }
}
