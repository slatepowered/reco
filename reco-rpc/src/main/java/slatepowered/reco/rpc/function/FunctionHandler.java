package slatepowered.reco.rpc.function;

import slatepowered.reco.rpc.RPCManager;

import java.util.function.Supplier;

@FunctionalInterface
public interface FunctionHandler {

    Object call(RPCManager manager,
                Supplier<CallInfo> callInfoSupplier,
                Object... args);

}
