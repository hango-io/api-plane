package org.hango.cloud;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by caukie on 2019/8/5.
 */
@RunWith(SpringRunner.class)
public class RequestTransformerPluginTest {
    @Test
    public void baseTest() {
        System.out.println("功能测试环境");
    }

    /**
     * 测试方式：
     * 1，构造请求，下发到开发服务器，对网关进行配置；
     * 2，发起业务请求，检查配置是否正常；
     * 3，注意配置信息的reset；
     *
     * 用例主体对齐envoy：
     * https://g.hz.netease.com/qingzhou/envoy-function-instructions/blob/master/basic-function.md
     */

    /**
     *
     * 请求转换插件
     *  1）header转换：新增，修改，替换，删除等；
     */

    /**
     *
     * 请求转换插件
     *  1）query params转换：新增，修改，替换，删除等；
     */

    /**
     *
     * 请求转换插件
     *  1）body转换：新增，修改，替换，删除等；——高级功能，单个变量替换？完整body替换？
     */
}
