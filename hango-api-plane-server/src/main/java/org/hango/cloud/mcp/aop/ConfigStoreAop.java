package org.hango.cloud.mcp.aop;

import org.hango.cloud.mcp.status.StatusConst;
import org.hango.cloud.mcp.status.StatusNotifier;
import org.hango.cloud.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.transaction.support.TransactionTemplate;

@Aspect
public class ConfigStoreAop {
    private TransactionTemplate transactionTemplate;
    private StatusNotifier statusNotifier;

    public ConfigStoreAop(TransactionTemplate transactionTemplate, StatusNotifier statusNotifier) {
        this.transactionTemplate = transactionTemplate;
        this.statusNotifier = statusNotifier;
    }

    @Pointcut("this(org.hango.cloud.mcp.McpConfigStore)")
    public void classPointcut() {
    }

    @Pointcut("execution(* update*(..))")
    public void updateMethodPointcut() {
    }

    @Pointcut("execution(* delete*(..))")
    public void deleteMethodPointcut() {
    }

    @Pointcut("classPointcut() && (updateMethodPointcut() || deleteMethodPointcut())")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        try {
            Object result = joinPoint.proceed();
            // 处理RateLimit
            if (args.length == 1 && args[0] instanceof ConfigMap) {
                statusNotifier.notifyStatus(StatusConst.RATELIMIT_VERSION);
            }
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new ApiPlaneException("MCP:An error occur when ConfigStoreAop joinPoint proceed", throwable);
        }
    }
}
