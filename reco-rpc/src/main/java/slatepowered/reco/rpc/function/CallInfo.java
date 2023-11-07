package slatepowered.reco.rpc.function;

import lombok.Data;
import slatepowered.reco.Channel;

/**
 * Records information on a remote call.
 */
@Data
public class CallInfo {

    final boolean isLocal;
    final Channel channel;
    final long callId;

    public static CallInfo local() {
        return new CallInfo(true, null, 0);
    }

    public static CallInfo remote(Channel channel, long callId) {
        return new CallInfo(false, channel, callId);
    }

}
