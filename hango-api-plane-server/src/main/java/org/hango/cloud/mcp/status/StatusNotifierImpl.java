package org.hango.cloud.mcp.status;

import org.hango.cloud.mcp.dao.StatusDao;
import org.hango.cloud.mcp.dao.meta.Status;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/5/6
 **/
public class StatusNotifierImpl implements StatusNotifier {
    private ValueGenerator defaultGenerator;

    private StatusDao statusDao;

    public StatusNotifierImpl(StatusDao statusDao, ValueGenerator defaultGenerator) {
        this.statusDao = statusDao;
        this.defaultGenerator = defaultGenerator;
    }

    @Override
    public void notifyStatus(String key) {
        statusDao.update(new org.hango.cloud.mcp.dao.meta.Status(key, defaultGenerator.generate(key)));
    }

    @Override
    public void notifyStatus(String key, String value) {
        statusDao.update(new org.hango.cloud.mcp.dao.meta.Status(key, value));
    }

    @Override
    public void notifyStatus(String key, ValueGenerator valueGenerator) {
        statusDao.update(new Status(key, valueGenerator.generate(key)));
    }
}
