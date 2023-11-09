package slatepowered.reco.rpc.event;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MCallEvent {

    /**
     * The name of the remote event.
     */
    String name;

    /**
     * The payload for the call.
     */
    Object payload;

    public static final String NAME = "rpcive";

}
