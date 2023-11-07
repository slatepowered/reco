package slatepowered.reco;

import slatepowered.veru.misc.Throwables;

import java.util.HashMap;
import java.util.Map;

/**
 * A message which can be sent and received on
 * a channel. This class is final because subclasses
 * can't be reliably deserialized.
 *
 * @param <T> The type of the payload.
 */
public final class Message<T> implements MessageLike<T> {

    @SuppressWarnings("unchecked")
    static <T> T throwR(Throwable t) {
        Throwables.sneakyThrow(t);
        return (T) new Object();
    }

    ///////////////////////////////

    @Override
    public Message<T> toMessage() {
        return this;
    }

    public Message(String name) {
        this.name     = name;
        this.nameHash = name.hashCode();
    }

    public Message(String name,
                   T content) {
        this(name);
        payload(content);
    }

    public Message(T content) {
        this(content instanceof NamedMessage ? ((NamedMessage)content).messageName() : Message.
                throwR(new IllegalArgumentException("Content is not a NameProvider")));
        payload(content);
    }

    /**
     * The channel this message was
     * received on.
     */
    protected Channel channel;

    /**
     * The optional name.
     * This is null if the message was received.
     */
    protected String name;

    /**
     * The hash code of the message name.
     */
    protected final int nameHash;

    public int nameHash() {
        return nameHash;
    }

    /**
     * Get the optional name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the channel this message was received on.
     */
    public Channel getChannel() {
        return channel;
    }

    public Message setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    // the object payload
    // nullable
    T content;

    // the meta values
    Map<String, Object> meta;

    @SuppressWarnings("unchecked")
    public <T2> T2 getMeta(String key) {
        if (meta == null) return null;
        return (T2) meta.get(key);
    }

    public Message<T> setMeta(String key, Object value) {
        if (meta == null) meta = new HashMap<>();
        meta.put(key, value);
        return this;
    }

    public boolean hasMeta(String key) {
        return meta != null && meta.containsKey(key);
    }

    public Message<T> payload(T content) {
        this.content = content;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T payload() {
        return (T) content;
    }

    @Override
    public String toString() {
        return "Message(" + name + " " + content + ")";
    }

}
