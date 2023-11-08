package slatepowered.reco;

import slatepowered.veru.misc.Throwables;

import java.util.HashMap;
import java.util.Map;

/**
 * A message which can be sent and received on
 * a channel.
 *
 * @param <T> The type of the payload.
 */
public class Message<T> implements MessageLike<T> {

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
        this.name = name;
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
     * The name of the message.
     */
    protected String name;

    /**
     * The payload value which is nullable.
     */
    protected T content;

    /**
     * The metadata values (these are not serialized with the message).
     */
    protected Map<String, Object> meta;

    /**
     * Get the optional name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a meta value from this message.
     *
     * @param key The key.
     * @param <T2> The return value type.
     * @return The metadata value.
     */
    @SuppressWarnings("unchecked")
    public <T2> T2 getMeta(String key) {
        if (meta == null) return null;
        return (T2) meta.get(key);
    }

    /**
     * Set a meta value on this message.
     *
     * @param key The key.
     * @param value The value.
     * @return This.
     */
    public Message<T> setMeta(String key, Object value) {
        if (meta == null) meta = new HashMap<>();
        meta.put(key, value);
        return this;
    }

    /**
     * Check whether this message contains the given
     * metadata key.
     *
     * @param key The key.
     * @return Whether it contains it.
     */
    public boolean hasMeta(String key) {
        return meta != null && meta.containsKey(key);
    }

    /**
     * Set the payload for this message.
     *
     * @param content The payload.
     * @return This.
     */
    public Message<T> payload(T content) {
        this.content = content;
        return this;
    }

    /**
     * Get the payload on this message.
     *
     * @param <T2> The casted return type.
     * @return The payload or null.
     */
    @SuppressWarnings("unchecked")
    public <T2 extends T> T2 payload() {
        return (T2) content;
    }

    @Override
    public String toString() {
        return "Message(" + name + " " + content + ")";
    }

}
