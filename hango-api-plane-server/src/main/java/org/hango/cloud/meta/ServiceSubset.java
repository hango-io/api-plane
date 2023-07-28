package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hango.cloud.meta.dto.PortalHealthCheckDTO;
import org.hango.cloud.meta.dto.PortalOutlierDetectionDTO;
import org.hango.cloud.meta.dto.PortalServiceConnectionPoolDTO;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ServiceSubset {

    private String name;

    private Map<String, String> labels;

    private TrafficPolicy trafficPolicy;

    private List<String> staticAddrs;

    /**
     *
     *
     * 服务meta label 数据map
     * mata_type: 服务meta数据类型
     * meta_data: 服务meta数据值，转换为Map
     */
    private Map<String,String> metaLabelMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public TrafficPolicy getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(TrafficPolicy trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public List<String> getStaticAddrs() {
        return staticAddrs;
    }

    public void setStaticAddrs(List<String> staticAddrs) {
        this.staticAddrs = staticAddrs;
    }

    public Map<String, String> getMetaLabelMap() {
        return metaLabelMap;
    }

    public void setMetaLabelMap(Map<String, String> metaLabelMap) {
        this.metaLabelMap = metaLabelMap;
    }

    public static class TrafficPolicy {
        /**
         * 负载均衡策略
         */
        private Service.ServiceLoadBalancer loadBalancer;

        /**
         * 主动健康检查
         */
        private PortalHealthCheckDTO healthCheck;

        /**
         * 异常点检测（被动健康检查）
         */
        private PortalOutlierDetectionDTO outlierDetection;

        /**
         * 连接池
         */
        private PortalServiceConnectionPoolDTO connectionPool;

        public Service.ServiceLoadBalancer getLoadBalancer() {
            return loadBalancer;
        }

        public void setLoadBalancer(Service.ServiceLoadBalancer loadBalancer) {
            this.loadBalancer = loadBalancer;
        }

        public PortalHealthCheckDTO getHealthCheck() {
            return healthCheck;
        }

        public void setHealthCheck(PortalHealthCheckDTO healthCheck) {
            this.healthCheck = healthCheck;
        }

        public PortalOutlierDetectionDTO getOutlierDetection() {
            return outlierDetection;
        }

        public void setOutlierDetection(PortalOutlierDetectionDTO outlierDetection) {
            this.outlierDetection = outlierDetection;
        }

        public PortalServiceConnectionPoolDTO getConnectionPool() {
            return connectionPool;
        }

        public void setConnectionPool(PortalServiceConnectionPoolDTO connectionPool) {
            this.connectionPool = connectionPool;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
