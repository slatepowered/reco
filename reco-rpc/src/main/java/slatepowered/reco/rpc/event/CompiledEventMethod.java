package slatepowered.reco.rpc.event;

import slatepowered.reco.Channel;
import slatepowered.reco.rpc.CompiledInterface;
import slatepowered.reco.rpc.CompiledMethod;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.function.CallExchange;
import slatepowered.reco.rpc.function.RemoteFunction;

import java.lang.reflect.Method;

public class CompiledEventMethod extends CompiledMethod {

    /**
     * The remote event object.
     */
    protected final RemoteEvent<?> remoteEvent;

    public CompiledEventMethod(CompiledInterface compiledInterface, Method method, RemoteEvent<?> remoteEvent) {
        super(compiledInterface, method);
        this.remoteEvent = remoteEvent;
    }

    public RemoteEvent<?> getRemoteEvent() {
        return remoteEvent;
    }

    @Override
    public CallExchange proxyCallExchange(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        return CallExchange.completed(proxyCall(manager, channel, instance, args));
    }

    @Override
    public Object proxyCall(RPCManager manager, Channel channel, Object instance, Object[] args) throws Throwable {
        return remoteEvent;
    }

    @Override
    public RemoteFunction getFunction() {
        return null;
    }

}
