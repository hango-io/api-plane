package org.hango.cloud.core.gateway.processor;

import org.hango.cloud.core.gateway.handler.DataHandler;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.core.template.TemplateTranslator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class RenderTwiceModelProcessor<T> implements ModelProcessor<T> {

    private TemplateTranslator templateTranslator;

    public RenderTwiceModelProcessor(TemplateTranslator templateTranslator) {
        this.templateTranslator = templateTranslator;
    }

    @Override
    public List<String> process(String template, T t, DataHandler<T> dataHandler) {
        List<TemplateParams> params = dataHandler.handle(t);
        return process(template, params);
    }

    @Override
    public String process(String template, TemplateParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> process(String template, List<TemplateParams> params) {
        if (CollectionUtils.isEmpty(params)) return Collections.emptyList();

        return params.stream()
                .map(p -> templateTranslator.translate("tmp",
                        templateTranslator.translate(template, p.output()),
                        p.output()))
                .filter(r -> !StringUtils.isEmpty(r))
                .collect(Collectors.toList());
    }

}
