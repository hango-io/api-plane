package org.hango.cloud.core.plugin.processor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ParameterValidateProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static final String PARAMETERS_VALIDATE_RULES = "$.parametersValidateRules";
    private static final String PARAMETERS_NAME = "parametersName";
    private static final String PARAMETERS_FROM = "parameterFrom";

    private static final String REQUIRED = "required";

    private static final String RULE_STRING_VALIDATE = "$.rule.string_validate";
    private static final String RULE_STRING_VALIDATE_LENGTH = "$.rule.string_validate.length";

    private static final String FROM = "from";

    private static final String NAME = "name";

    private static final String MUST = "must";
    private static final String MAX = "max";
    private static final String MIN = "min";

    private static final String INTEGER_MIN_VALUE = "integerMinValue";
    private static final String INTEGER_MAX_VALUE = "integerMaxValue";
    private static final String RULE = "rule";

    private static final String MIN_STRING_LENGTH = "minStringLength";

    private static final String MAX_STRING_LENGTH = "maxStringLength";

    private static final String STRING_VALIDATE = "string_validate";

    private static final String RULE_STRING_VALIDATE_PATTERN = "$.rule.string_validate.pattern";

    private static final String INT_VALIDATE = "int_validate";
    private static final String FLOAT_VALIDATE = "float_validate";

    private static final String BOOL_VALIDATE = "bool_validate";

    private static final String RULE_INT_VALIDATE_RANGE = ".rule.int_validate.range";
    private static final String RULE_FLOAT_VALIDATE_RANGE = ".rule.float_validate.range";

    private static final String PARAMETER_VALIDATE_RULES = "$.parameter_validate_rules";
    private static final String FLOAT_MIN_VALUE = "floatMinValue";

    private static final String FLOAT_MAX_VALUE = "floatMaxValue";

    private static final String EXCEPTION = "The minimum must be less than the maximum";
    private final Logger logger = LoggerFactory.getLogger(ParameterValidateProcessor.class);

    @Override
    public String getName() {
        return "ParameterValidateProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        logger.info("[parameter validate] plugin: {}", plugin);

        PluginGenerator source = PluginGenerator.newInstance(plugin);

        if (!source.contain(PARAMETERS_VALIDATE_RULES)) {
            logger.error("[parameter validate] illegal format without parametersValidateRules");
            throw new ApiPlaneException("illegal format without parametersValidateRules");
        }
        PluginGenerator pluginGenerator = transformDataToYamlBuilder(source);
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(pluginGenerator.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    /**
     * @param source
     * @return 构建yaml配置的builder对象
     */
    private PluginGenerator transformDataToYamlBuilder(PluginGenerator source) {
        PluginGenerator builder = PluginGenerator.newInstance("{\"parameter_validate_rules\": []}");
        List<Map<String, String>> parametersValidateRules = source.getValue(PARAMETERS_VALIDATE_RULES, List.class);
        if (CollectionUtils.isEmpty(parametersValidateRules)) return builder;
        //参数名不能重复
        long parametersNameDistinctCount = parametersValidateRules.stream().map(rule -> rule.get(PARAMETERS_NAME)).distinct().count();
        long parametersNameCount = parametersValidateRules.stream().map(rule -> rule.get(PARAMETERS_NAME)).count();
        if (parametersNameDistinctCount < parametersNameCount) {
            throw new ApiPlaneException("Parameter names must be unique");
        }
        parametersValidateRules.forEach(rule -> {
            PluginGenerator ruleBuilder = PluginGenerator.newInstance("{\"from\":{}}");
            ruleBuilder.createOrUpdateValue("$", FROM, rule.get(PARAMETERS_FROM));
            ruleBuilder.createOrUpdateValue("$", NAME, rule.get(PARAMETERS_NAME));
            ruleBuilder.createOrUpdateValue("$", MUST, rule.get(REQUIRED));
            ruleBuilder.createOrUpdateJson("$", RULE, "{}");
            String parameterType = rule.get("parameterType");
            switch (parameterType) {
                case "String":
                    stringValidate(ruleBuilder, rule);
                    break;
                case "Integer":
                    integerValidate(ruleBuilder, rule);
                    break;
                case "Float":
                    floatValidate(ruleBuilder, rule);
                    break;
                case "Boolean":
                    ruleBuilder.createOrUpdateJson("$", RULE, "{\"bool_validate\":{}}");
                    break;
                case "Array":
                    arrayValidate(ruleBuilder, rule);
                    break;
                default:
                    break;
            }
            builder.addJsonElement(PARAMETER_VALIDATE_RULES, ruleBuilder.jsonString());
            if (rule.containsKey("enumArray")) {
                String enumArrayJson = ResourceGenerator.obj2json(rule.get("enumArray"));
                List<String> enumList = ResourceGenerator.json2obj(enumArrayJson, List.class);

                if (CollectionUtils.isNotEmpty(enumList)) {
                    PluginGenerator enumRuleBuilder = PluginGenerator.newInstance("{\"from\":{}}");
                    enumRuleBuilder.createOrUpdateValue("$", FROM, rule.get(PARAMETERS_FROM));
                    enumRuleBuilder.createOrUpdateValue("$", NAME, rule.get(PARAMETERS_NAME));
                    enumRuleBuilder.createOrUpdateValue("$", MUST, rule.get(REQUIRED));
                    enumRuleBuilder.createOrUpdateJson("$", RULE, "{\"enum_validate\":{}}");
                    enumRuleBuilder.createOrUpdateValue("$.rule.enum_validate", "values", enumList);
                    builder.addJsonElement(PARAMETER_VALIDATE_RULES, enumRuleBuilder.jsonString());
                }

            }
        });
        return builder;
    }

    private PluginGenerator stringValidate(PluginGenerator ruleBuilder, Map<String, String> rule) {
        String regexStr = rule.get("regexStr");
        ruleBuilder.createOrUpdateJson("$.rule", STRING_VALIDATE, "{}");
        if (StringUtils.isNotBlank(regexStr)) {
            ruleBuilder.createOrUpdateJson(RULE_STRING_VALIDATE, "pattern", "{\"google_re2\":{}}");
            ruleBuilder.createOrUpdateValue(RULE_STRING_VALIDATE_PATTERN, "regex", regexStr);
        }
        if (rule.get(MIN_STRING_LENGTH) != null && rule.get(MAX_STRING_LENGTH) != null && Integer.parseInt(String.valueOf(rule.get(MIN_STRING_LENGTH))) > Integer.parseInt(String.valueOf(rule.get(MAX_STRING_LENGTH)))) {
            throw new ApiPlaneException(EXCEPTION);
        }
        ruleBuilder.createOrUpdateJson(RULE_STRING_VALIDATE, "length", "{}");
        //有值填值 否则输入最小值
        if (rule.get(MIN_STRING_LENGTH) != null) {
            ruleBuilder.createOrUpdateValue(RULE_STRING_VALIDATE_LENGTH, MIN, rule.get(MIN_STRING_LENGTH));
        } else {
            ruleBuilder.createOrUpdateValue(RULE_STRING_VALIDATE_LENGTH, MIN, 0);
        }
        //有值填值 否则输入最大值
        if (rule.get(MAX_STRING_LENGTH) != null) {
            ruleBuilder.createOrUpdateValue(RULE_STRING_VALIDATE_LENGTH, MAX, rule.get(MAX_STRING_LENGTH));
        } else {
            ruleBuilder.createOrUpdateValue(RULE_STRING_VALIDATE_LENGTH, MAX, Integer.MAX_VALUE);
        }
        return ruleBuilder;
    }

    private PluginGenerator integerValidate(PluginGenerator ruleBuilder, Map<String, String> rule) {
        ruleBuilder.createOrUpdateJson("$.rule", INT_VALIDATE, "{}");
        if (rule.get(INTEGER_MIN_VALUE) != null && StringUtils.isNotBlank(rule.get(INTEGER_MIN_VALUE)) &&
                rule.get(INTEGER_MAX_VALUE) != null && StringUtils.isNotBlank(rule.get(INTEGER_MAX_VALUE)) && Integer.parseInt(rule.get(INTEGER_MIN_VALUE)) > Integer.parseInt(rule.get(INTEGER_MAX_VALUE))) {
            throw new ApiPlaneException(EXCEPTION);
        }
        //当有数字时需要补充rage结构
        ruleBuilder.createOrUpdateJson("$.rule.int_validate", "range", "{}");
        //如果有值填值，否则填integer最小值
        if (rule.get(INTEGER_MIN_VALUE) != null && StringUtils.isNotBlank(rule.get(INTEGER_MIN_VALUE))) {
            ruleBuilder.createOrUpdateValue(RULE_INT_VALIDATE_RANGE, MIN, Integer.parseInt(rule.get(INTEGER_MIN_VALUE)));
        } else {
            ruleBuilder.createOrUpdateValue(RULE_INT_VALIDATE_RANGE, MIN, Integer.MIN_VALUE);
        }
        //如果有值填值，否则填integer最大值
        if (rule.get(INTEGER_MAX_VALUE) != null && StringUtils.isNotBlank(rule.get(INTEGER_MAX_VALUE))) {
            ruleBuilder.createOrUpdateValue(RULE_INT_VALIDATE_RANGE, MAX, Integer.parseInt(rule.get(INTEGER_MAX_VALUE)));
        } else {
            ruleBuilder.createOrUpdateValue(RULE_INT_VALIDATE_RANGE, MAX, Integer.MAX_VALUE);
        }
        return ruleBuilder;
    }

    private PluginGenerator floatValidate(PluginGenerator ruleBuilder, Map<String, String> rule) {
        ruleBuilder.createOrUpdateJson("$", RULE, "{\"float_validate\":{}}");
        if (rule.get(FLOAT_MIN_VALUE) != null && rule.get(FLOAT_MAX_VALUE) != null && Double.parseDouble(String.valueOf(rule.get(FLOAT_MIN_VALUE))) > Double.parseDouble(String.valueOf(rule.get(FLOAT_MAX_VALUE)))) {
            throw new ApiPlaneException(EXCEPTION);
        }
        ruleBuilder.createOrUpdateJson("$.rule.float_validate", "range", "{}");
        //如果有值填值，否则填float最小值
        if (rule.get(FLOAT_MIN_VALUE) != null) {
            ruleBuilder.createOrUpdateValue(RULE_FLOAT_VALIDATE_RANGE, MIN, rule.get(FLOAT_MIN_VALUE));
        } else {
            ruleBuilder.createOrUpdateValue(RULE_FLOAT_VALIDATE_RANGE, MIN, Float.MIN_VALUE);
        }
        //如果有值填值，否则填float最大值
        if (rule.get(FLOAT_MAX_VALUE) != null) {
            ruleBuilder.createOrUpdateValue(RULE_FLOAT_VALIDATE_RANGE, MAX, rule.get(FLOAT_MAX_VALUE));
        } else {
            ruleBuilder.createOrUpdateValue(RULE_FLOAT_VALIDATE_RANGE, MAX, Float.MAX_VALUE);
        }
        return ruleBuilder;
    }

    private PluginGenerator arrayValidate(PluginGenerator ruleBuilder, Map<String, String> rule) {
        String arrayType = rule.get("arrayType");
        String validateType = "";
        if (StringUtils.equals("String", arrayType)) {
            validateType = STRING_VALIDATE;
        }
        if (StringUtils.equals("Integer", arrayType)) {
            validateType = INT_VALIDATE;
        }
        if (StringUtils.equals("Float", arrayType)) {
            validateType = FLOAT_VALIDATE;
        }
        if (StringUtils.equals("Boolean", arrayType)) {
            validateType = BOOL_VALIDATE;
        }
        if (StringUtils.isNotBlank(validateType)) {
            String maxArraySize = String.valueOf(rule.get("maxArraySize"));
            String minArraySize = String.valueOf(rule.get("minArraySize"));
            ruleBuilder.createOrUpdateJson("$", RULE, String.format("{\"list_validate\":{\"length\":{\"min\":%d,\"max\":%d},\"element\":{\"rule\":{\"%s\":{}}}}}", Integer.parseInt(maxArraySize), Integer.parseInt(minArraySize), validateType));
        }
        return ruleBuilder;
    }

}
