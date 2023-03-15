package org.hango.cloud.core.k8s.http;

import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.utils.URLUtils;
import okhttp3.*;
import okio.Buffer;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ResourceConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DefaultK8sHttpClient implements K8sHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultK8sHttpClient.class);

    protected static final MediaType JSON = MediaType.parse("application/json");
    protected static final String CLIENT_STATUS_FLAG = "CLIENT_STATUS_FLAG";

    protected Config config;
    protected OkHttpClient httpClient;
    protected EditorContext editorContext;

    private int response_body_limit = 3000;

    public DefaultK8sHttpClient(Config config, OkHttpClient httpClient, EditorContext editorContext) {
        this.config = config;
        this.httpClient = httpClient;
        this.editorContext = editorContext;
    }


    protected void assertResponseCode(Request request, Response response) {
        int statusCode = response.code();
        String customMessage = config.getErrorMessages().get(statusCode);

        if (response.isSuccessful()) {
            return;
        } else if (customMessage != null) {
            throw requestFailure(request, createStatus(statusCode, customMessage));
        } else {
            throw requestFailure(request, createStatus(response));
        }
    }

    protected String handleResponse(Request.Builder requestBuilder) {
        return handleResponse(requestBuilder, (request, response) -> assertResponseCode(request, response), null);
    }

    protected String handleResponse(Request.Builder requestBuilder, boolean silent) {
        return handleResponse(requestBuilder, (request, response) -> assertResponseCode(request, response), null, silent);
    }

    protected String handleResponse(Request.Builder requestBuilder, BiConsumer<Request, Response> responseConsumer, Function<Response, String> responseMapper) {
        return handleResponse(requestBuilder, responseConsumer, responseMapper, true);
    }

    protected String handleResponse(Request.Builder requestBuilder, BiConsumer<Request, Response> responseConsumer, Function<Response, String> responseMapper, boolean silent) {
        Request request = requestBuilder.build();
        if (Objects.nonNull(request)) {
            logger.info(request.toString());
        }
        Buffer buffer = new Buffer();
        try {
            if (Objects.nonNull(request.body()) && request.body().contentLength() != 0) {
                request.body().writeTo(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Response response = httpClient.newCall(request).execute()) {
            if (Objects.nonNull(responseConsumer)) responseConsumer.accept(request, response);
            if (!silent) {
                logger.info(response.toString());
            }
            if (Objects.nonNull(responseMapper)) return responseMapper.apply(response);
            if (Objects.nonNull(response.body())) {
                return response.body().string();
            }
            return null;
        } catch (IOException e) {
            logger.warn("",e);
            throw new ApiPlaneException(String.format("K8s request failed : %s.", request.toString()), e);
        }
    }


    protected Status createStatus(String statusMessage) {
        ResourceGenerator generator = ResourceGenerator.newInstance(statusMessage, ResourceType.JSON, editorContext);
        return generator.object(Status.class);
    }

    protected Status createStatus(Response response) {
        String statusMessage = "";
        ResponseBody body = response != null ? response.body() : null;
        int statusCode = response != null ? response.code() : 0;
        try {
            if (response == null) {
                statusMessage = "No response";
            } else if (body != null) {
                statusMessage = body.string();
            } else if (response.message() != null) {
                statusMessage = response.message();
            }
            Status status = createStatus(statusMessage);
            if (status.getCode() == null) {
                status = new StatusBuilder(status).withCode(statusCode).build();
            }
            return status;
        } catch (IOException e) {
            return createStatus(statusCode, statusMessage);
        }
    }

    protected Status createStatus(int statusCode, String message) {
        Status status = new StatusBuilder()
                .withCode(statusCode)
                .withMessage(message)
                .build();
        status.getAdditionalProperties().put(CLIENT_STATUS_FLAG, "true");
        return status;
    }

    protected ApiPlaneException requestFailure(Request request, Status status) {
        StringBuilder sb = new StringBuilder();
        sb.append("Failure executing: ").append(request.method())
                .append(" at: ").append(request.url().toString()).append(".");

        if (status.getMessage() != null && !status.getMessage().isEmpty()) {
            sb.append(" Message: ").append(status.getMessage()).append(".");
        }

        if (!status.getAdditionalProperties().containsKey(CLIENT_STATUS_FLAG)) {
            sb.append(" Received status: ").append(status).append(".");
        }
        logger.warn(sb.toString());
        return new ApiPlaneException(
                StringUtils.isEmpty(status.getMessage()) ? sb.toString() : status.getMessage(),
                status.getCode());
    }

    public String getMasterUrl() {
        return config.getMasterUrl();
    }

    public String getUrl(String kind, String namespace) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        return resourceEnum.selfLink(config.getMasterUrl(), namespace);
    }

    public String getUrl(String kind, String namespace, String name) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        return URLUtils.pathJoin(resourceEnum.selfLink(config.getMasterUrl(), namespace), name);
    }

    public String getUrlWithLabels(String url, Map<String, String> labels) {
        if (!CollectionUtils.isEmpty(labels)) {
            StringBuilder sb = new StringBuilder("labelSelector=");
            List<String> kvs = new ArrayList<>();
            labels.forEach((k, v) -> kvs.add(k + "%3D" + v));
            sb.append(String.join(",", kvs));
            return url + "?" + sb.toString();
        }
        return url;
    }

    public String getUrlWithLabels(String kind, String namespace, Map<String, String> labels) {
        String url = getUrl(kind, namespace);
        return getUrlWithLabels(url, labels);
    }

    public String getUrlWithLabels(String kind, String namespace, String name, Map<String, String> labels) {
        String url = getUrl(kind, name, namespace);
        return getUrlWithLabels(url, labels);
    }

    @Override
    public String getWithNull(String url) {
        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        return handleResponse(requestBuilder, null, resp -> {
            try {
                if (!resp.isSuccessful()) return null;
                if (Objects.nonNull(resp.body())) {
                    return resp.body().string();
                }
                return null;
            } catch (IOException e) {
                throw new ApiPlaneException(e.getMessage(), e);
            }
        });
    }

    @Override
    public String get(String url) {
        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        return handleResponse(requestBuilder, false);
    }

    @Override
    public String getInSilent(String url) {
        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        return handleResponse(requestBuilder, true);
    }

    @Override
    public String put(String url, String resource) {
        RequestBody body = RequestBody.create(JSON, resource);
        Request.Builder requestBuilder = new Request.Builder().put(body).url(url);
        return handleResponse(requestBuilder, (request, response) -> {
            int statusCode = response.code();
            if (statusCode == HttpStatus.CONFLICT.value()) {
                throw new ResourceConflictException(String.format("Resource put %s conflict.", url));
            }
            assertResponseCode(request, response);
        }, null);
    }

    @Override
    public String post(String url, String resource) {
        RequestBody body = RequestBody.create(JSON, resource);
        Request.Builder requestBuilder = new Request.Builder().post(body).url(url);
        return handleResponse(requestBuilder);
    }

    @Override
    public String delete(String url) {
        Request.Builder requestBuilder = new Request.Builder().delete().url(url);
        return handleResponse(requestBuilder);
    }
}
