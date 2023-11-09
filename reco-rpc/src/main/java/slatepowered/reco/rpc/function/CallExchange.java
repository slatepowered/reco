package slatepowered.reco.rpc.function;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a conversation/exchange of information
 * for remote calling.
 */
public class CallExchange {

    public static CallExchange completed(Object o) {
        CallExchange exchange = new CallExchange(0, null);
        exchange.getResponseFuture().complete(o);
        return exchange;
    }

    public static CallExchange completed(Object o, RemoteFunction function) {
        CallExchange exchange = new CallExchange(0, function);
        exchange.getResponseFuture().complete(o);
        return exchange;
    }

    /** The call ID. */
    private final long callId;

    /** The future to await a response. */
    private final CompletableFuture<Object> responseFuture = new CompletableFuture<>();

    /** The function which was called. */
    private final RemoteFunction function;

    public CallExchange(long callId, RemoteFunction function) {
        this.callId = callId;
        this.function = function;
    }

    public long getCallId() {
        return callId;
    }

    public CompletableFuture<Object> getResponseFuture() {
        return responseFuture;
    }

    public RemoteFunction getFunction() {
        return function;
    }

}
