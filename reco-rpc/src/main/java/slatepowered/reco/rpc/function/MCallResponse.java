package slatepowered.reco.rpc.function;

import lombok.Data;

@Data
public class MCallResponse {

    /**
     * The call exchange ID.
     */
    final long callId;

    /**
     * Whether the call was successful.
     */
    final boolean success;

    /**
     * The return value of the call.
     */
    final Object value;

    public static final String NAME = "rpcres";

}
