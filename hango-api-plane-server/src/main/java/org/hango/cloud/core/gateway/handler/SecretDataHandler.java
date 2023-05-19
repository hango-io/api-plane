package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.Secret;

import java.util.Collections;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 * secret资源处理器
 */
public class SecretDataHandler implements DataHandler<Secret> {

    @Override
    public List<TemplateParams> handle(Secret secret) {
        TemplateParams params = TemplateParams.instance()
                .put(TemplateConst.SECRET_NAME, secret.getName())
                .put(TemplateConst.SECRET_TLS_CRT, secret.getServerCrt())
                .put(TemplateConst.SECRET_TLS_KEY, secret.getServerKey())
                .put(TemplateConst.SECRET_CA_CRT, secret.getCaCrt());
        return Collections.singletonList(params);
    }
}
