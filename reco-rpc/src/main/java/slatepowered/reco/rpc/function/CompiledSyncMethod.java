package slatepowered.reco.rpc.function;

import slatepowered.reco.Channel;
import slatepowered.reco.rpc.CompiledInterface;
import slatepowered.reco.rpc.CompiledMethod;
import slatepowered.reco.rpc.RPCManager;

import java.lang.reflect.Method;

public class CompiledSyncMethod extends CompiledMethod {

    /**
     * The remote function.
     */
    protected final RemoteFunction function;

    public CompiledSyncMethod(CompiledInterface compiledInterface, Method method) {
        super(compiledInterface, method);

        this.function = new RemoteFunction(getRemoteFunctionName(), method.getParameterTypes(), method.getReturnType());
    }

    @Override
    public CallExchange proxyCallExchange(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        return manager.callExchange(getFunction(), channel, args);
    }

    @Override
    public Object proxyCall(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        // call remote function
        return proxyCallExchange(manager, channel, instance, args).getResponseFuture().join();
    }

    @Override
    public RemoteFunction getFunction() {
        return function;
    }

}
