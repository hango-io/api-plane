package org.hango.cloud.util.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;


public class IndentationDirective implements TemplateDirectiveModel {

    private static final String COUNT = "count";

    @Override
    public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        Integer count = 0;
        final Iterator iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String name = (String) entry.getKey();
            final TemplateModel value = (TemplateModel) entry.getValue();

            if (name.equals(COUNT)) {
                if (value instanceof TemplateNumberModel == false) {
                    throw new TemplateModelException("The \"" + COUNT + "\" parameter " + "must be a number");
                }
                count = ((TemplateNumberModel) value).getAsNumber().intValue();
                if (count < 0) {
                    throw new TemplateModelException("The \"" + COUNT + "\" parameter " + "cannot be negative");
                }
            } else {
                throw new TemplateModelException("Unsupported parameter '" + name + "'");
            }
        }

        Integer column = environment.getCurrentDirectiveCallPlace().getBeginColumn();
        final String indentation = StringUtils.repeat(' ', count + column - 1);
        final StringWriter writer = new StringWriter();
        body.render(writer);
        final String string = writer.toString();

        if (StringUtils.isEmpty(string)) return;

        final String lineFeed = string.contains("\n") ? "\n" : "";
        final String[] tokens = string.split(lineFeed);

        for (int i = 0; i < tokens.length; i++) {
            String indent = indentation;
            if (i == 0) {
                indent = StringUtils.repeat(' ', count);
            }

            environment.getOut().write(indent + tokens[i] + lineFeed);
        }
        writer.close();
    }
}
