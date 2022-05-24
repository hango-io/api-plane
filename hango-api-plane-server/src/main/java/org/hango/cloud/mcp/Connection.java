package org.hango.cloud.mcp;

import io.grpc.stub.StreamObserver;
import istio.mcp.v1alpha1.Mcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private final StreamObserver<Mcp.Resources> stream;
    private final McpResourceWatcher watcher;
    private final McpOptions mcpOptions;

    public Connection(StreamObserver<Mcp.Resources> stream, McpResourceWatcher watcher, McpOptions mcpOptions) {
        this.stream = stream;
        this.watcher = watcher;
        this.mcpOptions = mcpOptions;
    }

    public StreamObserver<Mcp.Resources> getStream() {
        return stream;
    }

    public void processClientRequest(Mcp.RequestResources req) {
        if (McpUtils.isTriggerResponse(req)) return;
        String collection = req.getCollection();
        if (StringUtils.isEmpty(req.getResponseNonce())) {
            logger.info("MCP: connection={} inc={} watch for {}", this.hashCode(), req.getIncremental(), collection);
            if (!McpUtils.isSupportedCollection(mcpOptions.getSnapshotCollections(), collection)) {
                pushEmpty(collection);
            } else {
                watcher.watch(this, collection);
            }
        } else {
            if (req.hasErrorDetail()) {
                // NACK Response
                logger.warn("MCP: connection={} NACK collection={} with nonce={} error={} inc={}", // nolint: lll
                        this.hashCode(), collection, req.getResponseNonce(), req.getErrorDetail().getMessage(), req.getIncremental());
            } else {
                // ASK Response
                logger.info("MCP: connection={} ACK collection={} with nonce={} inc={}",
                        this.hashCode(), collection, req.getResponseNonce(), req.getIncremental());
            }
        }
    }

    public void push(WatchResponse resp) {
        Mcp.Resources msg = Mcp.Resources.newBuilder(resp.getResource())
                .setSystemVersionInfo(String.format("Snapshot:[%s],Resource:[%s]", resp.getSnapshotVersion(), resp.getResource().getSystemVersionInfo()))
                .setIncremental(false)
                .setNonce(String.format("Snapshot:[%s]", resp.getSnapshotVersion()))
                .build();

        logger.debug("MCP: connection {}: SEND collection={} version={} nonce={} inc={}",
                this, msg.getCollection(), msg.getSystemVersionInfo(), msg.getNonce(), msg.getIncremental());
        try {
            getStream().onNext(msg);
        } catch (Exception e) {
            logger.warn("MCP: connection {} an error occurs when SEND collection={} version={} nonce={} inc={}, err={}",
                    this, msg.getCollection(), msg.getSystemVersionInfo(), msg.getNonce(), msg.getIncremental(), e.getMessage());
            this.close(e);
        }
    }

    public void pushEmpty(String collection) {
        Mcp.Resources resources = Mcp.Resources.newBuilder().setCollection(collection).build();
        WatchResponse response = new WatchResponse("", resources);
        push(response);
    }


    public void close(Throwable throwable) {
        throwable.printStackTrace();
        logger.info("MCP: close connection {}", this.hashCode());
        try {
            getStream().onError(throwable);
        } catch (Exception e) {
            logger.warn("MCP: connection {} an error occurs when CLOSE connection", this.hashCode());
        } finally {
            watcher.release(this);
        }
    }
}
