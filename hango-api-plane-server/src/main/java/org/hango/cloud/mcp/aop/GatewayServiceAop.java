package org.hango.cloud.mcp.aop;

import org.hango.cloud.mcp.status.StatusConst;
import org.hango.cloud.mcp.status.StatusNotifier;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Aspect
public class GatewayServiceAop {
    private TransactionTemplate transactionTemplate;
    private StatusNotifier statusNotifier;

    public GatewayServiceAop(TransactionTemplate transactionTemplate, StatusNotifier statusNotifier) {
        this.transactionTemplate = transactionTemplate;
        this.statusNotifier = statusNotifier;
    }

    @Pointcut("this(org.hango.cloud.service.GatewayService)")
    public void interfacePointcut() {
    }

    @Pointcut("execution(* update*(..))")
    public void updateMethodPointcut() {
    }

    @Pointcut("execution(* delete*(..))")
    public void deleteMethodPointcut() {
    }

    @Pointcut("interfacePointcut() && (updateMethodPointcut() || deleteMethodPointcut())")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                try {
                    Object result = joinPoint.proceed();
                    statusNotifier.notifyStatus(StatusConst.RESOURCES_VERSION);
                    return result;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new ApiPlaneException("MCP:An error occur when GatewayServiceAop joinPoint proceed", throwable);
                }
            }
        });
    }
}
