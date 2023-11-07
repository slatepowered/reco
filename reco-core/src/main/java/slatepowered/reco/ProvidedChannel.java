package slatepowered.reco;

public interface ProvidedChannel extends Channel {

    /**
     * Handles a message as if it
     * were received by the channel directly.
     *
     * @param message The message to handle.
     */
    void received(Message<?> message);

}
