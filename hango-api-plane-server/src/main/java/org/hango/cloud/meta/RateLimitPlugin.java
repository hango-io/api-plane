package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class RateLimitPlugin {

    private String kind = "mesh-rate-limiting";

    @JsonProperty(value = "limit_by_list")
    private List<Rule> rules = new ArrayList<>();

    public static class Rule {
        @JsonProperty(value = "pre_condition")
        private List<PreCondition> preConditions;

        private String type;

        private String when;

        private String then;

        private Integer day;

        private Integer hour;

        private Integer minute;

        private Integer second;

        public List<PreCondition> getPreConditions() {
            return preConditions;
        }

        public void setPreConditions(List<PreCondition> preConditions) {
            this.preConditions = preConditions;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public Integer getHour() {
            return hour;
        }

        public void setHour(Integer hour) {
            this.hour = hour;
        }

        public Integer getMinute() {
            return minute;
        }

        public void setMinute(Integer minute) {
            this.minute = minute;
        }

        public Integer getSecond() {
            return second;
        }

        public void setSecond(Integer second) {
            this.second = second;
        }

        public String getWhen() {
            return when;
        }

        public void setWhen(String when) {
            this.when = when;
        }

        public String getThen() {
            return then;
        }

        public void setThen(String then) {
            this.then = then;
        }
    }


    public static class PreCondition {

        /**
         * 格式必须为Header[$header]
         */
        @JsonProperty(value = "custom_extractor")
        private String customExtractor;

        /**
         * present, =,  ≈, !≈, !=
         */
        private String operator;

        /**
         * 条件反转,默认false
         */
        private Boolean invert = false;

        @JsonProperty(value = "right_value")
        private String rightValue;

        public String getCustomExtractor() {
            return customExtractor;
        }

        public void setCustomExtractor(String customExtractor) {
            this.customExtractor = customExtractor;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Boolean getInvert() {
            return invert;
        }

        public void setInvert(Boolean invert) {
            this.invert = invert;
        }

        public String getRightValue() {
            return rightValue;
        }

        public void setRightValue(String rightValue) {
            this.rightValue = rightValue;
        }
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
