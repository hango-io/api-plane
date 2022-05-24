package org.hango.cloud.core.gateway.processor;

import org.hango.cloud.core.gateway.handler.DataHandler;
import org.hango.cloud.core.template.TemplateParams;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 顾名思义，永远不返回空，包括空list和null，仅用于特殊场景
 *
 **/
public class NeverReturnNullModelProcessor<T> implements ModelProcessor<T> {

    private ModelProcessor<T> modelProcessor;
    public String replacement;

    public NeverReturnNullModelProcessor(ModelProcessor<T> modelProcessor, String replacement) {
        this.modelProcessor = modelProcessor;
        this.replacement = replacement;
    }

    @Override
    public List<String> process(String template, T t, DataHandler<T> dataHandler) {

        if (modelProcessor != null) {
            List<String> results = modelProcessor.process(template, t, dataHandler);
            if (!CollectionUtils.isEmpty(results)) return results;
        }
        return Arrays.asList(replacement);
    }

    @Override
    public String process(String template, TemplateParams params) {

        if (modelProcessor != null) {
            String result = modelProcessor.process(template, params);
            if (!StringUtils.isEmpty(result)) return result;
        }
        return replacement;
    }

    @Override
    public List<String> process(String template, List<TemplateParams> params) {
        if (modelProcessor != null) {
            List<String> results = modelProcessor.process(template, params);
            if (!CollectionUtils.isEmpty(results)) return results;
        }
        return Arrays.asList(replacement);
    }
}
