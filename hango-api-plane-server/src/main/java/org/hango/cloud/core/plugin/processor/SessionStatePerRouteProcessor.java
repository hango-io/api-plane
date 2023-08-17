package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class SessionStatePerRouteProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "SessionStatePerRouteProcessor";
    }

    /**
     * Cookie Name
     */
    private static final String COOKIE_NAME = "$." + "CookieName";

    /**
     * Cookie TTL
     */
    private static final String COOKIE_TTL = "$." + "CookieTTL";
    /**
     * Cookie Path
     */
    private static final String COOKIE_PATH = "$." + "CookiePath";

    private String sessionStatePerRouteContent = "{\n" +
            "  \"stateful_session\": {\n" +
            "    \"session_state\": {\n" +
            "      \"name\": \"envoy.http.stateful_session.cookie\",\n" +
            "      \"typedConfig\": {\n" +
            "        \"@type\": \"type.googleapis.com/envoy.extensions.http.stateful_session.cookie.v3.CookieBasedSessionState\",\n" +
            "        \"cookie\": {\n" +
            "          \"name\": \"%s\",\n" +
            "          \"path\": \"%s\",\n" +
            "          \"ttl\": \"%ds\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        String cookieName = source.getValue(COOKIE_NAME);
        Integer cookieTTL = source.getValue(COOKIE_TTL, Integer.class);
        String cookiePath = source.getValue(COOKIE_PATH);
        String format = String.format(sessionStatePerRouteContent, cookieName, cookiePath, cookieTTL);
        ResourceGenerator builder = ResourceGenerator.newInstance(format);
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setGatewayPluginsFragment(wrapper);
        return fragmentHolder;
    }
}
