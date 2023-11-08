package slatepowered.reco.rpc.event;

import slatepowered.veru.collection.Placement;
import slatepowered.veru.functional.Callback;
import slatepowered.veru.functional.HandlerResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a remote event/callback.
 * todo: event functions
 */
public class RemoteEvent<E> implements Callback<E> {

    /**
     * The callback to delegate to.
     */
    protected final Callback<E> callback = Callback.multi();

    @Override
    public Callback<E> thenApply(Function<E, HandlerResult> handler) {
        callback.thenApply(handler);
        return this;
    }

    @Override
    public Callback<E> thenApply(Function<E, HandlerResult> handler, Placement placement) {
        callback.thenApply(handler, placement);
        return this;
    }

    @Override
    public CompletableFuture<E> await() {
        return callback.await();
    }

    @Override
    public void call(E value) {
        callback.call(value);
    }

}
