package org.hango.cloud.configuration.ext;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.Properties;

/**
 * yaml文件读取类，@PropertySource注解类启动后会逐个读取yaml配置文件
 *
 * @author yutao04
 */
public class YamlPropertyLoaderFactory extends DefaultPropertySourceFactory {
	@Override
	public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
		YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
		factory.setResources(resource.getResource());
		Properties properties = factory.getObject();
		return new PropertiesPropertySource(resource.getResource().getFilename(), properties);
	}
}
