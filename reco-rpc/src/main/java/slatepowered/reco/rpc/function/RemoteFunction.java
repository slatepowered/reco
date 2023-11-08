package slatepowered.reco.rpc.function;

import java.util.HashSet;

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
    private HashSet<String> allowedSecurityGroups;

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

    public HashSet<String> getAllowedSecurityGroups() {
        return allowedSecurityGroups;
    }

    public RemoteFunction setAllowedSecurityGroups(HashSet<String> allowedSecurityGroups) {
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
