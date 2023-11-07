package slatepowered.reco.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import slatepowered.reco.Serializer;

import java.io.*;
import java.util.function.Supplier;

public class KryoSerializer implements Serializer {

    public static KryoSerializer standard() {
        return new KryoSerializer(() -> {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            return kryo;
        });
    }

    /**
     * The Kryo instance to use.
     */
    private final ThreadLocal<Kryo> kryoLocal;

    KryoSerializer(Supplier<Kryo> kryo) {
        kryoLocal = ThreadLocal.withInitial(kryo);
    }

    @Override
    public Object read(InputStream stream) throws IOException {
        Input input = new Input(stream);
        return kryoLocal.get().readClassAndObject(input);
    }

    @Override
    public void write(OutputStream stream, Object object) throws IOException {
        Output output = new Output(stream);
        kryoLocal.get().writeClassAndObject(output, object);
    }

}
