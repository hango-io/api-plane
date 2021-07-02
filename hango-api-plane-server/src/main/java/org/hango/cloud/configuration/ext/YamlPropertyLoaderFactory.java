package org.hango.cloud.configuration.ext;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

public class YamlPropertyLoaderFactory extends DefaultPropertySourceFactory {
	@Override
	public org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
		return new YamlPropertySourceLoader().load(resource.getResource().getFilename(), resource.getResource(), null);
	}
}
