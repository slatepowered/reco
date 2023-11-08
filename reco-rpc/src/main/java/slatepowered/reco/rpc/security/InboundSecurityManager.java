package slatepowered.reco.rpc.security;

import slatepowered.reco.Message;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.function.MCallRemote;

/**
 * Handles inbound calls from remote sources.
 */
public abstract class InboundSecurityManager {

    /**
     * Check whether the given inbound call is allowed.
     *
     * @param manager The RPC manager.
     * @param message The call message.
     * @param securityGroups The security groups for the source node.
     * @return Whether it is allowed.
     */
    public abstract boolean checkInboundCall(RPCManager manager,
                                             Message<MCallRemote> message,
                                             String[] securityGroups);

    /**
     * Find the security groups for the given source node
     * in the given context.
     *
     * @param manager The RPC manager.
     * @param message The call message.
     * @return The security groups.
     */
    public abstract String[] getSecurityGroups(RPCManager manager,
                                               Message<MCallRemote> message);

}
