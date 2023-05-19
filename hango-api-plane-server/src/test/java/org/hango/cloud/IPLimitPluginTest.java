package org.hango.cloud;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by caukie on 2019/8/5.
 */
@RunWith(SpringRunner.class)
public class IPLimitPluginTest {
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
     * 2，IP黑白名单插件
     *  1）
     */
}
