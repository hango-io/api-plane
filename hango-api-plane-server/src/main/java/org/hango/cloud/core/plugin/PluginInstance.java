package org.hango.cloud.core.plugin;

import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;

public class PluginInstance extends ResourceGenerator {
    protected PluginInstance(Object resource, ResourceType type, EditorContext editorContext) {
        super(resource, type, editorContext);
    }

    public PluginInstance(String json) {
        this(json, ResourceType.JSON, defaultContext);
    }

    public String getKind() {
        return getValue("$.kind");
    }
}
