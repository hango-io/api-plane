package org.hango.cloud.meta.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import java.util.Arrays;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/6/20
 **/
public class ObjectMetadataFilter extends SimpleBeanPropertyFilter {

    private static final List<String> MAINTAIN_FILEDS = Arrays.asList("name", "namespace");

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (!isReserve(writer)) return;
        super.serializeAsField(pojo, jgen, provider, writer);
    }

    private boolean isReserve(PropertyWriter writer) {
        return MAINTAIN_FILEDS.contains(writer.getName());
    }
}
