package org.kaaproject.kaa.server.common.verifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a marker annotation for {@link UserVerifierConfig}.
 * 
 * @author Andrew Shvayka
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) 
public @interface KaaUserVerifierConfig {

}
