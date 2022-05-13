package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.util.function.Equals;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
public interface k8sResourceOperator<T extends HasMetadata> {

    T merge(T old, T fresh);

    T subtract(T old, String value);

    boolean adapt(String name);

    /**
     * 删除old list中与new list重复的元素，
     * 然后将new list的元素全部加入old list中
     * @param oldL
     * @param newL
     * @param eq
     * @return
     */
    default List mergeList(List oldL, List newL, Equals eq) {
        List result = null;
        if (!CollectionUtils.isEmpty(newL)) {
            if (CollectionUtils.isEmpty(oldL)) {
                return newL;
            } else {
                result = new ArrayList(oldL);
                for (Object no : newL) {
                    for (Object oo : oldL) {
                        if (eq.apply(no, oo)) {
                            result.remove(oo);
                        }
                    }
                }
                result.addAll(newL);
            }
        }
        return result;
    }

    default Map mergeMap(Map oldM, Map newM, Equals eq) {
        Map result = null;
        if (!CollectionUtils.isEmpty(newM)) {
            if (CollectionUtils.isEmpty(oldM)) {
                return newM;
            } else {
                result = new HashMap(oldM);
                for (Object no : newM.keySet()) {
                    for (Object oo : oldM.keySet()) {
                        if (eq.apply(no, oo)) {
                            result.remove(oo);
                        }
                    }
                }
                result.putAll(newM);
            }
        }
        return result;
    }

    boolean isUseless(T t);
}
