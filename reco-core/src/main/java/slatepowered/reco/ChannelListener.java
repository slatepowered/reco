package slatepowered.reco;

import slatepowered.veru.functional.Callback;

public interface ChannelListener {

    /**
     * Get a callback for any message received.
     *
     * @param <P> The payload type.
     * @return The callback.
     */
    <P> Callback<ReceivedMessage<P>> on();

    /**
     * Get a callback for any message received.
     *
     * @param <P> The payload type.
     * @param removeOnComplete Whether to destroy the callback after it's called once.
     * @return The callback.
     */
    <P> Callback<ReceivedMessage<P>> on(boolean removeOnComplete);

    /**
     * Remove the given callback.
     *
     * @param callback The callback.
     */
    void remove(Callback<? extends ReceivedMessage<?>> callback);

    /**
     * Calls a message event.
     *
     * @param message The message.
     */
    void call(ReceivedMessage<?> message);

}
