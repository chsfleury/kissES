package org.kisses.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Charles Fleury
 * @since 27/10/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Mapping {

  String index();

  String type();

  String indexSettings() default "";

  String typeMapping() default "";

}
