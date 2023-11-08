package slatepowered.reco.rpc.event;

import lombok.Data;

@Data
public class MCallEvent {

    /**
     * The name of the remote event.
     */
    final String name;

    /**
     * The payload for the call.
     */
    final Object payload;

    public static final String NAME = "rpcive";

}
