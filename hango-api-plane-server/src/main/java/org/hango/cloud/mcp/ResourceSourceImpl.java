package org.hango.cloud.mcp;

import io.grpc.stub.StreamObserver;
import istio.mcp.v1alpha1.Mcp;
import istio.mcp.v1alpha1.ResourceSourceGrpc;

public class ResourceSourceImpl extends ResourceSourceGrpc.ResourceSourceImplBase {

    private McpResourceWatcher watcher;
    private McpOptions options;


    public ResourceSourceImpl(McpResourceWatcher watcher, McpOptions options) {
        this.watcher = watcher;
        this.options = options;
    }

    @Override
    public StreamObserver<Mcp.RequestResources> establishResourceStream(StreamObserver<Mcp.Resources> resp) {
        Connection conn = new Connection(resp, watcher, options);
        return new StreamObserver<Mcp.RequestResources>() {
            @Override
            public void onNext(Mcp.RequestResources req) {
                conn.processClientRequest(req);
            }

            @Override
            public void onError(Throwable throwable) {
                conn.close(throwable);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
