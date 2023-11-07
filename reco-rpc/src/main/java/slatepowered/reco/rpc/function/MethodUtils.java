package slatepowered.reco.rpc.function;

import slatepowered.veru.misc.Throwables;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodUtils {

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

    private static final Constructor<MethodHandles.Lookup> classLookupConstructor;
    private static final Map<Class<?>, MethodHandles.Lookup> cachedClassLookups = new HashMap<>();
    private static final Map<Method, MethodHandle> cachedSpecialHandles = new HashMap<>();

    static {
        try {
            classLookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
            classLookupConstructor.setAccessible(true);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodHandles.Lookup getClassLookup(Class<?> klass) {
        // check cache
        MethodHandles.Lookup lookup = cachedClassLookups.get(klass);
        if (lookup == null) {
            try {
                lookup = classLookupConstructor.newInstance(klass)
                        .in(klass);
                cachedClassLookups.put(klass, lookup);
            } catch (Exception e) {
                Throwables.sneakyThrow(e);
                throw new AssertionError();
            }
        }

        return lookup;
    }

    public static MethodHandle getSpecialMethodHandle(Method method) {
        MethodHandle handle = cachedSpecialHandles.get(method);
        if (handle == null) {
            try {
                MethodHandles.Lookup lookup = getClassLookup(method.getDeclaringClass());
                handle = lookup.unreflectSpecial(method, method.getDeclaringClass());
            } catch (Exception e) {
                Throwables.sneakyThrow(e);
                throw new AssertionError();
            }
        }

        return handle;
    }

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
