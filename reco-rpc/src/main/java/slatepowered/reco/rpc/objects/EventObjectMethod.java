package slatepowered.reco.rpc.objects;

import slatepowered.reco.Channel;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.event.CompiledEventMethod;

import java.lang.reflect.Method;

public final class EventObjectMethod extends CompiledObjectMethod {

    /**
     * The API event method.
     */
    protected final CompiledEventMethod apiMethod;

    public EventObjectMethod(CompiledObjectClass objectClass, Method method, CompiledEventMethod apiMethod) {
        super(objectClass, method);
        this.apiMethod = apiMethod;
    }

    public CompiledEventMethod getApiMethod() {
        return apiMethod;
    }

    @Override
    public Object proxyCall(RPCManager manager, Channel channel, Object uid, Object apiInstance, Object instance, Object[] args) throws Throwable {
        throw new AssertionError("This should not be called");
    }

}
