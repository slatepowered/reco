package slatepowered.reco.rpc;

import lombok.Getter;
import slatepowered.reco.Channel;
import slatepowered.reco.rpc.function.CallExchange;
import slatepowered.reco.rpc.function.RemoteFunction;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

@Getter
public abstract class CompiledMethod {

    /**
     * The compiled interface.
     */
    protected final CompiledInterface compiledInterface;

    /**
     * The method this
     */
    protected final Method method;

    /**
     * The remote function name.
     */
    private String remoteFunctionName;

    protected CompiledMethod(CompiledInterface compiledInterface, Method method) {
        this.compiledInterface = compiledInterface;
        this.method = method;
        this.remoteFunctionName = composeRemoteFunctionName();
    }

    /**
     * Invoked when this method is invoked locally
     * through the interface proxy.
     *
     * @return The return call exchange.
     */
    public abstract CallExchange proxyCallExchange(RPCManager manager,
                                                   Channel channel,
                                                   Object instance,
                                                   Object[] args) throws Throwable;

    /**
     * Invoked when this method is invoked locally
     * through the interface proxy.
     *
     * @return The return value.
     */
    public abstract Object proxyCall(RPCManager manager,
                                     Channel channel,
                                     Object instance,
                                     Object[] args) throws Throwable;

    protected String composeRemoteFunctionName() {
        StringBuilder b = new StringBuilder("[");
        boolean first = true;
        for (Class<?> param : method.getParameterTypes()) {
            if (first) first = false;
            else b.append(", ");

            b.append(param.getName());
        }

        return method.getDeclaringClass().getName() + "." + method.getName() + b;
    }

    public String getRemoteFunctionName() {
        return remoteFunctionName;
    }

    /**
     * Creates a remote function with a handler
     * for local executions.
     *
     * @return The remote function.
     */
    public abstract RemoteFunction getFunction();

}
