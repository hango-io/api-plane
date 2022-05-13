package org.hango.cloud.mcp;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/22
 **/
public class McpMarshaller {
    private static final Logger logger = LoggerFactory.getLogger(McpMarshaller.class);

    public McpMarshaller(McpOptions options) {
        JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.newBuilder().add(options.getRegisteredDescriptors()).build();
        this.printer = JsonFormat.printer().usingTypeRegistry(registry);
        this.parser = JsonFormat.parser().usingTypeRegistry(registry);
    }

    private final JsonFormat.Printer printer;
    private final JsonFormat.Parser parser;

    public void merge(String json, com.google.protobuf.Message.Builder builder) {
        try {
            parser.merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            logger.error("MCP Marshall: parse error. type:[{}], data:[{}]"
                    , builder.getDescriptorForType().getName(), json, e);
        }
    }

    public String print(com.google.protobuf.Message message) {
        try {
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            logger.error("MCP Marshall: print error. type:[{}}]"
                    , message.getDescriptorForType().getName(), e);
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }
}
