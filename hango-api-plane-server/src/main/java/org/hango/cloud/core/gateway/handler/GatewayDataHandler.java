package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.IstioGateway;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/8
 */
public abstract class GatewayDataHandler implements  DataHandler<IstioGateway>{

    @Override
    public List<TemplateParams> handle(IstioGateway istioGateway) {
        return doHandle(TemplateParams.instance(), istioGateway);    }

    abstract List<TemplateParams> doHandle(TemplateParams tp, IstioGateway istioGateway);

}
