package test.slatepowered.reco;

import slatepowered.reco.rpc.function.MethodUtils;

public class DeEncapsulationTest {

    public static void main(String[] args) throws Throwable {
        MethodUtils.getSpecialMethodHandle(DeEncapsulationTest.class.getMethod("a"));
    }

    public void a() {

    }

}
