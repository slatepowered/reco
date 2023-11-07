package slatepowered.reco.rpc.function;

public class RemoteFunction {

    /** The name of the function. */
    private final String name;

    /** The argument types. */
    private final Class<?>[] argTypes;

    /** The return type. */
    private final Class<?> returnType;

    /** The handler. */
    private FunctionHandler handler;

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
