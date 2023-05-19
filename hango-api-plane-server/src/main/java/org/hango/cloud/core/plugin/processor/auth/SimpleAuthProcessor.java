package org.hango.cloud.core.plugin.processor.auth;

import org.springframework.stereotype.Component;

@Component
public class SimpleAuthProcessor extends AuthProcessor{
    @Override
    public String getName() {
        return "SimpleAuth";
    }
}
