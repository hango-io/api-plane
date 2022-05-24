package org.hango.cloud.mcp.status;

import org.hango.cloud.mcp.dao.StatusDao;

import java.util.ArrayList;
import java.util.List;

public class StatusProductorImpl implements StatusProductor {
    private StatusDao dao;

    public StatusProductorImpl(StatusDao dao) {
        this.dao = dao;
    }

    @Override
    public Status product() {
        List<org.hango.cloud.mcp.dao.meta.Status> statuses = dao.list();
        List<Status.Property> properties = new ArrayList<>();
        statuses.forEach(item -> {
            properties.add(new Status.Property(item.getName(), item.getValue()));
        });
        return new Status(properties.toArray(new Status.Property[0]));
    }
}
