package org.hango.cloud.meta;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("java:S115")
public enum UriMatch {

    exact,
    prefix,
    regex,
    ;

    public static UriMatch get(String type) {
        if (StringUtils.isEmpty(type)) throw new IllegalArgumentException();
        for (UriMatch uriMatch : values()) {
            if (uriMatch.name().equalsIgnoreCase(type)) {
                return uriMatch;
            }
        }
        throw new NullPointerException();
    }
}
