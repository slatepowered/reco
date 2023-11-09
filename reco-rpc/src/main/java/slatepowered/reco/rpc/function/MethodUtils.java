package slatepowered.reco.rpc.function;

import jdk.internal.misc.Unsafe;
import slatepowered.veru.misc.Throwables;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodUtils {

    // the unsafe instance
    static final sun.misc.Unsafe UNSAFE;

    static {
        sun.misc.Unsafe unsafe = null; // temp var

        try {
            // get using reflection
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (sun.misc.Unsafe) field.get(null);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
        }

        // set unsafe
        UNSAFE = unsafe;
    }

    public static sun.misc.Unsafe getUnsafe() {
        return UNSAFE;
    }

    private static final Map<Method, MethodHandle> cachedSpecialHandles = new HashMap<>();
    private static final MethodHandles.Lookup INTERNAL_LOOKUP;

    static {
        try {
            // de-encapsulate if Modules exist
            Class<?> CLASS_Module = Class.forName("java.lang.Module");


        } catch (ClassNotFoundException ignored) {
            // no de-encapsulation required
        } catch (Throwable t) {
            throw new RuntimeException("Failed to de-encapsulate", t);
        }

        try {
            // get lookup
            Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            MethodHandles.publicLookup();
            INTERNAL_LOOKUP = (MethodHandles.Lookup)
                    UNSAFE.getObject(
                            UNSAFE.staticFieldBase(field),
                            UNSAFE.staticFieldOffset(field)
                    );
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public static MethodHandle getSpecialMethodHandle(Method method) {
        MethodHandle handle = cachedSpecialHandles.get(method);
        if (handle == null) {
            try {
                handle = INTERNAL_LOOKUP.unreflectSpecial(method, method.getDeclaringClass());
            } catch (Exception e) {
                Throwables.sneakyThrow(e);
                throw new AssertionError();
            }
        }

        return handle;
    }

    /**
     * Find the implMethod implementing the given base implMethod
     * in the given class if present.
     *
     * @param implementingClass The class supposed to implement the implMethod.
     * @param base The base implMethod.
     * @return The implementation implMethod or null if absent.
     */
    public static Method findImplementation(Class<?> implementingClass, Method base) {
        try {
            return implementingClass.getMethod(base.getName(), base.getParameterTypes());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Invokes the default (non-proxy) method, equivalent of
     * {@link java.lang.reflect.InvocationHandler#invokeDefault(Object, Method, Object...)}
     * in Java 16.
     *
     * @param on The object to invoke it on.
     * @param method The method to invoke.
     * @param args The arguments.
     * @return The return value.
     */
    public static Object invokeDefault(Object on, Method method, Object[] args) {
        try {
            MethodHandle handle = getSpecialMethodHandle(method);
            return handle.invoke(on, args);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            throw new AssertionError();
        }
    }

    public static Object invokeSafe(Object on, MethodHandle handle, Object[] args) {
        try {
            return handle.invoke(on, args);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            throw new AssertionError();
        }
    }

}
