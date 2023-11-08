package slatepowered.reco;

/**
 * A message received from a remote source.
 *
 * @param <T> The content type.
 */
public class ReceivedMessage<T> extends Message<T> {

    public ReceivedMessage(String name) {
        super(name);
    }

    public ReceivedMessage(String name, T content) {
        super(name, content);
    }

    public ReceivedMessage(T content) {
        super(content);
    }

    /**
     * The channel this message was
     * received on.
     */
    protected Channel channel;

    /**
     * The sender/source of this message.
     */
    protected String source;

    /**
     * The domain of the received message.
     */
    protected CommunicationProvider.Domain domain;

    /**
     * Get the channel this message was received on.
     */
    public Channel getChannel() {
        return channel;
    }

    public Message<T> setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    /**
     * Get the sender of this message.
     */
    public String getSource() {
        return source;
    }

    public Message<T> setSource(String source) {
        this.source = source;
        return this;
    }

    /**
     * Get the domain of this message.
     */
    public CommunicationProvider.Domain getDomain() {
        return domain;
    }

    public ReceivedMessage<T> setDomain(CommunicationProvider.Domain domain) {
        this.domain = domain;
        return this;
    }
    
}
