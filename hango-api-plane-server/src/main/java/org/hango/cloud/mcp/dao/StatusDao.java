package org.hango.cloud.mcp.dao;

import org.hango.cloud.mcp.dao.meta.Status;

import java.util.List;

/**
 * status表的dao
 * 只允许修改value，不允许新增或删除
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public interface StatusDao {
    String get(String name);

    void update(Status status);

    List<Status> list();
}