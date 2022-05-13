package org.hango.cloud.util.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * 当前区块没有value的时候，将整个区块删除。
 * e.g
 *
 *
 * a:
 *   b:
 *   c:
 *   d:
 *
 * 以上区块整体删除。
 *
 * a:
 *   b: 1
 *   c:
 *   d:
 *
 * 以上区块有value为1,保留。
 *
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/28
 **/
public class AutoRemoveDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

        final StringWriter writer = new StringWriter();
        body.render(writer);
        final String string = writer.toString();
        final String lineFeed = "\n";
        final String[] tokens = string.split(lineFeed);
        boolean hasValue = false;

        for (String token : tokens) {
            // has value
            if (!token.trim().endsWith(":") && !token.trim().equals("")) hasValue = true;
        }

        if (hasValue) env.getOut().write(string);
    }
}
