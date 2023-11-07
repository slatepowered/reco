package slatepowered.reco.rpc.objects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a method on a remote object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ObjectMethod {

    /**
     * The name of the method in the API interface.
     *
     * The first parameter of that method must be
     * the key/UID of this object.
     */
    String value() default "";

}
