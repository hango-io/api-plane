package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xin li
 * @date 2022/7/20 11:33
 */
@Component
public class WafProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {


    private static final String WAF_RULE_PATH = "waf_rule_path";

    private static final String WAF_CONFIG_PREFIX = "tx.";

    private static final String DOS_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-912-DOS-PROTECTION.conf";
    private static final String SCANNER_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-913-SCANNER-DETECTION.conf";
    private static final String LFI_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-930-APPLICATION-ATTACK-LFI.conf";
    private static final String RFI_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-931-APPLICATION-ATTACK-RFI.conf";
    private static final String RCE_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-932-APPLICATION-ATTACK-RCE.conf";
    private static final String PHP_INJECTION_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-933-APPLICATION-ATTACK-PHP.conf";
    private static final String XSS_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-941-APPLICATION-ATTACK-XSS.conf";
    private static final String SQLI_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-942-APPLICATION-ATTACK-SQLI.conf";
    private static final String SESSION_FIXATION_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-943-APPLICATION-ATTACK-SESSION-FIXATION.conf";
    private static final String JAVA_INJECTION_WAF_RULE_PATH = "/etc/envoy/waf/REQUEST-944-APPLICATION-ATTACK-JAVA.conf";
    private static final String CGI_DATA_LEAKAGES_WAF_RULE_PATH = "/etc/envoy/waf/RESPONSE-950-DATA-LEAKAGES.conf";
    private static final String SQL_DATA_LEAKAGES_WAF_RULE_PATH = "/etc/envoy/waf/RESPONSE-951-DATA-LEAKAGES-SQL.conf";
    private static final String JAVA_DATA_LEAKAGES_WAF_RULE_PATH = "/etc/envoy/waf/RESPONSE-952-DATA-LEAKAGES-JAVA.conf";
    private static final String PHP_DATA_LEAKAGES_WAF_RULE_PATH = "/etc/envoy/waf/RESPONSE-953-DATA-LEAKAGES-PHP.conf";
    private static final String IIS_DATA_LEAKAGES_WAF_RULE_PATH = "/etc/envoy/waf/RESPONSE-954-DATA-LEAKAGES-IIS.conf";

    @Override
    public String getName() {
        return "WafProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{\"waf_rule\":[]}");
        generateWafRules(source, builder);

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);

        return fragmentHolder;
    }

    private void generateWafRules(PluginGenerator source, PluginGenerator builder) {
        PluginGenerator elementBuilder;
        if (getWafPluginSwitch(source, "dosSwitch")) {
            List<Map<String, Object>> dosConfigs = source.getValue("$.wafRule..dosConfig");
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, DOS_WAF_RULE_PATH);

            Map<String, Object> dosConfig = extractedConfig(dosConfigs);

            elementBuilder.createOrUpdateValue("$", "config", dosConfig);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "scannerSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, SCANNER_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "lfiSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, LFI_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "rfiSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, RFI_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "rceSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, RCE_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "phpInjectionSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, PHP_INJECTION_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "xssSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, XSS_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "sqliSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, SQLI_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "sessionFixationSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, SESSION_FIXATION_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "javaInjectionSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, JAVA_INJECTION_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "cgiDataLeakagesSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, CGI_DATA_LEAKAGES_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "sqlDataLeakagesSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, SQL_DATA_LEAKAGES_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "javaDataLeakagesSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, JAVA_DATA_LEAKAGES_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "phpDataLeakagesSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, PHP_DATA_LEAKAGES_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }

        if (getWafPluginSwitch(source, "iisDataLeakagesSwitch")) {
            elementBuilder = PluginGenerator.newInstance("{}");
            elementBuilder.createOrUpdateValue("$", WAF_RULE_PATH, IIS_DATA_LEAKAGES_WAF_RULE_PATH);
            builder.addJsonElement("$.waf_rule", elementBuilder.jsonString());
        }
    }

    /**
     * 数据面约定的waf配置中key为{tx.xxx}，但若key中含有"."，会导致轻舟schema执行校验时出现层级匹配问题 => please transfer a valid prop path to form item!
     * 故在后端处理配置key前缀
     * @param configs
     * @return
     */
    private static Map<String, Object> extractedConfig(List<Map<String, Object>> configs) {
        Map<String, Object> dosConfig = configs.get(0);
        Map<String, Object> config = new HashMap<>();
        for (Map.Entry<String, Object> entry : dosConfig.entrySet()) {
            config.put(WAF_CONFIG_PREFIX + entry.getKey(), entry.getValue());
        }
        return config;
    }

    private boolean getWafPluginSwitch(PluginGenerator source, String wafPluginKey) {
        List<Boolean> pluginSwitch = source.getValue("$.wafRule.." + wafPluginKey);
        return pluginSwitch.get(0);
    }
}
