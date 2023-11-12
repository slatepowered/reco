package test.slatepowered.reco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.rmq.RMQProvider;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.reco.rpc.event.ObjectEvent;
import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.reco.rpc.function.Allow;
import slatepowered.reco.serializer.KryoSerializer;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class RemoteEventTest {

    final String RMQ_HOST = "127.0.0.1";
    final int RMQ_PORT = 5672;
    final String RMQ_USER = "guest";
    final String RMQ_PASSWORD = "guest";
    final String RMQ_VHOST = "/";

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class MyEvent implements ObjectEvent {
        private String object;
        private String message;

        @Override
        public Object getRemoteObjectUID() {
            return object;
        }
    }

    /**
     * The test API.
     */
    public interface API extends RemoteAPI {
        RemoteEvent<MyEvent> onMyEvent();
    }

    private CommunicationProvider<?> connect(String name) {
        return new RMQProvider(name, KryoSerializer.standard())
                .connect(RMQ_HOST, RMQ_PORT, RMQ_USER, RMQ_PASSWORD, RMQ_VHOST)
                .bind("test");
    }

    @Test
    public void test_RemoteEvents() {
        CommunicationProvider<?> providerA = connect("a");
        CommunicationProvider<?> providerB = connect("b");
        RPCManager rpcA = new RPCManager(providerA);
        RPCManager rpcB = new RPCManager(providerB);

        // register implementation
        rpcA.register(new API() {
            @Override
            public RemoteEvent<MyEvent> onMyEvent() {
                return null;
            }
        });

        // get remote API and register handler
        API api = rpcB.bindRemote(providerB.channel("a"), API.class);
        api.onMyEvent().then(System.out::println);

        // call event remotely
        rpcA.invokeRemoteEvent(API.class, "onMyEvent", new MyEvent("myObject", "Hello World!"));
        rpcA.invokeRemoteEvent(API.class, "onMyEvent", new MyEvent("myObject", "Hello World 2!"));

        try { Thread.sleep(200); }
        catch (Exception e) { e.printStackTrace(); }
    }

}
