package org.hango.cloud.util.function;

/**
 * merge 两个k8s资源
 * @param <T>
 */
@FunctionalInterface
public interface Merger<T> {

    T merge(T old, T latest);
}
