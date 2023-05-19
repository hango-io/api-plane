package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.dto.GrpcEnvoyFilterDTO;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

import static org.hango.cloud.core.template.TemplateConst.*;

/**
 * @author xin li
 * @date 2022/6/24 10:34
 */
@SuppressWarnings("ALL")
public class GrpcEnvoyFilterDataHandler implements DataHandler<GrpcEnvoyFilterDTO> {
    @Override
    public List<TemplateParams> handle(GrpcEnvoyFilterDTO grpcEnvoyFilterDto) {
        String servicesYml = "";
        if (!CollectionUtils.isEmpty(grpcEnvoyFilterDto.getServices())) {
            //"\n        - "处理yml填充缩进
            servicesYml = grpcEnvoyFilterDto.getServices().stream().map(a -> "\n        - " + a).reduce((a, b) -> a + b).get();
        }
        TemplateParams efParams = TemplateParams.instance()
                .put(ENVOY_FILTER_PORT_NUMBER, grpcEnvoyFilterDto.getPortNumber())
                .put(GRPC_CONFIG_PATCH_PROTO_DESCRIPTOR_BIN, generateProtoDescriptionBin(grpcEnvoyFilterDto))
                .put(GRPC_CONFIG_PATCH_SERVICES, servicesYml);
        return Collections.singletonList(efParams);
    }

    private String generateProtoDescriptionBin(GrpcEnvoyFilterDTO grpcEnvoyFilterDto) {
        return grpcEnvoyFilterDto.getProtoDescriptorBin() == null ?
                "" : grpcEnvoyFilterDto.getProtoDescriptorBin().replace(" ", "").replace("\n", "");
    }
}
