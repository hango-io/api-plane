package org.hango.cloud.core.plugin;

public class FragmentWrapper {
    private FragmentTypeEnum fragmentType;
    private String content;

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


    public static class Builder {
        private FragmentTypeEnum fragmentType;
        private String content;


        public Builder withFragmentType(FragmentTypeEnum fragmentType) {
            this.fragmentType = fragmentType;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public FragmentWrapper build() {
            FragmentWrapper wrapper = new FragmentWrapper();
            wrapper.setFragmentType(fragmentType);
            wrapper.setContent(content);
            return wrapper;
        }
    }
}
