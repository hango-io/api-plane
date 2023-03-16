package org.hango.cloud.core;

import org.hango.cloud.ApiPlaneApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiPlaneApplication.class)
@Import(BaseConfiguration.class)
public class BaseTest {

    @Test
    public void test() {
        //do not remove
    }
}
