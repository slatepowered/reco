package slatepowered.reco.rpc;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CompiledInterface {

    final Class<?> klass;                                          // The interface which was compiled
    final List<CompiledMethod> methods = new ArrayList<>();        // The compiled methods in this interface
    final Map<Method, CompiledMethod> methodMap = new HashMap<>(); // The compiled methods by reflection method

    public void register(CompiledMethod function) {
        methods.add(function);
        methodMap.put(function.method, function);
    }

}
