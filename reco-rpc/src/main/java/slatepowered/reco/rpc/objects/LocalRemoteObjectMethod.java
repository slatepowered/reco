package slatepowered.reco.rpc.objects;

import slatepowered.reco.Channel;
import slatepowered.reco.rpc.CompiledInterface;
import slatepowered.reco.rpc.CompiledMethod;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.function.CallExchange;
import slatepowered.reco.rpc.function.RemoteFunction;

import java.lang.reflect.Method;

public class LocalRemoteObjectMethod extends CompiledMethod {

    /**
     * The compiled object class.
     */
    protected final CompiledObjectClass objectClass;

    public LocalRemoteObjectMethod(CompiledInterface compiledInterface, Method method, CompiledObjectClass objectClass) {
        super(compiledInterface, method);
        this.objectClass = objectClass;
    }

    @Override
    public CallExchange proxyCallExchange(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        return CallExchange.completed(proxyCall(manager, channel, instance, args));
    }

    @Override
    public Object proxyCall(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        return manager.instantiateRemoteObject(objectClass, compiledInterface, channel, instance, args[0]);
    }

    @Override
    public RemoteFunction getFunction() {
        return null;
    }

}
