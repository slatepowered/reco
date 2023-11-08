package slatepowered.reco;

import slatepowered.veru.functional.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class SimpleChannelListener implements ChannelListener {

    // the callbacks
    List<Callback<ReceivedMessage<?>>> callbacks = new ArrayList<>();
    // the callbacks to remove
    List<Callback<ReceivedMessage<?>>> toRemove = new ArrayList<>();

    @Override
    public Callback<ReceivedMessage<?>> on() {
        return on(false);
    }

    @Override
    public Callback<ReceivedMessage<?>> on(boolean removeOnComplete) {
        Callback<ReceivedMessage<?>> callback = Callback.multi();
        callbacks.add(callback);
        if (removeOnComplete)
            callback.then(__ -> toRemove.add(callback));
        return callback;
    }

    @Override
    public void remove(Callback<? extends ReceivedMessage<?>> callback) {
        callbacks.remove(callback);
    }

    @Override
    public void call(ReceivedMessage<?> message) {
        // call all callbacks
        for (Callback<ReceivedMessage<?>> callback : callbacks) {
            callback.call(message);
        }

        // remove callbacks
        callbacks.removeAll(toRemove);
    }

}
