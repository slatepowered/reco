package slatepowered.reco.rpc.event;

/**
 * An event which can be bound to a remote object
 * and therefore has a remote object UID.
 */
public interface ObjectEventPayload {

    /**
     * Get the UID of the remote object this
     * event is bound to.
     *
     * @return The UID.
     */
    Object getRemoteObjectUID();

}
