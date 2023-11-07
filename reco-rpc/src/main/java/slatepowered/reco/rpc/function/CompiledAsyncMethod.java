package slatepowered.reco.rpc.function;

import slatepowered.reco.Channel;
import slatepowered.reco.rpc.CompiledMethod;
import slatepowered.reco.rpc.RPCManager;

import java.lang.reflect.Method;

public class CompiledAsyncMethod extends CompiledMethod {

    /**
     * The synchronous method.
     */
    protected final CompiledMethod syncMethod;

    public CompiledAsyncMethod(Method method, CompiledMethod syncMethod) {
        super(method);
        this.syncMethod = syncMethod;
    }

    @Override
    public CallExchange proxyCallExchange(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        return syncMethod.proxyCallExchange(manager, channel, instance, args);
    }

    @Override
    public Object proxyCall(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        return proxyCallExchange(manager, channel, instance, args).getResponseFuture();
    }

    @Override
    public RemoteFunction getFunction() {
        return null;
    }

}
