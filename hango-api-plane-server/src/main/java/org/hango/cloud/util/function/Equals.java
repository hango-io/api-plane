package org.hango.cloud.util.function;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/1
 **/
@FunctionalInterface
public interface Equals<T> {

    boolean apply(T ot, T nt);

}
