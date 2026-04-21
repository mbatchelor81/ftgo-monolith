package net.chrisrichardson.ftgo.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AspectJ advice that logs method entry / exit at DEBUG and failures at
 * ERROR for every Spring bean whose class is annotated with
 * {@link org.springframework.stereotype.Service} or
 * {@link org.springframework.stereotype.Repository}.
 *
 * <p>Scope is package-configurable via the {@code ftgo.logging.aspect.*}
 * property namespace (see {@code logback-ftgo.xml} and the
 * {@code ftgo.logging.LoggingAspect} logger). The aspect itself is cheap
 * — it early-returns when the logger is disabled — so services are free
 * to leave it on in production. DEBUG entry/exit can be enabled per
 * package by flipping the logger level.
 *
 * <p>The aspect never logs method <em>arguments</em> because those
 * frequently contain PII, credentials, or free-form user input that the
 * {@link SensitiveDataMaskingConverter} can't reliably scrub without
 * context. Entry/exit only records the method name and elapsed time.
 */
@Aspect
public class LoggingAspect {

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void springService() {
        // Pointcut only, no body.
    }

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void springRepository() {
        // Pointcut only, no body.
    }

    @Around("springService() || springRepository()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        Logger logger = resolveLogger(pjp);
        if (!logger.isDebugEnabled() && !logger.isErrorEnabled()) {
            return pjp.proceed();
        }

        String method = methodName(pjp);
        long start = System.nanoTime();
        if (logger.isDebugEnabled()) {
            logger.debug("-> {}", method);
        }
        try {
            Object result = pjp.proceed();
            if (logger.isDebugEnabled()) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
                logger.debug("<- {} ({} ms)", method, elapsedMs);
            }
            return result;
        } catch (Throwable t) {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            logger.error("!! {} failed after {} ms: {}", method, elapsedMs, t.toString());
            throw t;
        }
    }

    private static Logger resolveLogger(ProceedingJoinPoint pjp) {
        return LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
    }

    private static String methodName(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }
}
