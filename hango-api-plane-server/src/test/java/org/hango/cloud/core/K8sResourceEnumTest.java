package org.hango.cloud.core;

import com.google.common.collect.ImmutableList;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.K8sVersion;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class K8sResourceEnumTest {
    @Test
    public void testSelect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = K8sResourceEnum.class.getDeclaredMethod("select", Collection.class, K8sVersion.class);
        method.setAccessible(true);
        Object result1 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion("v1.1.0"), new K8sVersion("v1.7.0")), new K8sVersion("v1.1.0"));
        Assert.assertEquals(new K8sVersion("v1.1.0"), result1);
        // 报错
        try {
            Object result2 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion("v1.5.0"), new K8sVersion("v1.7.0")), new K8sVersion("v1.1.0"));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
        Object result3 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion("v1.5.0"), new K8sVersion("v1.7.0")), new K8sVersion("v1.6.0"));
        Assert.assertEquals(new K8sVersion("v1.5.0"), result3);
        Object result4 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion("v1.5.0"), new K8sVersion("v1.7.0")), new K8sVersion("v1.8.0"));
        Assert.assertEquals(new K8sVersion("v1.7.0"), result4);
    }
}
