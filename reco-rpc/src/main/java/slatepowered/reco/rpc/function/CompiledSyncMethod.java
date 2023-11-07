package slatepowered.reco.rpc.function;

import slatepowered.reco.Channel;
import slatepowered.reco.rpc.CompiledMethod;
import slatepowered.reco.rpc.RPCManager;

import java.lang.reflect.Method;

public class CompiledSyncMethod extends CompiledMethod {

    public CompiledSyncMethod(Method method) {
        super(method);
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
        return new RemoteFunction(getRemoteFunctionName(), method.getParameterTypes(), method.getReturnType());
    }

}
