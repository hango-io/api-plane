package org.hango.cloud.mcp;

import istio.mcp.nsf.SnapshotOuterClass;
import istio.mcp.v1alpha1.Mcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/10
 **/
public class McpCache implements McpResourceDistributor, McpResourceWatcher {
    private static final Logger logger = LoggerFactory.getLogger(McpCache.class);

    private ExecutorService scheduleThread = Executors.newSingleThreadExecutor();

    private SnapshotOuterClass.Snapshot snapshot;

    private final Set<Connection> connections = new HashSet<>();
    private final Map<String, Set<Connection>> watchMap = new HashMap<>();

    @Override
    public synchronized void watch(Connection connection, String collection) {
        connections.add(connection);
        watchMap.putIfAbsent(collection, new HashSet<>());
        Set<Connection> subscribeConnection = watchMap.get(collection);
        if (subscribeConnection.add(connection) && Objects.nonNull(snapshot)) {
            scheduleThread.execute(() -> distribute(connection, collection, snapshot));
        }
    }

    @Override
    public synchronized void release(Connection connection) {
        connections.remove(connection);
        logger.info("MCP: release connection:{}, remain count:{}", connection, connections.size());
        for (Map.Entry<String, Set<Connection>> entry : watchMap.entrySet()) {
            entry.getValue().remove(connection);
        }
    }

    @Override
    public synchronized void setSnapshot(SnapshotOuterClass.Snapshot snapshot) {
        this.snapshot = snapshot;
        scheduleThread.execute(() -> distribute(snapshot));
    }

    @Override
    public synchronized void clearSnapshot() {
        snapshot = null;
    }

    private void distribute(SnapshotOuterClass.Snapshot snapshot) {
        for (String collection : snapshot.getResourcesMap().keySet()) {
            if (watchMap.containsKey(collection)) {
                for (Connection connection : watchMap.get(collection)) {
                    distribute(connection, collection, snapshot);
                }
            }
        }
    }

    private void distribute(Connection connection, String collection, SnapshotOuterClass.Snapshot snapshot) {
        Mcp.Resources resources = snapshot.getResourcesMap().get(collection);
        if (Objects.nonNull(resources)) {
            WatchResponse response = new WatchResponse(snapshot.getVersion(), resources);
            connection.push(response);
        } else {
            logger.warn("MCP: Distribute: No resources(collection=[{}]) were found in the snapshot(version=[{}])", collection, snapshot.getVersion());
            connection.pushEmpty(collection);
        }
    }
}
