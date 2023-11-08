package slatepowered.reco.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simply denotes an API interface as being a possible remote API.
 */
public interface RemoteAPI {

    /**
     * Denotes that a field in a registered handler should be dependency injected.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Inject {

    }

}
