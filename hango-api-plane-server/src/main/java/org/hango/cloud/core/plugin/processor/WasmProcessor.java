package org.hango.cloud.core.plugin.processor;

import org.springframework.stereotype.Component;

/**
 * @Author zhufengwei
 * @Date 2023/8/9
 */
@Component
public class WasmProcessor extends LuaProcessor{

    @Override
    public String getName() {
        return "WasmProcessor";
    }
}
