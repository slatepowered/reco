package slatepowered.reco;

import java.io.Serializable;

public interface MessageLike<T> extends Serializable {

    /**
     * Creates a full message from this
     * message-like.
     *
     * @return The message.
     */
    Message<T> toMessage();

}
