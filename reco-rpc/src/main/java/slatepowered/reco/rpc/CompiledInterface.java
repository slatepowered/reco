package slatepowered.reco.rpc;

import lombok.Data;
import slatepowered.veru.misc.Throwables;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CompiledInterface {
    final RPCManager rpcManager;                                   // The RPC manager
    final Class<?> klass;                                          // The interface which was compiled
    final List<CompiledMethod> methods = new ArrayList<>();        // The compiled methods in this interface
    final Map<Method, CompiledMethod> methodMap = new HashMap<>(); // The compiled methods by reflection method

    public void register(CompiledMethod function) {
        methods.add(function);
        methodMap.put(function.method, function);
    }

    public CompiledMethod findMethodByName(String name) {
        for (CompiledMethod method : methods) {
            if (method.getMethod().getName().equals(name)) {
                return method;
            }
        }

        // find and compile method
        for (Method method : klass.getMethods()) {
            if (method.getName().equals(name)) {
                try {
                    CompiledMethod compiledMethod = rpcManager.compileMethod(this, method);
                    register(compiledMethod);
                    return compiledMethod;
                } catch (Throwable t) {
                    Throwables.sneakyThrow(t);
                }
            }
        }

        return null;
    }

}
