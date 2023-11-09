package slatepowered.reco.rpc.event;

import slatepowered.veru.collection.Placement;
import slatepowered.veru.functional.Callback;
import slatepowered.veru.functional.HandlerResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a remote event/callback.
 * todo: event functions
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class RemoteEvent<E> implements Callback<E> {

    public static <T> RemoteEvent<T> simple() {
        return new RemoteEvent<T>() {
            @Override
            public Object getUIDFromPayload(Object o) {
                return null;
            }
        };
    }

    /**
     * The callback to delegate to.
     */
    protected final Callback<E> callback = Callback.multi();

    /**
     * The child remote event objects by UID.
     */
    public final Map<Object, RemoteEvent<?>> byUID = new HashMap<>();

    @SuppressWarnings("unchecked")
    public RemoteEvent<E> byUID(Object uid) {
        return (RemoteEvent<E>) byUID.computeIfAbsent(uid, __ -> RemoteEvent.simple());
    }

    public RemoteEvent<E> byUID(Object uid, RemoteEvent remoteEvent) {
        byUID.put(uid, remoteEvent);
        return this;
    }

    public abstract Object getUIDFromPayload(Object o);

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void call(E value) {
        System.out.println("event called: " + value);
        callback.call(value);

        Object uid = getUIDFromPayload(value);
        if (uid != null) {
            RemoteEvent event = byUID.get(uid);
            if (event != null) {
                event.call(value);
            }
        }
    }

}
