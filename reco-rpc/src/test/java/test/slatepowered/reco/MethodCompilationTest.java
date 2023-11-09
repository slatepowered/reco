package test.slatepowered.reco;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import slatepowered.reco.rpc.CompiledInterface;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.reco.rpc.function.CompiledAsyncMethod;
import slatepowered.reco.rpc.function.CompiledSyncMethod;

import java.util.concurrent.CompletableFuture;

public class MethodCompilationTest {

    /**
     * The test API.
     */
    interface API extends RemoteAPI {
        void log(String msg);
        CompletableFuture<Void> logAsync(String msg);
    }

    @Test
    void test_BasicInterface() throws Throwable {
        RPCManager rpcManager = new RPCManager(null);
        CompiledInterface compiledInterface = rpcManager.compileInterface(API.class);

        Assertions.assertTrue(compiledInterface.getMethodMap().containsKey(API.class.getMethod("log", String.class)));
        Assertions.assertTrue(compiledInterface.getMethodMap().get(API.class.getMethod("log", String.class)) instanceof CompiledSyncMethod);
        Assertions.assertTrue(compiledInterface.getMethodMap().containsKey(API.class.getMethod("logAsync", String.class)));
        Assertions.assertTrue(compiledInterface.getMethodMap().get(API.class.getMethod("logAsync", String.class)) instanceof CompiledAsyncMethod);
    }

}
