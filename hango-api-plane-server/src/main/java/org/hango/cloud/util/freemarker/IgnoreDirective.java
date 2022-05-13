package org.hango.cloud.util.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/23
 **/
public class IgnoreDirective implements TemplateDirectiveModel {

    private static final String LIST = "list";

    @Override
    public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

        String ignoreList = null;
        final Iterator iterator = parameters.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String name = (String) entry.getKey();
            final TemplateModel value = (TemplateModel) entry.getValue();

            if (name.equals(LIST))
            {
                ignoreList = ((SimpleScalar) value).getAsString();
            }
            else
            {
                throw new TemplateModelException("Unsupported parameter '" + name + "'");
            }
        }

        final StringWriter writer = new StringWriter();
        body.render(writer);
        final String string = writer.toString();
        final String lineFeed = "\n";
        final boolean containsLineFeed = string.contains(lineFeed) == true;
        final String[] tokens = string.split(lineFeed);

        String[] ignores = ignoreList.split(",");

        for (String token : tokens) {
            for (String ignore : ignores) {
                if (token.contains(ignore + ": ")) {
                    token = token.substring(token.indexOf(ignore) + ignore.length() + 2);
                    break;
                }
            }
            environment.getOut().write(token + (containsLineFeed == true ? lineFeed : ""));
        }
        writer.close();
    }
}
