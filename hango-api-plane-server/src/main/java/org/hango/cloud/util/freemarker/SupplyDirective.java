package org.hango.cloud.util.freemarker;

import com.jayway.jsonpath.Criteria;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.template.TemplateConst;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/26
 **/
public class SupplyDirective implements TemplateDirectiveModel {

    enum Keyword {

        MATCH("match:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_MATCH_YAML))),
        ROUTE("route:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_ROUTE_YAML))),
        EXTRA("extra:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_EXTRA_YAML))),
        RETRY("retry:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_HTTP_RETRY_YAML))),
        API("name:", wrap(TemplateConst.API_IDENTITY_NAME)),
        META("meta:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_META_YAML))),
        PRIORITY("priority:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_MATCH_PRIORITY_YAML))),
        MIRROR("mirror:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_MIRROR_YAML))),
        ;

        String name;
        String replacement;

        Keyword(String name, String replacement) {
            this.name = name;
            this.replacement = replacement;
        }
    }

    private static String indent(String str) {
        return "<@indent>" + str + "</@indent>";
    }

    private static String wrap(String str) {
        return "${" + str + "}";
    }

    @Override
    public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final StringWriter writer = new StringWriter();
        if (body != null) {
            body.render(writer);
        }
        String string = writer.toString();
        if (StringUtils.isEmpty(string)) {
            string = "[{}]";
        }
        ResourceGenerator gen = ResourceGenerator.newInstance(string, ResourceType.YAML);
        gen.createOrUpdateValue("$[?]", "nsf-template-match", Keyword.MATCH.replacement, Criteria.where("match").exists(false));
        gen.createOrUpdateValue("$[?]", "nsf-template-route", Keyword.ROUTE.replacement, Criteria.where("route").exists(false));
        TemplateHashModel dataModel = environment.getDataModel();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(dataModel.get("t_virtual_service_mirror_yaml").toString())){
            gen.createOrUpdateValue("$[?]", "nsf-template-mirror", Keyword.MIRROR.replacement, Criteria.where("mirror").exists(false));
        }
        gen.createOrUpdateValue("$[?]", "nsf-template-extra", Keyword.EXTRA.replacement, Criteria.where("extra").exists(false));
        gen.createOrUpdateValue("$[?]", "nsf-template-retry", Keyword.RETRY.replacement, Criteria.where("retry").exists(false));
        gen.createOrUpdateValue("$[?]", "nsf-template-meta", Keyword.META.replacement, Criteria.where("meta").exists(false));
        gen.createOrUpdateValue("$[?]", "nsf-template-priority", Keyword.PRIORITY.replacement, Criteria.where("priority").exists(false));
        gen.createOrUpdateValue("$[?]", "name", Keyword.API.replacement, Criteria.where("name").exists(false));

        String yaml = gen.yamlString();
        yaml = yaml.replaceAll("(?m)^(-?\\s*)nsf-template-.*?:(?:\\s*)(<.*>)", "$1$2");

        environment.getOut().write(yaml);
        writer.close();
    }
}
