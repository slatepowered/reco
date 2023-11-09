package slatepowered.reco.rpc.function;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RemoteFunction {

    /** The name of the function. */
    private final String name;

    /** The argument types. */
    private final Class<?>[] argTypes;

    /** The return type. */
    private final Class<?> returnType;

    /** The handler. */
    private FunctionHandler handler;

    /** All security groups allowed. */
    private Set<String> allowedSecurityGroups;

    public RemoteFunction(String name, Class<?>[] argTypes, Class<?> returnType) {
        this.name = name;
        this.argTypes = argTypes;
        this.returnType = returnType;
    }

    public FunctionHandler getHandler() {
        return handler;
    }

    public RemoteFunction setHandler(FunctionHandler handler) {
        this.handler = handler;
        return this;
    }

    public Set<String> getAllowedSecurityGroups() {
        return allowedSecurityGroups == null ? Collections.emptySet() : allowedSecurityGroups;
    }

    public RemoteFunction setAllowedSecurityGroups(Set<String> allowedSecurityGroups) {
        this.allowedSecurityGroups = allowedSecurityGroups;
        return this;
    }

    public String getName() {
        return name;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

}
