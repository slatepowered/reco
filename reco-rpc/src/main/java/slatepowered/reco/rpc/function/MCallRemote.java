package slatepowered.reco.rpc.function;

import lombok.Data;

@Data
public class MCallRemote {

    public static final String NAME = "rpcivk";

    final long callId;
    final String name;
    final Object[] args;

}
