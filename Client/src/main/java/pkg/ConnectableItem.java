package pkg;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IndexAnnotated
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface ConnectableItem {
    /**
     * @return general parameters
     */
    String name();

    int priority() default 0;

    Item type();

    /**
     * @return controller parameters
     */
    String pathFXML() default "";
}