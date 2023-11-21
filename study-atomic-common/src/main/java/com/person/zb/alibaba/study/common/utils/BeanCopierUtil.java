package com.person.zb.alibaba.study.common.utils;


import com.person.zb.alibaba.study.common.model.LruLinkedHashMap;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * bean属性复制
 *
 * @author : ZhouBin
 * @date :  2021/03/25
 */
public class BeanCopierUtil {

    /**
     * 设置 最大容量 1000个  缓存
     */
    private static Map<String, BeanCopier> mapCaches = new LruLinkedHashMap<>(1000);


    /**
     * 属性值复制
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null) {
            return;
        }
        BeanCopier copier = getBeanCopier(source.getClass(), target.getClass());
        copier.copy(source, target, null);
    }

    /**
     * 属性值复制，返回目标对象
     *
     * @param source 源对象
     * @param target 目标对象类
     */
    public static <T> T copyProperties(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }
        T instance;
        try {
            instance = target.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        copyProperties(source, instance);
        return instance;
    }

    /**
     * 属性值复制，返回目标对象
     *
     * @param source 源对象
     * @param target 目标对象类
     */
    public static <T> T copyProperties(Object source, Class<T> target, Consumer<T> consumer) {
        if (source == null) {
            return null;
        }
        T instance;
        try {
            instance = target.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        copyProperties(source, instance);
        consumer.accept(instance);
        return instance;
    }

    /**
     * 属性值复制，返回目标对象
     *
     * @param source 源对象
     * @param target 目标对象类
     */
    public static <S, T> T copyProperties(S source, Class<T> target, BiConsumer<S, T> consumer) {
        if (source == null) {
            return null;
        }
        T instance;
        try {
            instance = target.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        copyProperties(source, instance);
        consumer.accept(source, instance);
        return instance;
    }

    /**
     * list 复制
     *
     * @param srcList   原集合
     * @param destClass 目标类
     */
    public static <S, D> List<D> copyList(List<S> srcList, Class<D> destClass) {
        List<D> destList = new ArrayList<>();
        if (CollectionUtils.isEmpty(srcList)) {
            return destList;
        }
        Class<?> srcClass = srcList.get(0).getClass();
        BeanCopier copier = getBeanCopier(srcClass, destClass);
        try {
            Constructor<D> constructor = destClass.getDeclaredConstructor();
            for (S s : srcList) {
                D d = constructor.newInstance();
                if (s == null) {
                    continue;
                }
                copier.copy(s, d, null);
                destList.add(d);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return destList;
    }

    /**
     * list 复制
     *
     * @param srcList   原集合
     * @param destClass 目标类
     */
    public static <S, D> List<D> copyList(List<S> srcList, Class<D> destClass, Consumer<D> consumer) {
        List<D> destList = new ArrayList<>();
        if (CollectionUtils.isEmpty(srcList)) {
            return destList;
        }
        Class<?> srcClass = srcList.get(0).getClass();
        BeanCopier copier = getBeanCopier(srcClass, destClass);
        try {
            Constructor<D> constructor = destClass.getDeclaredConstructor();
            for (S s : srcList) {
                D d = constructor.newInstance();
                if (s == null) {
                    continue;
                }
                copier.copy(s, d, null);
                consumer.accept(d);
                destList.add(d);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return destList;
    }

    /**
     * list 复制
     *
     * @param srcList   原集合
     * @param destClass 目标类
     */
    public static <S, D> List<D> copyList(List<S> srcList, Class<D> destClass, BiConsumer<S, D> consumer) {
        List<D> destList = new ArrayList<>();
        if (CollectionUtils.isEmpty(srcList)) {
            return destList;
        }
        Class<?> srcClass = srcList.get(0).getClass();
        BeanCopier copier = getBeanCopier(srcClass, destClass);
        try {
            Constructor<D> constructor = destClass.getDeclaredConstructor();
            for (S s : srcList) {
                D d = constructor.newInstance();
                if (s == null) {
                    continue;
                }
                copier.copy(s, d, null);
                consumer.accept(s, d);
                destList.add(d);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return destList;
    }

    private static BeanCopier getBeanCopier(Class<?> srcClass, Class<?> destClass) {
        String key = srcClass.toString() + "->" + destClass.toString();
        return mapCaches.computeIfAbsent(key, k -> BeanCopier.create(srcClass, destClass, false));
    }


}