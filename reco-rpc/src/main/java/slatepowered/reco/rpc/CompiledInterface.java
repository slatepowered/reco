package slatepowered.reco.rpc;

import lombok.Data;
import slatepowered.reco.rpc.function.RemoteFunction;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Data
public class CompiledInterface {

    final Class<?> klass;
    final List<FuncMethod> methods;
    final Map<Method, FuncMethod> methodMap;

    // represents a method-function pair
    // in a compiled interface
    @Data
    public static class FuncMethod {

        final Method method;
        final RemoteFunction function;
        final boolean isAsync;
        final FuncMethod sync;

        public RemoteFunction syncFunction() {
            if (isAsync && sync != null)
                return sync.syncFunction();
            return function;
        }
    }

}
