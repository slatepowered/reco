package slatepowered.reco.rpc.objects;

import slatepowered.reco.Channel;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.event.RemoteEvent;

import java.lang.reflect.Method;

public class EventObjectMethod extends CompiledObjectMethod {

    /**
     * The remote event.
     */
    protected final RemoteEvent<?> remoteEvent;

    public EventObjectMethod(CompiledObjectClass objectClass, Method method, RemoteEvent<?> remoteEvent) {
        super(objectClass, method);
        this.remoteEvent = remoteEvent;
    }

    public RemoteEvent<?> getRemoteEvent() {
        return remoteEvent;
    }

    @Override
    public Object proxyCall(RPCManager manager, Channel channel, Object uid, Object apiInstance, Object instance, Object[] args) throws Throwable {
        return remoteEvent;
    }

}
