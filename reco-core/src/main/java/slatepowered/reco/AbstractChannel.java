package slatepowered.reco;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Basic implementation of a channel.
 */
public abstract class AbstractChannel implements ProvidedChannel {

    // the channel logger
    protected Logger logger = Logger.getLogger(getClass().getSimpleName());

    // the general channel listener
    final ChannelListener listener = new SimpleChannelListener();
    // the channel listeners by message name
    final HashMap<String, ChannelListener> listenerMap = new HashMap<>();

    @Override
    public ChannelListener listen() {
        return listener;
    }

    @Override
    public ChannelListener listen(String name) {
        return listenerMap.computeIfAbsent(name, __ -> new SimpleChannelListener());
    }

    @Override
    public void received(ReceivedMessage<?> message) {
        // call general listener
        listener.call(message);
        // get channel listener for type
        ChannelListener l = listenerMap.get(message.name);
        if (l != null)
            l.call(message);
    }

}
