package slatepowered.reco.rpc.function;

import lombok.Data;

@Data
public class MCallResponse {

    final long callId;
    final boolean success;
    final Object value;

    public static final String NAME = "rpcres";

}
