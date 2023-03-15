package org.hango.cloud;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by caukie on 2019/8/5.
 */
@RunWith(SpringRunner.class)
public class RouterPluginTest {
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
     * 2，路由插件
     *  1）header：精确匹配，正则匹配，区间匹配（左闭右开），key是否存在，前缀匹配，后缀匹配，取反；
     *  2）与现有条件表达式对齐，一个条件多个变量为与，多个条件之间为或，按顺序依次匹配；
     */

    /**
     *
     * 3，路由插件
     *  1）query params：精确匹配，正则匹配；
     *  2）与现有条件表达式对齐，一个条件多个变量为与，多个条件之间为或；
     *  3）按顺序依次匹配，进行条件分流；
     */

    /**
     *
     * 4，路由插件
     *  1）命中请求后，rewrite；
     *
     */

    /**
     *
     * 5，路由插件
     *  1）命中请求后，按权重分流；
     *  2）条件和权重的执行顺序？
     */

    /**
     *
     * 6，路由插件
     *  1）命中请求后，执行重定向；
     */

    /**
     *
     * 7，路由插件
     *  1）命中请求后，返回指定内容；
     */
}
