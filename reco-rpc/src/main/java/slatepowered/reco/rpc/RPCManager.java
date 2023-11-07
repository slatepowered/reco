package slatepowered.reco.rpc;

import slatepowered.reco.Channel;
import slatepowered.reco.Message;
import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.reco.rpc.function.*;
import slatepowered.reco.rpc.objects.*;
import slatepowered.veru.collection.ArrayUtil;
import slatepowered.veru.misc.Throwables;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RPCManager {

    private static final Logger LOGGER = Logger.getLogger("RemoteManager");

    /** The local network channel. */
    private final Channel localChannel;

    /** The registered functions. */
    private final Map<String, RemoteFunction> functionMap = new HashMap<>();

    /** The outgoing call exchanges. */
    private final ConcurrentHashMap<Long, CallExchange> outgoingCalls = new ConcurrentHashMap<>();

    /** Registered compiled interfaces. */
    private final Map<Class<?>, CompiledInterface> compiledInterfaceMap = new HashMap<>();

    /** Cached compiled methods. */
    private final Map<Method, CompiledMethod> compiledMethodCache = new HashMap<>();

    /** The method compilation hooks. */
    private final List<BiFunction<CompiledInterface, Method, CompiledMethod>> methodCompilerHooks = new ArrayList<>();

    /** Cached compiled object classes. */
    private final Map<Class<?>, CompiledObjectClass> compiledObjectClassMap = new HashMap<>();

    public RPCManager(Channel localChannel) {
        this.localChannel = localChannel;
    }

    /**
     * Start listening for requests.
     */
    public void start() {
        try {
            /* Listen for remote call. */
            localChannel.listen(MCallRemote.NAME)
                    .on().then((message -> {
                        MCallRemote call = message.payload();
                        long callId = call.getCallId();
                        Channel channel = message.getChannel();

                        RemoteFunction function = getFunction(call.getName());

                        // call implMethod locally
                        Object ret; boolean success;
                        try {
                            if (function == null) {
                                throw new UnsupportedFunctionException("Unknown function(" + call.getName() + ")");
                            }

                            ret = callLocalOnRq(() -> CallInfo.remote(channel, callId),
                                    function,
                                    call.getArgs());
                            success = true;
                        } catch (Throwable t) {
                            ret = t;
                            success = false;

                            // log error
                            LOGGER.warning("Error while executing remote function");
                            LOGGER.warning("  function(" + function.getName() + ") channel(" + channel + ")");
                            t.printStackTrace();
                        }

                        // send response
                        channel.send(new Message<>(MCallRemote.NAME).payload(new MCallResponse(callId, success, ret)));
                    })
            );

            /* Listen for call response. */
            localChannel.listen(MCallResponse.NAME)
                    .on().then((message -> {
                        MCallResponse response = message.payload();
                        long callId = response.getCallId();
                        boolean success = response.isSuccess();
                        Object value = response.getValue();

                        CallExchange exchange = outgoingCalls.remove(callId);
                        if (exchange == null) {
                            LOGGER.warning("Received response for non-existent exchange callId(" + callId + ")");
                            return;
                        }

                        CompletableFuture<Object> responseFuture = exchange.getResponseFuture();

                        if (!success) {
                            // create exception
                            RemoteException e;
                            if (value instanceof Throwable) e = new RemoteException((Throwable) value);
                            else if (value instanceof String) e = new RemoteException((String) value);
                            else e = new RemoteException();

                            responseFuture.completeExceptionally(e);
                        } else {
                            responseFuture.complete(value);
                        }
                    })
            );
        } catch (Exception e) {
            throw new IllegalStateException("RPCManager initialization failed", e);
        }
    }

    public RPCManager addMethodCompilationHook(BiFunction<CompiledInterface, Method, CompiledMethod> hook) {
        methodCompilerHooks.add(hook);
        return this;
    }

    /**
     * Get a function by name.
     *
     * @param name The name.
     * @return The function or null if absent.
     */
    public RemoteFunction getFunction(String name) {
        return functionMap.get(name);
    }

    /**
     * Register a new function.
     *
     * @param function The function.
     * @return This.
     */
    public RPCManager register(RemoteFunction function) {
        if (function == null)
            return this;
        functionMap.put(function.getName(), function);
        return this;
    }

    private long nextCallId() {
        return System.currentTimeMillis() ^ System.nanoTime();
    }

    private CallExchange createExchange() {
        CallExchange e = new CallExchange(nextCallId());
        outgoingCalls.put(e.getCallId(), e);
        return e;
    }

    /**
     * Calls the given function on the given channel
     * with the provided arguments passed. This creates an
     * outgoing exchange which will exist until a response.
     *
     * If a function completes successfully, the completable future
     * will complete normally with the returned value. If the functions
     * return value is void, the value will always be null.
     *
     * If a function is unable to complete successfully on the remote,
     * the response future will be completed exceptionally with a
     * {@link RemoteException}.
     *
     * @param function The function.
     * @param channel The channel.
     * @param args The parameters/arguments.
     * @return The call exchange.
     */
    @SuppressWarnings("unchecked")
    public CallExchange callExchange(
            RemoteFunction function,
            Channel channel,
            Object... args) {
        CallExchange exchange = createExchange();

        // create and send call message
        MCallRemote m = new MCallRemote(exchange.getCallId(), function.getName(), args);
        channel.send(new Message<>(MCallRemote.NAME).payload(m));

        return exchange;
    }

    /**
     * Calls the given function on the given channel
     * with the provided arguments passed. This creates an
     * outgoing exchange which will exist until a response.
     *
     * If a function completes successfully, the completable future
     * will complete normally with the returned value. If the functions
     * return value is void, the value will always be null.
     *
     * If a function is unable to complete successfully on the remote,
     * the response future will be completed exceptionally with a
     * {@link RemoteException}.
     *
     * @param function The function.
     * @param channel The channel.
     * @param args The parameters/arguments.
     * @param <T> The return type.
     * @return The response future.
     */
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> callRemote(
            RemoteFunction function,
            Channel channel,
            Object... args) {
        return (CompletableFuture<T>) callExchange(function, channel, args).getResponseFuture();
    }

    // implMethod which handles locally calling methods
    // issued by a remote source
    private Object callLocalOnRq(
            Supplier<CallInfo> callInfoSupplier,
            RemoteFunction function,
            Object... args) {
        FunctionHandler handler = function.getHandler();
        if (handler == null)
            throw new UnsupportedFunctionException(function.getName());

        return handler.call(
                this,
                callInfoSupplier,
                args
        );
    }

    /**
     * Get the remote function name for the given method.
     *
     * @param method The method.
     * @return The function name.
     */
    public String getFunctionName(Method method) {
        return method.getClass().getName() + "." + method.getName();
    }

    private CompiledMethod compileMethod(CompiledInterface itf, Method method) throws Exception {
        CompiledMethod compiledMethod = compiledMethodCache.get(method);
        if (compiledMethod != null)
            return compiledMethod;

        Class<?> klass = method.getDeclaringClass();

        /* Checks for no functions */
        if (Modifier.isStatic(method.getModifiers())) return null;
        if (method.isAnnotationPresent(NoFunction.class)) return null;

        // evaluate hooks
        for (BiFunction<CompiledInterface, Method, CompiledMethod> hook : methodCompilerHooks) {
            compiledMethod = hook.apply(itf, method);
            if (compiledMethod != null) {
                break;
            }
        }

        Class<?> returnType = method.getReturnType();
        if (CompletableFuture.class.isAssignableFrom(returnType)) {
            /*
                Create async function
             */

            // try to find sync method
            String syncMethodName;
            String methodName = method.getName();
            if (methodName.endsWith("Async")) syncMethodName = methodName.substring(0, methodName.length() - 5);
            else syncMethodName = methodName + "Sync";
            Method syncMethod;
            try {
                syncMethod = klass.getMethod(syncMethodName, method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Could not find sync method for " + method);
            }

            // find sync func
            CompiledMethod syncCompiledMethod = compileMethod(itf, syncMethod);
            compiledMethod = new CompiledAsyncMethod(itf, method, syncCompiledMethod);
        } else if (RemoteEvent.class.isAssignableFrom(returnType)) {
            /*
                Create event getter function
             */

            // todo
        } else if (RemoteObject.class.isAssignableFrom(returnType)) {
            /*
                Create remote object creation function
             */

            CompiledObjectClass objectClass = compileObjectClass(itf, returnType);
            compiledMethod = new LocalRemoteObjectMethod(itf, method, objectClass);
        } else {
            /*
                Create sync function
             */

            compiledMethod = new CompiledSyncMethod(itf, method);
        }

        compiledMethodCache.put(method, compiledMethod);
        return compiledMethod;
    }

    // compiles the given class into a
    // compiled proxy-able interface
    private CompiledInterface compileInterface(Class<?> klass) {
        try {
            // check the cache
            CompiledInterface compiledInterface = compiledInterfaceMap.get(klass);
            if (compiledInterface != null)
                return compiledInterface;

            // check if it is a valid remote API
            if (!RemoteAPI.class.isAssignableFrom(klass))
                return null;

            // compile methods
            compiledInterface = new CompiledInterface(klass);
            for (Method method : klass.getMethods()) {
                CompiledMethod funcMethod = compileMethod(compiledInterface, method);
                if (funcMethod == null)
                    continue;
                register(funcMethod.getFunction());
            }

            compiledInterfaceMap.put(klass, compiledInterface);
            return compiledInterface;
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    /**
     * Creates a new proxy of the given interface
     * class, with all methods bound to call the
     * corresponding remote function.
     *
     * @param channel The remote channel.
     * @param klass The interface class.
     * @param <T> The interface type.
     * @return The proxy.
     */
    @SuppressWarnings("unchecked")
    public <T> T bindRemote(Channel channel, Class<T> klass) {
        final CompiledInterface compiledInterface = compileInterface(klass);
        if (compiledInterface == null)
            return null;
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ klass },
                ((proxy, method, args) -> {
                    CompiledMethod cm = compiledInterface.getMethodMap().get(method);
                    if (cm == null) {
                        return MethodUtils.invokeDefault(proxy, method, args);
                    }

                    return cm.proxyCall(this, channel, proxy, args);
                }));
    }

    /**
     * Registers a given handler instance.
     *
     * @param handler The handler.
     * @param <T> The handler type.
     * @return The handler.
     */
    public <T> T register(final T handler) {
        Class<?> handlerClass = handler.getClass();

        // compile superinterfaces
        for (Class<?> itf : handlerClass.getInterfaces()) {
            final CompiledInterface compiledInterface = compileInterface(itf);
            if (compiledInterface == null)
                continue;

            // register handlers
            for (CompiledMethod fm : compiledInterface.getMethods()) {
                // find impl
                Method base = fm.getMethod();
                Method impl = MethodUtils.findImplementation(handlerClass, base);
                if (impl == null)
                    continue;

                // create function and set handler
                RemoteFunction function = fm.getFunction();
                if (function == null)
                    continue;
                function.setHandler((manager, callInfoSupplier, args) -> {
                    try {
                        return impl.invoke(handler, args);
                    } catch (Throwable t) {
                        Throwables.sneakyThrow(t);
                        return null;
                    }
                });
            }
        }

        return handler;
    }

    /**
     * Compile the given remote API object class.
     *
     * @param apiItf The compiled API interface.
     * @param klass The class.
     * @return The compiled class.
     */
    public CompiledObjectClass compileObjectClass(CompiledInterface apiItf, Class<?> klass) {
        CompiledObjectClass compiledObjectClass = compiledObjectClassMap.get(klass);
        if (compiledObjectClass != null) {
            return compiledObjectClass;
        }

        if (!RemoteObject.class.isAssignableFrom(klass))
            return null;

        compiledObjectClass = new CompiledObjectClass(klass);
        for (Method method : klass.getMethods()) {
            // check for UID method
            if (method.isAnnotationPresent(UID.class)) {
                compiledObjectClass.setUidMethod(method);
                compiledObjectClass.setUidType(method.getReturnType());
                continue;
            }

            // check for object method
            ObjectMethod annotation = method.getAnnotation(ObjectMethod.class);
            if (annotation != null) {
                String apiMethodName = annotation.value();
                if (apiMethodName.isEmpty()) apiMethodName = method.getName();

                CompiledMethod compiledApiMethod;
                try {
                    Method apiMethod = apiItf.klass.getMethod(apiMethodName, ArrayUtil.concat(new Class[]{ klass }, method.getParameterTypes()));
                    compiledApiMethod = apiItf.getMethodMap().get(apiMethod);
                } catch (Throwable t) {
                    Throwables.sneakyThrow(t);
                    throw new AssertionError();
                }

                compiledObjectClass.getMethodMap().put(method, new CompiledObjectMethod(method, compiledApiMethod));
                continue;
            }
        }

        compiledObjectClassMap.put(klass, compiledObjectClass);
        return compiledObjectClass;
    }

    /** Instantiates a remote object. */
    public Object instantiateRemoteObject(CompiledObjectClass objectClass, CompiledInterface apiItf,
                                          Channel channel,
                                          Object apiInstance, Object uid) {
        final Method uidMethod = objectClass.getUidMethod();

        return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{objectClass.getKlass()}, (proxy, method, args) -> {
            // handle UID method
            if (method == uidMethod) {
                return uid;
            }

            // handle other methods
            CompiledObjectMethod cm = objectClass.getMethodMap().get(method);
            if (cm == null) {
                return MethodUtils.invokeDefault(proxy, method, args);
            }

            return cm.getApiMethod().proxyCall(this, channel, apiInstance, ArrayUtil.concat(new Object[]{ uid }, args));
        });
    }

}
