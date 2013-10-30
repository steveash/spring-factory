package com.github.steveash.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for documentation purposes.  This will not be picked up during component scanning because
 * the factory that creates the prototype instances will get picked up and that will automatically register the
 * prototype bean definition
 *
 * @author Steve Ash
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PrototypeComponent {
}
