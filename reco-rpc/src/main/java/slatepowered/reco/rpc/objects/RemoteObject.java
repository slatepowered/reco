package slatepowered.reco.rpc.objects;

import slatepowered.reco.rpc.RemoteAPI;

/**
 * Implemented to denote a remote API object.
 *
 * Remote objects are locally associated/stores as one value,
 * which is used as the key/UID. Each method called on these
 * objects are forwarded to the remote API with the key/UID value
 * and the arguments.
 */
public interface RemoteObject<A extends RemoteAPI> {

    /**
     * @return The remote API.
     */
    A api();

}
