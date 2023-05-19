package org.hango.cloud.core.plugin.processor.auth;

import org.springframework.stereotype.Component;

@Component
public class Oauth2AuthProcessor extends AuthProcessor{
    @Override
    public String getName() {
        return "Oauth2Auth";
    }
}
