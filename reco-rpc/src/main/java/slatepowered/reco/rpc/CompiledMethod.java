package slatepowered.reco.rpc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import slatepowered.reco.Channel;
import slatepowered.reco.rpc.function.CallExchange;
import slatepowered.reco.rpc.function.RemoteFunction;

import java.lang.reflect.Method;

@RequiredArgsConstructor
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

    protected String getRemoteFunctionName() {
        return method.getClass().getName() + "." + method.getName();
    }

    /**
     * Creates a remote function with a handler
     * for local executions.
     *
     * @return The remote function.
     */
    public abstract RemoteFunction getFunction();

}