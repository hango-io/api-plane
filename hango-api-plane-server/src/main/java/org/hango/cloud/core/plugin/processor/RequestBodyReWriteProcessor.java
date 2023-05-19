package org.hango.cloud.core.plugin.processor;

import org.springframework.stereotype.Component;

@Component
public class RequestBodyReWriteProcessor extends BodyReWriteProcessor {


    @Override
    public String getName() {
        return "RequestBodyReWriteProcessor";
    }

    @Override
    protected boolean needMatchResponse() {
        //请求体重写插件不支持响应头匹配
        return false;
    }

    @Override
    protected String getConfigKey() {
        return "decoder_body_transformation";
    }
}
