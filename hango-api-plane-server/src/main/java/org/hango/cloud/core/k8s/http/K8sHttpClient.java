package org.hango.cloud.core.k8s.http;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/31
 **/
public interface K8sHttpClient {

    String getWithNull(String url);

    String get(String url);

    /**
     * 基本等于get, 不打印response
     * @param url
     * @return
     */
    String getInSilent(String url);

    String put(String url, String resource);

    String post(String url, String resource);

    String delete(String url);
}
