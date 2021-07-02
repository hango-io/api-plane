package org.hango.cloud.core.template;

import org.hango.cloud.util.exception.ApiPlaneException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Arrays;

@Component
public class TemplateTranslator {
    private static final Logger logger = LoggerFactory.getLogger(TemplateTranslator.class);

    private Configuration configuration;

    public static final String DEFAULT_TEMPLATE_SPILIT = "---";

    @Autowired
    public TemplateTranslator(Configuration configuration) {
        this.configuration = configuration;
    }


    /**
     * @param templateName 模板名
     * @param model        数据模型
     * @return
     */
    public String translate(String templateName, Object model) {
        String content = null;
        try {
            Template template = configuration.getTemplate(templateName + ".ftl");
            content = translate(template, model);
        } catch (IOException e) {
            logger.warn("get template failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        }
        return content;
    }

    /**
     * @param template 模板
     * @param model    数据模型
     * @return
     */
    public String translate(Template template, Object model) {
        String content = null;
        try {
            content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException e) {
            logger.warn("get template failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        } catch (TemplateException e) {
            logger.warn("parse template failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        }
        return content;
    }

    /**
     * @param templateName 模板名
     * @param sourceCode   模板代码
     * @param model        数据模型
     * @return
     */
    public String translate(String templateName, String sourceCode, Object model) {
        String content = null;
        try {
            Template template = new Template(templateName, sourceCode, configuration);
            content = translate(template, model);
        } catch (IOException e) {
            logger.warn("new template failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        }
        return content;
    }

    /**
     * @param templateName 模板名
     * @param model        数据模型
     * @param separator    分隔符
     * @return
     */
    public String[] translate(String templateName, Object model, String separator) {
        String content = translate(templateName, model);
        return split(content, separator);
    }

    /**
     * @param template  模板
     * @param model     数据模型
     * @param separator 分隔符
     * @return
     */
    public String[] translate(Template template, Object model, String separator) {
        String content = translate(template, model);
        return split(content, separator);
    }

    /**
     * @param templateName 模板名
     * @param sourceCode   模板代码
     * @param model        数据模型
     * @param separator    分隔符
     * @return
     */
    public String[] translate(String templateName, String sourceCode, Object model, String separator) {
        String content = translate(templateName, sourceCode, model);
        return split(content, separator);
    }

    private String[] split(String content, String separator) {
        return Arrays.stream(content.split(separator))
                .filter(s -> !StringUtils.isEmpty(s))
                .toArray(String[]::new);
    }
}
