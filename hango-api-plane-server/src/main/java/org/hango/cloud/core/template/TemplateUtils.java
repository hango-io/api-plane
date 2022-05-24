package org.hango.cloud.core.template;

import org.hango.cloud.util.exception.ApiPlaneException;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.hango.cloud.core.template.TemplateConst.BLANK_LINE;
import static org.hango.cloud.core.template.TemplateConst.IGNORE_SCHEME;
/**
 * Template工具类
 * 1. 可以将一个Template拆分成多个TemplateWrapper
 * 2. 可以根据Label查找TemplateWrapper
 *
 **/
public class TemplateUtils {

    private static final String YAML_SPLIT = "---";

    private TemplateUtils() {
    }

    public static List<TemplateWrapper> getWrappersWithFilter(Template template, Predicate<TemplateWrapper> filter) {
        List<TemplateWrapper> wrappers = getSpiltWrapper(template);
        return getWrappersWithFilter(wrappers, filter);
    }

    public static List<TemplateWrapper> getWrappersWithFilter(List<TemplateWrapper> wrappers, Predicate<TemplateWrapper> filter) {
        List<TemplateWrapper> ret = new ArrayList<>();
        wrappers.forEach(wrapper -> {
            if (filter.test(wrapper))
                ret.add(wrapper);
        });
        return ret;
    }

    public static TemplateWrapper getWrapperWithFilter(Template template, Predicate<TemplateWrapper> filter) {
        List<TemplateWrapper> wrappers = getSpiltWrapper(template);
        return getWrapperWithFilter(wrappers, filter);
    }

    public static TemplateWrapper getWrapperWithFilter(List<TemplateWrapper> wrappers, Predicate<TemplateWrapper> filter) {
        for (TemplateWrapper wrapper : wrappers) {
            if (filter.test(wrapper)) {
                return wrapper;
            }
        }
        throw new ApiPlaneException("The template does not exist. Please check the parameters");
    }

    public static List<TemplateWrapper> getSpiltWrapper(Template template) {
        List<TemplateWrapper> wrappers = new ArrayList<>();
        List<String> blocks = spilt(template.toString());
        for (String block : blocks) {
            wrappers.add(getWrapper(template.getName(), block, template.getConfiguration()));
        }
        return wrappers;
    }

    public static TemplateWrapper getWrapper(Template template) {
        try {
            return new TemplateWrapper(template);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static TemplateWrapper getWrapper(String name, String source, Configuration configuration) {
        try {
            return new TemplateWrapper(name, source, configuration);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static Template getTemplate(String name, Configuration configuration) {
        try {
            return configuration.getTemplate(name);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    /**
     * 切分context，并且过滤掉空行和非代码block
     */
    public static List<String> spilt(String context) {
        List<String> ret = new ArrayList<>();
        // 过滤空行
        context = filter(context, BLANK_LINE);
        String[] blocks = context.split(YAML_SPLIT);

        for (String block : blocks) {
            if (contain(block, IGNORE_SCHEME) && !"".equals(block)) {
                ret.add(block);
            }
        }
        return ret;
    }

    private static String filter(String templateSource, String... filters) {
        String tmp = templateSource;
        for (String filter : filters) {
            tmp = Pattern.compile(filter).matcher(tmp).replaceAll("");
        }
        return tmp;
    }

    private static boolean contain(String source, String regex) {
        return Pattern.compile(regex).matcher(source).find();
    }
}
