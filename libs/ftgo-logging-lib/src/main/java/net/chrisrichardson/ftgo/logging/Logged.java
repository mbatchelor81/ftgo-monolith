package net.chrisrichardson.ftgo.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class for automatic entry/exit logging via AOP.
 *
 * When placed on a method, the {@link LoggedAspect} logs method entry
 * with arguments and exit with the return value (or exception).
 *
 * When placed on a class, all public methods are logged.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logged {

    /**
     * Custom operation name. Defaults to {@code ClassName.methodName}.
     */
    String value() default "";
}
