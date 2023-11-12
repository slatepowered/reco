package slatepowered.reco.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.instantiator.basic.ConstructorInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import slatepowered.reco.Serializer;

import java.io.*;
import java.util.function.Supplier;

public class KryoSerializer implements Serializer {

    public static KryoSerializer standard() {
        return new KryoSerializer(() -> {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);

            DefaultInstantiatorStrategy instantiatorStrategy = new DefaultInstantiatorStrategy();
            kryo.setInstantiatorStrategy(instantiatorStrategy);

            FieldSerializer.FieldSerializerConfig fieldSerializerConfig = new FieldSerializer.FieldSerializerConfig();
            fieldSerializerConfig.setIgnoreSyntheticFields(false);
            kryo.setDefaultSerializer(new SerializerFactory.FieldSerializerFactory(fieldSerializerConfig));

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
        output.flush();
    }

}
