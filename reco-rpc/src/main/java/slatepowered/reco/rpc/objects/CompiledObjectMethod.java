package slatepowered.reco.rpc.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import slatepowered.reco.Channel;
import slatepowered.reco.rpc.RPCManager;

import java.lang.reflect.Method;

@RequiredArgsConstructor
@Getter
public abstract class CompiledObjectMethod {

    /**
     * The compiled object class.
     */
    protected final CompiledObjectClass objectClass;

    /**
     * The compiled method.
     */
    protected final Method method;

    /**
     * Called when this method is invoked through
     * a remote object proxy.
     *
     * @param manager The RPC manager instance.
     * @param args The arguments.
     * @return The return value.
     */
    public abstract Object proxyCall(RPCManager manager,
                                     Channel channel,
                                     Object instance,
                                     Object apiInstance,
                                     Object uid,
                                     Object[] args) throws Throwable;

}
