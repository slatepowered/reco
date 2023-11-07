package slatepowered.reco;

/**
 * A communication provider which is built on byte streams
 * (binary data), so which needs a {@link Serializer} instance.
 */
public abstract class BinaryCommunicationProvider<C extends ProvidedChannel> extends CommunicationProvider<C> {

    // the serialization engine
    protected final Serializer serializer;

    public BinaryCommunicationProvider(String localName, Serializer serializer) {
        super(localName);
        this.serializer = serializer;
    }

}
