package org.hango.cloud.core.plugin;

import org.hango.cloud.core.k8s.K8sResourceEnum;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/19
 **/
public class FragmentWrapper {
    private K8sResourceEnum resourceType;
    private FragmentTypeEnum fragmentType;
    private String content;
    private String xUserId;

    public K8sResourceEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(K8sResourceEnum resourceType) {
        this.resourceType = resourceType;
    }

    public FragmentTypeEnum getFragmentType() {
        return fragmentType;
    }

    public void setFragmentType(FragmentTypeEnum fragmentType) {
        this.fragmentType = fragmentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getXUserId() {
        return xUserId;
    }

    public void setXUserId(String xUserId) {
        this.xUserId = xUserId;
    }

    public static class Builder {
        private K8sResourceEnum resourceType;
        private FragmentTypeEnum fragmentType;
        private String content;
        private String xUserId;

        public Builder withResourceType(K8sResourceEnum resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder withFragmentType(FragmentTypeEnum fragmentType) {
            this.fragmentType = fragmentType;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withXUserId(String id) {
            this.xUserId = id;
            return this;
        }

        public FragmentWrapper build() {
            FragmentWrapper wrapper = new FragmentWrapper();
            wrapper.setResourceType(resourceType);
            wrapper.setFragmentType(fragmentType);
            wrapper.setContent(content);
            wrapper.setXUserId(xUserId);
            return wrapper;
        }
    }
}
