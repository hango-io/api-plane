package org.hango.cloud.core.plugin.processor;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public abstract class AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Autowired
    protected EditorContext editorContext;

    /**
     * https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/route/v3/route_components.proto#envoy-v3-api-field-config-route-v3-headermatcher-string-match
     {
        "name":"%s",
        "string_match":{
            "safe_regex_match":{
                "google_re2":"{}",
                "regex":"%s"
            }
        }
     }
     */
    public String safe_regex_string_match = "{\"name\":\"%s\",\"string_match\":{\"safe_regex\":{\"google_re2\":{},\"regex\":\"%s\"}}}";

    public String exact_string_match = "{\"name\":\"%s\",\"string_match\":{\"exact\": \"%s\"}}";


    public String prefix_string_match = "{\"name\":\"%s\",\"string_match\":{\"prefix\": \"%s\"}}";

    public static final String PREFIX_MATCH = "{\"name\":\"%s\",\"prefix_match\":\"%s\"}";

    public String present_match = "{\"name\":\"%s\", \"present_match\":true}";

    public String present_invert_match = "{\"name\":\"%s\", \"present_match\":true, \"invert_match\":true}";

    public String exact_match = "{\"name\":\"%s\", \"exact_match\":\"%s\"}";

    public String exact_invert_match = "{\"name\":\"%s\", \"exact_match\":\"%s\", \"invert_match\":true}";

    public String regex_match = "{\"name\":\"%s\", \"regex_match\":\"%s\"}";

    public String regex_invert_match = "{\"name\":\"%s\", \"regex_match\":\"%s\", \"invert_match\":true}";

    public String present_match_separate = "{\"name\":\"%s\", \"present_match_separate\":true}";



    protected String getRegexByOp(String op, String value) {
        switch (op) {
            case "exact":
            case "=":
                return String.format("^%s$", escapeExprSpecialWord(value));
            case "!=":
                return String.format("((?!%s).)*", escapeExprSpecialWord(value));
            case "regex":
            case "≈":
                return escapeBackSlash(value);
            case "prefix":
            case "startsWith":
                return String.format("%s.*", escapeExprSpecialWord(value));
            case "endsWith":
                return String.format(".*%s", escapeExprSpecialWord(value));
            case "nonRegex":
            case "!≈":
                return String.format("((?!%s).)*", escapeBackSlash(value));
            default:
                throw new ApiPlaneException("Unsupported op :[" + op + "]");
        }
    }

    /**
     * 将一个普通expression作为regex表达式，转移其中所有特殊字符，并填充到json中时需要转义,
     * 例如 . 转义为 \\\\. 第一个反斜杠转义是不作为正则表达式中的特殊字符.第二个反斜杆是在json中特殊字符需要转义
     *
     * @param keyword
     * @return
     */
    protected String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 将一个正则expression填充到json中时，需要转义\
     *
     * @param keyword
     * @return
     */
    protected String escapeBackSlash(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.replaceAll("\\\\", "\\\\\\\\");
        }
        return keyword;
    }

    protected boolean haveNull(Object... items) {
        for (Object item : items) {
            if (Objects.isNull(item)) {
                return true;
            }
        }
        return false;
    }

    protected boolean nonNull(Object... items) {
        for (Object item : items) {
            if (Objects.isNull(item)) {
                return false;
            }
            if (item instanceof String){
                if (StringUtils.isEmpty((String)item)){
                    return false;
                }
            }
        }
        return true;
    }


}
