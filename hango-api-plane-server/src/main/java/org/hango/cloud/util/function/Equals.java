package org.hango.cloud.util.function;


@FunctionalInterface
public interface Equals<T> {

    boolean apply(T ot, T nt);

}
