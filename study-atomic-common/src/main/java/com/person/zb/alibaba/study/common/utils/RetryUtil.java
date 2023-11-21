package com.person.zb.alibaba.study.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.person.zb.alibaba.study.common.functional.Fun;
import com.person.zb.alibaba.study.common.functional.WorkRtnFun;
import com.person.zb.alibaba.study.common.functional.Worker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executor;

/**
 * @author :  ZhouBin
 * @date :  2021-01-28
 */
@Slf4j
public class RetryUtil {

    private static final int TIMES = 3;

    private static final int SLEEP_TIME = 100;

    public static <R> R executeRtn(WorkRtnFun<R> fun) {
        return executeRtn(fun, TIMES, SLEEP_TIME);
    }

    public static <R> R executeRtn(WorkRtnFun<R> fun, int excTimes, int sleepTime) {
        int times = Math.max(excTimes, 1);
        int st = sleepTime < 1 ? SLEEP_TIME : sleepTime;
        int num = 0;
        while (true) {
            num++;
            try {
                return fun.doWork();
            } catch (Exception e) {
                exceptionHandle(num, times, st, e);
            }
        }
    }

    public static void execute(Worker fun) {
        execute(fun, TIMES, SLEEP_TIME);
    }

    public static void execute(Worker fun, int excTimes, int sleepTime) {
        int times = Math.max(excTimes, 1);
        int st = sleepTime < 1 ? SLEEP_TIME : sleepTime;
        int num = 0;
        while (true) {
            num++;
            try {
                fun.doWork();
                return;
            } catch (Exception e) {
                exceptionHandle(num, times, st, e);
            }
        }
    }

    private static void exceptionHandle(int num, int times, int sleepTime, Exception e) {
        if (num >= times) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        } else {
            log.info("循环调用 {} ,异常:{}", num, Throwables.getStackTraceAsString(e));
        }
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            log.warn("线程中断异常", e);
        }
    }

    public static <R> R directExecAndAlert(WorkRtnFun<R> fun, String bizDesc, String owner, Object... alertParamArg) {
        try {
            return fun.doWork();
        } catch (Exception e) {
            alert(e, bizDesc, owner, alertParamArg);
            throw e;
        }
    }


    public static <R> R execAndAlert(WorkRtnFun<R> fun, int sleepTime, String bizDesc, String owner, Object... alertParamArg) {
        try {
            return executeRtn(fun, TIMES, sleepTime);
        } catch (Exception e) {
            alert(e, bizDesc, owner, alertParamArg);
            throw e;
        }
    }

    public static void alert(Exception e, String bizDesc, String owner, Object... alertParamArg) {
        if (StringUtils.isNotBlank(bizDesc)) {
            String param = JSONObject.toJSONString(alertParamArg);
            String realMessage = CompletableUtil.getRealMessage(e);
            realMessage = StringUtils.isBlank(realMessage) ? "程序异常" : realMessage;
            log.info("{},告警参数：{},异常告警内容：{}", bizDesc, param, realMessage);
//            AlertManager.sendImportantAlert(owner, realMessage, bizDesc, param, AlertConstant.ALERT_LEVEL_PAY_ATTENTION);
        }
    }

    /**
     * 异步执行并重试，失败告警
     */
    public static void asyncExec(Worker fun, int sleepMillis, Executor executor, String bizDesc, String owner, Object... alertParamArg) {
        CompletableUtil.runSingleAsync(() -> {
            try {
                execute(fun, TIMES, sleepMillis);
            } catch (Exception e) {
                log.warn("异步线程执行异常", e);
                alert(e, bizDesc, owner, alertParamArg);
            }
        }, executor);
    }

    public static void asyncExec(Worker fun, int sleepMillis, Executor executor, Fun<Exception> exceptionFun) {
        CompletableUtil.runSingleAsync(() -> {
            try {
                execute(fun, TIMES, sleepMillis);
            } catch (Exception e) {
                if (exceptionFun != null) {
                    exceptionFun.execute(e);
                } else {
                    log.warn("异步线程执行异常", e);
                }
            }
        }, executor);
    }

}
