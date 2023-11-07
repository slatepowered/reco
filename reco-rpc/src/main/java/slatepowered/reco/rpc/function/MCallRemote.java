package slatepowered.reco.rpc.function;

import lombok.Data;

@Data
public class MCallRemote {

    public static final String NAME = "rpcivk";

    /**
     * The call ID of the exchange.
     */
    final long callId;

    /**
     * The name of the remote function.
     */
    final String name;

    /**
     * The arguments for the call.
     */
    final Object[] args;

}
