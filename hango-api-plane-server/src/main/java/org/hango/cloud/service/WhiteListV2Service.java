package org.hango.cloud.service;

import org.hango.cloud.meta.dto.WhiteListV2AuthRuleDto;

import java.util.List;

public interface WhiteListV2Service {

    void updateServiceAuth(String service, Boolean authOn, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList);

    void createOrUpdateAuthRule(String service, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList);

    void deleteAuthRule(String service, String ruleName, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList);
}
