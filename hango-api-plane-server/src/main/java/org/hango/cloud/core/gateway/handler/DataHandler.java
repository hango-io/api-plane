package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateParams;

import java.util.List;

/**
 * 处理数据
 * @param <T>
 */
public interface DataHandler<T> {

    List<TemplateParams> handle(T t);

}
