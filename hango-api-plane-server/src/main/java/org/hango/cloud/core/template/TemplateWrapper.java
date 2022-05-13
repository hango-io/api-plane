package org.hango.cloud.core.template;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 封装Template
 * 1. 自动过滤Template注释行
 * 2. 支持Template 打Label, 格式为#@key=value
 * 3. 支持Template 注释, 格式为#注释
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public class TemplateWrapper {

    private Map<String, String> labels;
    private List<String> description;
    private String source;

    private Template originTemplate;
    private Template thisTemplate;

    private void initThisTemplate(Template template) throws IOException {
        String tmp = originTemplate.toString();
        Pattern notePat = Pattern.compile(TemplateConst.DESCRIPTION_TAG);
        Matcher noteMat = notePat.matcher(tmp);
        while (noteMat.find()) {
            description.add(noteMat.group(1));
        }
        tmp = noteMat.replaceAll("");

        Pattern labelPat = Pattern.compile(TemplateConst.LABEL_TAG);
        Matcher labelMat = labelPat.matcher(tmp);
        while (labelMat.find()) {
            labels.put(labelMat.group(1), labelMat.group(2));
        }
        tmp = labelMat.replaceAll("");

        tmp = Pattern.compile(TemplateConst.BLANK_LINE).matcher(tmp).replaceAll("");
        this.source = tmp;

        thisTemplate = new Template(template.getName(), tmp, template.getConfiguration());
    }

    public TemplateWrapper(Template template) throws IOException {
        this.originTemplate = template;
        this.labels = new HashMap<>();
        this.description = new ArrayList<>();
        initThisTemplate(template);
    }

    public TemplateWrapper(String name, String sourceCode, Configuration cfg) throws IOException {
        this(new Template(name, sourceCode, cfg));
    }

    public boolean containKey(String key) {
        return labels.containsKey(key);
    }

    public boolean containLabel(String key, String value) {
        return labels.containsKey(key) && labels.get(key).equals(value);
    }

    public String getLabelValue(String key) {
        return labels.get(key);
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getDescription() {
        return String.join("\n", description.toArray(new String[0]));
    }

    public String getSource() {
        return this.source;
    }

    public Template getOrigin() {
        return this.originTemplate;
    }

    public Template get() {
        return this.thisTemplate;
    }
}
