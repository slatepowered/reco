package slatepowered.reco.rpc.objects;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import slatepowered.reco.rpc.CompiledMethod;

import java.lang.reflect.Method;

@RequiredArgsConstructor
@Getter
public class CompiledObjectMethod {

    /**
     * The compiled method.
     */
    protected final Method method;

    /**
     * The method in the API it is forwarded to.
     */
    protected final CompiledMethod apiMethod;

}
