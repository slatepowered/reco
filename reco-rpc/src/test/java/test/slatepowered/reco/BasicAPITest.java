package test.slatepowered.reco;

import org.junit.jupiter.api.Test;
import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.rmq.RMQProvider;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.reco.rpc.function.Allow;
import slatepowered.reco.serializer.KryoSerializer;

import java.util.concurrent.CompletableFuture;

public class BasicAPITest {

    final String RMQ_HOST = "127.0.0.1";
    final int RMQ_PORT = 5672;
    final String RMQ_USER = "guest";
    final String RMQ_PASSWORD = "guest";
    final String RMQ_VHOST = "/";

    /**
     * The test API.
     */
    public interface API extends RemoteAPI {
        @Allow
        void log(String msg);

        default CompletableFuture<Void> logAsync(String msg) {
            return null;
        }
    }

    private CommunicationProvider<?> connect(String name) {
        return new RMQProvider(name, KryoSerializer.standard())
                .connect(RMQ_HOST, RMQ_PORT, RMQ_USER, RMQ_PASSWORD, RMQ_VHOST, "test");
    }

    @Test
    public void test_BasicAPI() {
        CommunicationProvider<?> providerA = connect("a");
        CommunicationProvider<?> providerB = connect("b");
        RPCManager rpcA = new RPCManager(providerA);
        RPCManager rpcB = new RPCManager(providerB);

        // register implementation on node A
        rpcA.register(new API() {
            @Override
            public void log(String msg) {
                System.out.println("logged remotely: " + msg);
            }
        });

        // test remote calls
        API remoteAPI = rpcB.bindRemote(providerB.channel("a"), API.class);
        remoteAPI.logAsync("Hello").whenComplete((_1, _2) -> System.out.println("Logged `Hello`"));
        remoteAPI.log("World");
        System.out.println("Logged `World`"); // it joins the future in sync RPC methods
    }

}
