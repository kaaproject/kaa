package org.kaaproject.kaa.server.common.dao.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retry {

    /**
     * The number of retry attempts
     *
     * @return retry attempts
     */
    int times() default 1;

    /**
     * Declare the exception types the retry will be issued on.
     *
     * @return exception types causing a retry
     */
    Class<? extends Exception>[] on();
}
