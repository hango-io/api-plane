package org.hango.cloud.core;

import org.hango.cloud.ApiPlaneApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/26
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiPlaneApplication.class, properties = {"k8s.clusters.default.k8s-api-server=https://1.1.1.1"})
@Import(BaseConfiguration.class)
public class BaseTest {

    @Test
    public void test() {
        //do not remove
    }
}
