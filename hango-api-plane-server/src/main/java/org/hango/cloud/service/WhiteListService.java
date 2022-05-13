package org.hango.cloud.service;

import org.hango.cloud.meta.WhiteList;

/**
 * 几个概念:
 * 1. SourceService: 源服务，访问TargetService的服务
 * 2. TargetService: 目标服务，白名单访问策略在这一端配置
 * 3. User: 访问角色
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public interface WhiteListService {
    void updateService(WhiteList whiteList);

    void removeService(WhiteList whiteList);
}
