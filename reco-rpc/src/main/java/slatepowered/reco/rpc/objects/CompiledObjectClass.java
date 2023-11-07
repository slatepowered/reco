package slatepowered.reco.rpc.objects;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class CompiledObjectClass {

    protected final Class<?> klass;                                                // The interface which was compiled
    protected final Map<Method, CompiledObjectMethod> methodMap = new HashMap<>(); // The compiled methods by reflection method
    protected Class<?> uidType;
    protected Method uidMethod;

}
