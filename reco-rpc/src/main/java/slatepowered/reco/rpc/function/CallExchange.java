package slatepowered.reco.rpc.function;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a conversation/exchange of information
 * for remote calling.
 */
public class CallExchange {

    /** The call ID. */
    private final long callId;

    /** The future to await a response. */
    private final CompletableFuture<Object> responseFuture = new CompletableFuture<>();

    public CallExchange(long callId) {
        this.callId = callId;
    }

    public long getCallId() {
        return callId;
    }

    public CompletableFuture<Object> getResponseFuture() {
        return responseFuture;
    }

}
