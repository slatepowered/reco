package slatepowered.reco;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides communication channels.
 *
 * A remote is an external client or group of clients.
 * An auxiliary channel is a publication channel which is like
 * a more targeted global channel.
 *
 * @param <C> The channel type.
 */
public abstract class CommunicationProvider<C extends ProvidedChannel> extends AbstractChannel {

    /**
     * The communication domain in which an
     * exchange happened/happens.
     */
    // domains by encoded value
    static final Domain[] domainsByEncoded = new Domain[256];
    public enum Domain {

        /** Sent to one target. */
        DIRECT(0),

        /** Publish to multiple targets. */
        PUBLISH(1),

        /** Multiple targets. */
        AUX(2)

        ;

        static void register(Domain domain) {
            domainsByEncoded[domain.encoded] = domain;
        }

        public static Domain getByEncoded(byte b) {
            return domainsByEncoded[b];
        }

        // the encoded byte value
        // to represent this domain
        final byte encoded;

        Domain(int encoded) {
            this((byte) encoded);
        }

        Domain(byte encoded) {
            this.encoded = encoded;
            register(this);
        }

        public byte getEncoded() {
            return encoded;
        }

    }

    /**
     * The remote name for publishing.
     */
    public static final String PUB_REMOTE = "pub";

    /////////////////////////////

    public CommunicationProvider(String localName) {
        this.localName = localName;
    }

    // the logger
    protected final Logger logger = Logger.getLogger(getClass().getSimpleName());

    // the name of the local node
    protected final String localName;

    // channels registered by remote
    protected final HashMap<String, C> channelsByRemote = new HashMap<>();

    /**
     * Optional channel set by an implementation.
     * This will be called whenever a published
     * message is received and can not be mapped
     * to any remotes.
     */
    protected C pubChannel;

    // the all channel
    protected Channel allChannel = new AbstractChannel() {
        @Override public CommunicationProvider<?> provider() { return CommunicationProvider.this; }
        @Override public String remote() { return null; }
        @Override public void send(Message<?> message) { publish(message); }
        @Override public void publish(Message<?> message) { CommunicationProvider.this.publish(message); }
    };

    @Override
    public CommunicationProvider<?> provider() {
        return this;
    }

    /* Channels */

    /**
     * Get an unmodifiable copy of the channels
     * by remote map.
     *
     * @return The map.
     */
    public Map<String, C> getChannelsByRemote() {
        return Collections.unmodifiableMap(channelsByRemote);
    }

    /**
     * Get a channel by remote name.
     *
     * @param remote The remote name.
     * @return The channel or null if absent.
     */
    @SuppressWarnings("unchecked")
    public C getChannelByRemote(String remote) {
        // get publish channel
        if (pubChannel != null)
            if (remote.equals(pubChannel.remote()))
                return pubChannel;
        // get by remote or this
        C channel = channelsByRemote.get(remote);
        if (channel == null)
            return (C) this;
        return channel;
    }

    /**
     * Get all channels registered by remote.
     *
     * @return Collection of channels.
     */
    public Collection<C> getChannels() {
        return channelsByRemote.values();
    }

    /**
     * Register a new channel to this provider.
     *
     * @param channel The channel.
     * @return The channel back.
     */
    public C withChannel(C channel) {
        // put by remote
        channelsByRemote.put(channel.remote(), channel);

        // return
        return channel;
    }

    /**
     * Get the channel used for publishing
     * if present. This can be null if not
     * set by the implementation.
     *
     * @return The channel or null if absent.
     */
    public C publishingChannel() {
        return pubChannel;
    }

    // the implementation should call this
    // to have any incoming messages handled
    // by the listeners
    protected void received(Message<?> message,
                            Domain domain,
                            String queue,
                            String source) {
        if (source.equals(localName))
            return; // this can happen on publication channels

        // set message meta values
        message.setMeta("source", source);
        message.setMeta("queue", queue);
        message.setMeta("domain", domain);

//        System.out.println("[!] comm received src(" + source + ") domain(" + domain + ") name(" + message.name + ") content(" + message.content + ")");

        // call general listener
        super.received(message);

        // check for direct
        if (domain == Domain.DIRECT) {
            // route to channels
            C channel = getChannelByRemote(source);
            if (channel == null) {
                logger.warning("Direct message received from unknown remote '" + source + "'");
                return;
            }

            message.setChannel(channel);

            // call listener
            channel.received(message);
        } else if (domain == Domain.AUX) {
            // route to channels
            C channel = getChannelByRemote(queue);
            if (channel == null) {
                logger.warning("Direct message received from unknown aux channel '" + queue + "'");
                return;
            }

            message.setChannel(channel);

            // call listener
            channel.received(message);
        } else if (domain == Domain.PUBLISH) {
            // call publish channel
            if (pubChannel != null)
                pubChannel.received(message);
        }
    }

    /**
     * Get or create a channel for
     * the specified remote.
     *
     * @param remote The remote.
     * @return The channel.
     * @see CommunicationProvider#getChannelByRemote(String)
     * @see CommunicationProvider#newChannel(String, boolean)
     */
    public C channel(String remote) {
        C channel = channelsByRemote.get(remote);
        if (channel != null)
            return channel;
        return newChannel(remote, false);
    }

    /**
     * Get or create an auxiliary channel for
     * the specified remote.
     *
     * @param remote The remote.
     * @return The channel.
     * @see CommunicationProvider#getChannelByRemote(String)
     * @see CommunicationProvider#newChannel(String, boolean)
     */
    public C auxChannel(String remote) {
        C channel = channelsByRemote.get(remote);
        if (channel != null)
            return channel;
        return newChannel(remote, true);
    }

    /**
     * Create and register a new channel
     * for the specified remote.
     *
     * @param remote The remote.
     * @param aux Whether the channel is auxiliary (is it
     * @return The channel.
     */
    public abstract C newChannel(String remote, boolean aux);

    /**
     * Send a message across the given channel.
     *
     * @param message The message to send.
     * @param channel The channel to send across.
     */
    public abstract void send(Message<?> message, C channel);

    /**
     * Publish a message for all remotes listening.
     *
     * @param message The message to publish.
     */
    @Override
    public abstract void publish(Message<?> message);

    ///////////////////////////

    @Override
    public String remote() {
        return null;
    }

    @Override
    public void send(Message<?> message) {
        // just publish the message
        publish(message);
    }

}
