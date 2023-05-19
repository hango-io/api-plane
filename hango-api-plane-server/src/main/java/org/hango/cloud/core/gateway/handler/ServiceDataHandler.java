package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.Service;

import java.util.List;

public abstract class ServiceDataHandler implements DataHandler<Service> {

    @Override
    public List<TemplateParams> handle(Service service) {
        return doHandle(TemplateParams.instance(), service);
    }

    abstract List<TemplateParams> doHandle(TemplateParams tp, Service service);

    String decorateHost(String code) {
        return String.format("com.netease.%s", code);
    }
}
