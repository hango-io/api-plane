package org.hango.cloud.cache.extractor;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ResourceInfoExtractorTest {

    private static String type_exact = "exact";
    private static String nsf_default_pattern = "nsf.skiff.netease.com/project";
    private static String nsf_default_pattern2 = "nsf.skiff.netease.com/project-${0}";
    private static String return_from_value = "value";
    private static String target_project ="project";
    private static String position_label = "label";
    private static String expected_project = "project1";
    private static Service service;

    private static String type_pattern = "pattern";
    private static String default_param ="project1";
    private static String type_regex = "regex";
    private static String return_from_key = "key";
    private static String regex_pattern ="(?<=nsf.skiff.netease.com/project-).*";




    @Before
    public void init(){
        Service service1 = new Service();
        Map<String,String> label = new HashMap<>();
        ObjectMeta objectMeta = new ObjectMeta();
        label.put("nsf.skiff.netease.com/project","project1");
        label.put("nsf.skiff.netease.com/project-project1","true");
        objectMeta.setLabels(label);
        service1.setMetadata(objectMeta);
        service = service1;
    }


    @Test
    public void testExtractData() {
        // 从nsf.skiff.netease.com/project:project1获取项目信息
        ResourceExtractorConfigProperties.ExtractorConfig config1 = new ResourceExtractorConfigProperties.ExtractorConfig();
        ResourceExtractorConfigProperties.InternalExtractorConfig keyConfig = new ResourceExtractorConfigProperties.InternalExtractorConfig();
        keyConfig.setType(type_exact);
        keyConfig.setPattern(nsf_default_pattern);
        config1.setKeyConfig(keyConfig);
        config1.setReturnFrom(return_from_value);
        config1.setTarget(target_project);
        config1.setPosition(position_label);

        ResourceInfoExtractor extractor1 = ResourceInfoExtractor.createExtractor(config1);
        String s = extractor1.extractData(service);
        Assert.assertEquals(expected_project,s);



        // 从nsf.skiff.netease.com/project-project1:true获取项目信息
        ResourceExtractorConfigProperties.ExtractorConfig config2 = new ResourceExtractorConfigProperties.ExtractorConfig();
        ResourceExtractorConfigProperties.InternalExtractorConfig keyConfig2 = new ResourceExtractorConfigProperties.InternalExtractorConfig();
        keyConfig2.setType(type_pattern);
        keyConfig2.setPattern(nsf_default_pattern2);
        config2.setKeyConfig(keyConfig2);

        ResourceExtractorConfigProperties.InternalExtractorConfig valueConfig = new ResourceExtractorConfigProperties.InternalExtractorConfig();
        valueConfig.setType(type_regex);
        valueConfig.setPattern(regex_pattern);
        config2.setValueConfig(valueConfig);



        config2.setReturnFrom(return_from_key);
        config2.setTarget(target_project);
        config2.setPosition(position_label);

        ResourceInfoExtractor extractor2 = ResourceInfoExtractor.createExtractor(config2);
        String s2 = extractor2.extractData(service,default_param);
        Assert.assertEquals(expected_project,s2);





    }


}