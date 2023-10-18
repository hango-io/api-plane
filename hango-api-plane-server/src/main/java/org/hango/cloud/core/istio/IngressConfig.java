package org.hango.cloud.core.istio;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author zhufengwei
 * @Date 2023/9/21
 */
@Component
public class IngressConfig {
    private static final Logger logger = LoggerFactory.getLogger(IngressConfig.class);

    @Autowired
    PilotHttpClient pilotHttpClient;

    private static final String INGRESS_CONTROLLER_MODE = "ingressControllerMode";
    private static final String INGRESS_CLASS = "ingressClass";
    private static final String DEFAULT_INGRESS_CLASS = "istio";
    private static final String INGRESS_PORT = "ingressPort";
    private static final String INGRESS_TLS_PORT = "ingressTlsPort";
    private static final Integer DEFAULT_INGRESS_PORT = 80;
    private static final Integer DEFAULT_INGRESS_TLS_PORT = 443;


    public Boolean openIngress(){
        Map<String, String> data = pilotHttpClient.getIstioConfigData();
        String ingressControllerMode = data.get(INGRESS_CONTROLLER_MODE);
        return "3".equals(ingressControllerMode);
    }

    public String getIngressClass(){
        Map<String, String> data = pilotHttpClient.getIstioConfigData();
        return data.getOrDefault(INGRESS_CLASS, DEFAULT_INGRESS_CLASS);
    }

    public Integer getIngressPort(){
        Map<String, String> data = pilotHttpClient.getIstioConfigData();
        String ingressPort = data.get(INGRESS_PORT);
        if (StringUtils.isBlank(ingressPort)){
            return DEFAULT_INGRESS_PORT;
        }
        try {
            int port = Integer.parseInt(ingressPort);
            if (port > 0 && port < 65536){
                return port;
            }
        } catch (NumberFormatException e) {
            logger.error("error ingressPort: {}", ingressPort);
        }
        return DEFAULT_INGRESS_PORT;
    }

    public Integer getIngressTlsPort(){
        Map<String, String> data = pilotHttpClient.getIstioConfigData();
        String ingressPort = data.get(INGRESS_TLS_PORT);
        if (StringUtils.isBlank(ingressPort)){
            return DEFAULT_INGRESS_TLS_PORT;
        }
        try {
            int port = Integer.parseInt(ingressPort);
            if (port > 0 && port < 65536){
                return port;
            }
        } catch (NumberFormatException e) {
            logger.error("error ingressPort: {}", ingressPort);
        }
        return DEFAULT_INGRESS_TLS_PORT;
    }

}
