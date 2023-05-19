package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.dto.IpSourceEnvoyFilterDTO;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

import static org.hango.cloud.core.template.TemplateConst.*;

/**
 * @Author zhufengwei
 * @Date 2023/5/10
 */
public class IpSourceEnvoyFilterDataHandler implements DataHandler<IpSourceEnvoyFilterDTO>{

    @Override
    public List<TemplateParams> handle(IpSourceEnvoyFilterDTO envoyFilterDTO) {
        String customIpAddressHeader = envoyFilterDTO.getCustomIpAddressHeader();
        TemplateParams efParams = TemplateParams.instance()
                .put(ENVOY_FILTER_PORT_NUMBER, envoyFilterDTO.getPortNumber());

        //使用自定义header，useRemoteAddress=false
        if (StringUtils.hasText(customIpAddressHeader)){
            efParams.put(IP_CONFIG_PATCH_CUSTOM_HEADER, customIpAddressHeader)
                    .put(IP_CONFIG_PATCH_USE_REMOTE_ADDRESS, "false");
        }else {
            efParams.put(IP_CONFIG_PATCH_XFF_NUM_TRUSTED_HOPS, envoyFilterDTO.getXffNumTrustedHops())
                    .put(IP_CONFIG_PATCH_USE_REMOTE_ADDRESS, String.valueOf(envoyFilterDTO.getUseRemoteAddress()));
        }
        return Collections.singletonList(efParams);
    }
}
