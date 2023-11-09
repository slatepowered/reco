package slatepowered.reco.rpc.function;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MCallResponse {

    /**
     * The call exchange ID.
     */
    long callId;

    /**
     * Whether the call was successful.
     */
    boolean success;

    /**
     * The return value of the call.
     */
    Object value;

    public static final String NAME = "rpcres";

}
