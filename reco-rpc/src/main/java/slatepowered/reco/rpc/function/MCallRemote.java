package slatepowered.reco.rpc.function;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MCallRemote {

    public static final String NAME = "rpcivk";

    /**
     * The call ID of the exchange.
     */
    long callId;

    /**
     * The name of the remote function.
     */
    String name;

    /**
     * The arguments for the call.
     */
    Object[] args;

}
