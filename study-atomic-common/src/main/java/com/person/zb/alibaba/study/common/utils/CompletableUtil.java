package com.person.zb.alibaba.study.common.utils;


import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.person.zb.alibaba.study.common.functional.Fun;
import com.person.zb.alibaba.study.common.functional.FunRtn;
import com.person.zb.alibaba.study.common.functional.Worker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.SupplierWrapper;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 多线程执行工具类
 *
 * @author :
 * @date :  2021-05-21
 */
@Slf4j
public class CompletableUtil {

    public static final String TRACE_ID = "traceId";

    public static ThreadPoolExecutor defaultExecutor;

    private static final ThreadLocal<Object> CACHE_LOCAL = new ThreadLocal<>();

    /**
     * 异步执行单任务
     */
    public static CompletableFuture<Void> runSingleAsync(Worker fun) {
        if (defaultExecutor == null) {
            initExecutor();
        }
        return runSingleAsync(fun, defaultExecutor);
    }

    /**
     * 异步执行单任务, 指定 线程池
     */
    public static CompletableFuture<Void> runSingleAsync(Worker fun, Executor executor) {
        Object cacheObject = getCacheLocal();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        String mastNo = UUID.randomUUID().toString().replaceAll("-", "");
        log.info("开启多线程调用：{}", mastNo);
        return CompletableFuture.runAsync(RunnableWrapper.of(() -> {
            try {
                setContent(contextMap, cacheObject, mastNo);
                fun.doWork();
            } catch (Exception e) {
                log.error("线程执行异常", e);
            } finally {
                MDC.clear();
                remove();
            }
        }), executor);
    }

    /**
     * 多线程异步执行批量任务，等待全部任务执行完
     */
    public static <P> void runAndWaitAsync(List<P> list, Fun<P> fun) {
        if (defaultExecutor == null) {
            initExecutor();
        }
        runAndWaitAsync(list, fun, defaultExecutor);

    }

    /**
     * 多线程异步执行批量任务，等待全部任务执行完
     */
    public static <P> void runAndWaitAsync(List<P> list, Fun<P> fun, Executor executor) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (list.size() == 1) {
            fun.execute(list.get(0));
            return;
        }
        Object cacheObject = getCacheLocal();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        String mastNo = UUID.randomUUID().toString().replaceAll("-", "");
        log.info("开启多线程调用：{}", mastNo);
        try {
            CompletableFuture.allOf(list.stream().map(t -> CompletableFuture.runAsync(RunnableWrapper.of(() -> {
                try {
                    setContent(contextMap, cacheObject, mastNo);
                    fun.execute(t);
                } finally {
                    MDC.clear();
                    remove();
                }
            }), executor)).toArray(CompletableFuture[]::new)).join();
        } catch (Exception e) {
            throw getBaseException(e);
        }

    }

    /**
     * 多线程异步执行批量任务
     */
    public static <P> void runAsync(List<P> list, Fun<P> fun) {
        if (defaultExecutor == null) {
            initExecutor();
        }
        runAsync(list, fun, defaultExecutor);
    }

    /**
     * 多线程异步执行批量任务
     */
    public static <P> void runAsync(List<P> list, Fun<P> fun, Executor executor) {
        Object cacheObject = getCacheLocal();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        String mastNo = UUID.randomUUID().toString().replaceAll("-", "");
        log.info("开启多线程调用：{}", mastNo);
        list.forEach(t -> CompletableFuture.runAsync(RunnableWrapper.of(() -> {
            try {
                setContent(contextMap, cacheObject, mastNo);
                fun.execute(t);
            } catch (Exception e) {
                log.error("线程执行异常 {}", Throwables.getStackTraceAsString(e));
            } finally {
                MDC.clear();
                remove();
            }
        }), executor));
    }

    /**
     * 多线程异步执行批量任务，有返回。 有任何一个报错 都抛异常
     */
    public static <P, R> List<R> supplyAsync(List<P> list, FunRtn<P, R> fun) {
        if (defaultExecutor == null) {
            initExecutor();
        }
        return supplyAsync(list, fun, defaultExecutor);
    }

    /**
     * 多线程异步执行批量任务，有返回。 有任何一个报错 都抛异常
     */
    public static <P, R> List<R> supplyAsync(List<P> list, FunRtn<P, R> fun, Executor executor) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        if (list.size() == 1) {
            return Lists.newArrayList(fun.execute(list.get(0)));
        }
        Object cacheObject = getCacheLocal();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        String mastNo = UUID.randomUUID().toString().replaceAll("-", "");
        log.info("开启多线程调用：{}", mastNo);
        try {
            List<CompletableFuture<R>> futureList = list.stream().map(p -> CompletableFuture.supplyAsync(SupplierWrapper.of(() -> {
                try {
                    setContent(contextMap, cacheObject, mastNo);
                    return fun.execute(p);
                } finally {
                    MDC.clear();
                    remove();
                }
            }), executor)).collect(Collectors.toList());
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            List<R> rtnList = new ArrayList<>();
            for (Future future : futureList) {
                rtnList.add((R) future.get());
            }
            return rtnList;
        } catch (Exception e) {
            throw getBaseException(e);
        }
    }

    public static ThreadPoolExecutor getDefaultExecutor() {
        if (defaultExecutor == null) {
            initExecutor();
        }
        return defaultExecutor;
    }

    private static synchronized void initExecutor() {
        if (defaultExecutor == null) {
            defaultExecutor = new ThreadPoolExecutor(10, 20,
                    60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5000), new ThreadFactoryBuilder()
                    .setNameFormat("Executor-%s").build(), new ThreadPoolExecutor.CallerRunsPolicy());
        }

    }

    /**
     * 主线程 设置传递对象，子线程使用。  使用方法， 必须主动调用 remove()
     */
    public static void setCacheLocal(Object o) {
        if (o != null) {
            CACHE_LOCAL.set(o);
        }

    }

    public static Object getCacheLocal() {
        return CACHE_LOCAL.get();
    }

    public static void remove() {
        CACHE_LOCAL.remove();
    }


    private static void setContent(Map<String, String> contextMap, Object cacheObject, String mastNo) {
        if (contextMap == null) {
            contextMap = new HashMap<>(8);
        }
        if (!contextMap.containsKey(TRACE_ID)) {
            contextMap.put(TRACE_ID, mastNo + "-" + RandomStringUtils.randomAlphanumeric(6));
        }
        MDC.setContextMap(contextMap);
        setCacheLocal(cacheObject);
    }

    public static String getRealMessage(Throwable e) {
        // 如果e不为空，则去掉外层的异常包装
        while (e != null) {
            Throwable cause = e.getCause();
            if (cause == null) {
                return e.getMessage();
            }
            e = cause;
        }
        return "";
    }

    public static RuntimeException getBaseException(Throwable e) {
        // 如果e不为空，则去掉外层的异常包装
        while (e != null) {
            if (e instanceof CompletionException) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    return new RuntimeException(e.getMessage());
                }
                e = cause;
            } else {
                if (e instanceof RuntimeException) {
                    return (RuntimeException) e;
                } else {
                    return new RuntimeException(getRealMessage(e));
                }
            }

        }
        return null;
    }
}
