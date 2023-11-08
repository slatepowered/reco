package slatepowered.reco.rpc.objects;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import slatepowered.reco.Channel;
import slatepowered.reco.rpc.CompiledMethod;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.veru.collection.ArrayUtil;

import java.lang.reflect.Method;

public class ForwardingObjectMethod extends CompiledObjectMethod {

    /**
     * The method in the API it is to be forwarded to.
     */
    protected final CompiledMethod apiMethod;

    public ForwardingObjectMethod(CompiledObjectClass objectClass, Method method, CompiledMethod apiMethod) {
        super(objectClass, method);
        this.apiMethod = apiMethod;
    }

    public CompiledMethod getApiMethod() {
        return apiMethod;
    }

    @Override
    public Object proxyCall(RPCManager manager, Channel channel, Object uid, Object apiInstance, Object instance, Object[] args) throws Throwable {
        return apiMethod.proxyCall(manager, channel, apiInstance, ArrayUtil.concat(new Object[]{ uid }, args));
    }

}
