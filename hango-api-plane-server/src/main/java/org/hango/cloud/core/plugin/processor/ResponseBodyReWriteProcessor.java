package org.hango.cloud.core.plugin.processor;

import org.springframework.stereotype.Component;

@Component
public class ResponseBodyReWriteProcessor extends BodyReWriteProcessor{
    @Override
    public String getName() {
        return "ResponseBodyReWriteProcessor";
    }

    @Override
    protected boolean needMatchResponse() {
        //响应体重写插件支持响应头匹配
        return true;
    }

    @Override
    protected String getConfigKey() {
        return "encoder_body_transformation";
    }
}
