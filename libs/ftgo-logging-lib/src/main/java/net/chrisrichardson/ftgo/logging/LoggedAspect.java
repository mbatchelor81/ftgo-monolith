package net.chrisrichardson.ftgo.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * AOP aspect that provides automatic entry/exit logging for methods
 * annotated with {@link Logged}.
 */
@Aspect
public class LoggedAspect {

    @Around("@annotation(net.chrisrichardson.ftgo.logging.Logged)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return doLog(joinPoint);
    }

    @Around("@within(net.chrisrichardson.ftgo.logging.Logged) && execution(public * *(..)) && !@annotation(net.chrisrichardson.ftgo.logging.Logged)")
    public Object logClassMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return doLog(joinPoint);
    }

    private Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Logger logger = LoggerFactory.getLogger(signature.getDeclaringType());

        String operationName = resolveOperationName(signature, method);

        if (logger.isDebugEnabled()) {
            logger.debug(">>> {} args={}", operationName, Arrays.toString(joinPoint.getArgs()));
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (logger.isDebugEnabled()) {
                logger.debug("<<< {} returned in {}ms", operationName, elapsed);
            }
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            logger.warn("<<< {} threw {} after {}ms", operationName,
                    t.getClass().getSimpleName(), elapsed);
            throw t;
        }
    }

    private String resolveOperationName(MethodSignature signature, Method method) {
        Logged annotation = method.getAnnotation(Logged.class);
        if (annotation == null) {
            annotation = (Logged) signature.getDeclaringType().getAnnotation(Logged.class);
        }
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        return signature.getDeclaringType().getSimpleName() + "." + method.getName();
    }
}
