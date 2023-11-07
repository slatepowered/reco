package slatepowered.reco;

public interface Channel {

    /**
     * Get the name of the remote node.
     *
     * @return The node.
     */
    String remote();

    /**
     * Sends a direct message to
     * the remote.
     *
     * @param message The message to send.
     */
    void send(Message<?> message);

    /**
     * @see Channel#send(Message)
     */
    default void send(MessageLike<?> messageLike) {
        send(messageLike.toMessage());
    }

    /**
     * Publishes a message using a
     * communication provider.
     *
     * @param message The message to send.
     */
    void publish(Message<?> message);

    /**
     * @see Channel#publish(Message)
     */
    default void publish(MessageLike<?> messageLike) {
        publish(messageLike.toMessage());
    }

    /**
     * Get the general channel listener.
     *
     * @return The listener.
     */
    ChannelListener listen();

    /**
     * Get or create a channel listener
     * for the specified message name
     * or type.
     *
     * @param name The message name.
     * @return The listener.
     */
    ChannelListener listen(String name);

    default ChannelListener listen(Class<? extends NamedMessage> klass) {
        return listen(NamedMessage.getName(klass));
    }

}
