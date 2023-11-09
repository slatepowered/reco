package slatepowered.reco.rpc.function;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
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
