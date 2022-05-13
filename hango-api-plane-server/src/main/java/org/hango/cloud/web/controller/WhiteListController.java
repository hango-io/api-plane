package org.hango.cloud.web.controller;

import org.hango.cloud.meta.WhiteList;
import org.hango.cloud.service.WhiteListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/26
 **/
@RestController
@RequestMapping(value = "/api/istio/rbac", params = "Version=2018-05-31")
public class WhiteListController extends BaseController {
    @Autowired
    private WhiteListService whiteListService;
    private static final Logger logger = LoggerFactory.getLogger(WhiteListController.class);

    @RequestMapping(params = "Action=EnableTls")
    public String update(@RequestHeader(value = "X-Forwarded-Client-Cert", required = false) String certHeader) {
        WhiteList whiteList = new WhiteList();
        if (!resolveRequestCert(whiteList, certHeader) || whiteList.getService() == null || whiteList.getService().equals("")) {
            return apiReturn(401, "UnAuthenticated", String.format("UnAuthenticated, X-Forwarded-Client-Cert: %s", certHeader), null);
        }
        if (whiteList.getService().startsWith("qz-") || whiteList.getNamespace().equals("istio-system")) {
            return apiReturn(403, "UnAuthorized", String.format("UnAuthorized, whitelist request not allowed from %s.%s", whiteList.getService(), whiteList.getNamespace()), null);
        }
        whiteListService.updateService(whiteList);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=GetGatewayIps", method = RequestMethod.POST)
    public String getGatewayIps() throws IOException {
        Map<String, Object> result = new HashMap<>();
		Properties properties = new Properties();
        try (InputStream in = new FileInputStream("/etc/qz-api/gateway-ips.properties")){
            properties.load(in);
            for(String key: Arrays.asList("ingressKong", "egressKong", "ingress", "egress")) {
                result.put(key, Arrays.asList(properties.getProperty(key, "").split(",")));
            }
        } catch (IOException e) {
            throw e;
        }
        return apiReturn(SUCCESS, "Success", null, result);
    }

    private boolean resolveRequestCert(WhiteList whiteList, String header) {
    	logger.info("resolving cert: '{}'", header);
        if (header == null || header.equals("")) {
            return false;
        }
        String[] spiltValues = header.split(";");
        String uri = null;
        for (String spiltValue : spiltValues) {
            Pattern pattern = Pattern.compile("(.*)=(.*)");
            Matcher matcher = pattern.matcher(spiltValue);
            if (matcher.find()) {
                String k = matcher.group(1);
                String v = matcher.group(2);
                if (k.equalsIgnoreCase("uri")) {
                    uri = v;
                    break;
                }
            }
        }
        if (uri == null || uri.equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile("spiffe://(.*)/ns/(.*)/sa/(.*)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            whiteList.setNamespace(matcher.group(2));
            whiteList.setService(matcher.group(3));
        } else {
            return false;
        }
        logger.info("successfully resolved target service: {}.{}, cert: {}", whiteList.getNamespace(), whiteList.getService(), header);
        return true;
    }

}
