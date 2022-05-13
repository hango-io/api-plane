package org.hango.cloud.core.gateway.processor;

import org.hango.cloud.core.gateway.handler.DataHandler;
import org.hango.cloud.core.template.TemplateParams;

import java.util.List;

public interface ModelProcessor<T> {

    List<String> process(String template, T t, DataHandler<T> dataHandler);

    String process(String template, TemplateParams params);

    List<String> process(String template, List<TemplateParams> params);

}
