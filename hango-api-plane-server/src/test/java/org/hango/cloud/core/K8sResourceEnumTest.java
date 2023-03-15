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
    public static final String V_1_1_0 = "v1.1.0";
    public static final String V_1_5_0 = "v1.5.0";
    public static final String V_1_6_0 = "v1.6.0";
    public static final String V_1_7_0 = "v1.7.0";
    public static final String V_1_8_0 = "v1.8.0";
    @Test
    public void testSelect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = K8sResourceEnum.class.getDeclaredMethod("select", Collection.class, K8sVersion.class);
        method.setAccessible(true);
        Object result1 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion(V_1_1_0), new K8sVersion(V_1_7_0)), new K8sVersion(V_1_1_0));
        Assert.assertEquals(new K8sVersion(V_1_1_0), result1);
        // 报错
        try {
            Object result2 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion(V_1_5_0), new K8sVersion(V_1_7_0)), new K8sVersion(V_1_1_0));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
        Object result3 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion(V_1_5_0), new K8sVersion(V_1_7_0)), new K8sVersion(V_1_6_0));
        Assert.assertEquals(new K8sVersion(V_1_5_0), result3);
        Object result4 = method.invoke(K8sResourceEnum.Pod, ImmutableList.of(new K8sVersion(V_1_5_0), new K8sVersion(V_1_7_0)), new K8sVersion(V_1_8_0));
        Assert.assertEquals(new K8sVersion(V_1_7_0), result4);
    }
}
