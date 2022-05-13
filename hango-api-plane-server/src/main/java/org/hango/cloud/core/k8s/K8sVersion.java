package org.hango.cloud.core.k8s;

import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 标记k8s版本
 */
public class K8sVersion implements Comparable<K8sVersion> {
    // 例如v1.1.0
    private static final Pattern FORMATTER = Pattern.compile("^v(\\d{0,3})\\.(\\d{0,3})\\.(\\d{0,3})$");

    public static final K8sVersion V1_11_0 = new K8sVersion("v1.11.0");
    public static final K8sVersion V1_17_0 = new K8sVersion("v1.17.0");

    private final String version;
    private final int x;
    private final int y;
    private final int z;

    public K8sVersion(String vxyz) {
        if (StringUtils.isEmpty(vxyz)) {
            throw new RuntimeException("version could not be null");
        }
        Matcher matcher = FORMATTER.matcher(vxyz);
        if (!matcher.matches()) {
            throw new RuntimeException(String.format("k8s version :%s be incompatible with the regulaion", vxyz));
        }
        this.version = vxyz;
        this.x = Integer.parseInt(matcher.group(1));
        this.y = Integer.parseInt(matcher.group(2));
        this.z = Integer.parseInt(matcher.group(3));
    }

    public String getVersion() {
        return version;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int compareTo(K8sVersion o) {
        int x = Integer.compare(this.x, o.x);
        if (x != 0) return x;
        int y = Integer.compare(this.y, o.y);
        if (y != 0) return y;
        return Integer.compare(this.z, o.z);
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        K8sVersion that = (K8sVersion) o;
        return x == that.x &&
                y == that.y &&
                z == that.z &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, x, y, z);
    }
}
