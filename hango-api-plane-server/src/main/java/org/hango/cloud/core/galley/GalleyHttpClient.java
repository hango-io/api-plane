package org.hango.cloud.core.galley;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.admission.AdmissionReview;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static okhttp3.TlsVersion.TLS_1_1;
import static okhttp3.TlsVersion.TLS_1_2;

@Component
public class GalleyHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(GalleyHttpClient.class);

    @Value(value = "${galleyHttpUrl:#{null}}")
    private String galleyHttpUrl;

    @Value(value = "${galleyNamespace:istio-system}")
    private String namespace;

    @Value(value = "${galleyPort:443}")
    private String port;

    @Value(value = "${galleyName:galley}")
    private String name;


    @Autowired
    private KubernetesClient kubernetesClient;

    private OkHttpClient galleyClient = HttpClientUtils.createHttpClient(new ConfigBuilder()
            .withTrustCerts(true)
            .withDisableHostnameVerification(true)
            .withRequestTimeout(5000)
            .withTlsVersions(TLS_1_2, TLS_1_1)
            .build());

    private String getGalleyUrl() {
        if (!StringUtils.isEmpty(galleyHttpUrl)) return galleyHttpUrl;
        List<Service> galleyServices = kubernetesClient.getObjectList(K8sResourceEnum.Service.name(), namespace, ImmutableMap.of("app", name));
        if (CollectionUtils.isEmpty(galleyServices))
            throw new ApiPlaneException(ExceptionConst.GALLEY_SERVICE_NON_EXIST);
        Service service = galleyServices.get(0);
        String ip = service.getSpec().getClusterIP();
        return String.format("https://%s:%s", ip, port);
    }

    public AdmissionReview admitpilot(AdmissionReview review) {
        RequestBody body = RequestBody.create(null, ResourceGenerator.obj2json(review));
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(getGalleyUrl() + "/admitpilot")
                .post(body)
                .build();
        logger.info(request.toString());
        try (Response response = galleyClient.newCall(request).execute()) {
            logger.info(response.toString());
            if (Objects.nonNull(response.body())) {
                String responseBody = response.body().string();
                logger.debug("Response body: \n{}", ResourceGenerator.prettyJson(responseBody));
                return ResourceGenerator.json2obj(responseBody, AdmissionReview.class);
            }
            return null;
        } catch (IOException e) {
            logger.warn(String.format("K8s request failed : %s.", request.toString()), e);
            throw new ApiPlaneException(String.format("K8s request failed : %s.", request.toString()), e);
        }
    }
}
