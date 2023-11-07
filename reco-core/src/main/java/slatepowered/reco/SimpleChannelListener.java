package slatepowered.reco;

import slatepowered.veru.functional.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class SimpleChannelListener implements ChannelListener {

    // the callbacks
    List<Callback<Message<?>>> callbacks = new ArrayList<>();
    // the callbacks to remove
    List<Callback<Message<?>>> toRemove = new ArrayList<>();

    @Override
    public Callback<Message<?>> on() {
        return on(false);
    }

    @Override
    public Callback<Message<?>> on(boolean removeOnComplete) {
        Callback<Message<?>> callback = Callback.multi();
        callbacks.add(callback);
        if (removeOnComplete)
            callback.then((Consumer<Message<?>>) __ -> toRemove.add(callback));
        return callback;
    }

    @Override
    public void remove(Callback<? extends Message<?>> callback) {
        callbacks.remove(callback);
    }

    @Override
    public void call(Message<?> message) {
        // call all callbacks
        for (Callback<Message<?>> callback : callbacks) {
            callback.call(message);
        }

        // remove callbacks
        callbacks.removeAll(toRemove);
    }

}
