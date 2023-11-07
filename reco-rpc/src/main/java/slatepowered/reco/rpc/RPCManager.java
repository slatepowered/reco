package slatepowered.reco.rpc;

import slatepowered.reco.Channel;
import slatepowered.reco.Message;
import slatepowered.reco.rpc.function.*;
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
    private final Map<Method, CompiledInterface.FuncMethod> compiledMethodCache = new HashMap<>();

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
                        Object ret = null; boolean success;
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

        LOGGER.info("callRemote callId(" + exchange.getCallId() + ") function(" + function.getName() + ") channel(" + channel + ")");

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

    private CompiledInterface.FuncMethod compileMethod(Method method,
                                                       Map<String, Integer> mNameMap) throws Exception {
        CompiledInterface.FuncMethod fm = compiledMethodCache.get(method);
        if (fm != null)
            return fm;

        Class<?> klass = method.getDeclaringClass();

        if (Modifier.isStatic(method.getModifiers())) return null;
        if (method.isAnnotationPresent(NoFunction.class)) return null;

        Class<?> returnType = method.getReturnType();
        if (CompletableFuture.class.isAssignableFrom(returnType)) {
            /*
                Create async function
             */

            // find sync method
            String syncMethodName;
            String methodName = method.getName();
            if (methodName.endsWith("Async")) syncMethodName = methodName.substring(0, methodName.length() - 5);
            else syncMethodName = methodName + "Sync";
            Method syncMethod = klass.getMethod(syncMethodName, method.getParameterTypes());

            // find sync func
            CompiledInterface.FuncMethod syncFm = compileMethod(syncMethod, mNameMap);

            fm = new CompiledInterface.FuncMethod(method, null, true, syncFm);
        } else {
            /*
                Create sync function
             */

            // calculate function name
            String name = getFunctionName(method);
            int cnt = mNameMap.getOrDefault(name, 0);
            if (cnt != 0) {
                name += cnt;
                mNameMap.put(name, cnt + 1);
            }

            // calculate arg types
            List<Class<?>> argTypes = new ArrayList<>();
            for (Class<?> kl : method.getParameterTypes()) {
//                if (kl.isAnnotationPresent(Special.class)) continue;
                argTypes.add(kl);
            }

            // create function
            RemoteFunction function = new RemoteFunction(
                    name,
                    argTypes.toArray(new Class[0]),
                    method.getReturnType()
            );

            fm = new CompiledInterface.FuncMethod(
                    method, function, false, null);
        }

        compiledMethodCache.put(method, fm);
        return fm;
    }

    // compiles the given class into a
    // compiled proxy-able interface
    private CompiledInterface compileInterface(Class<?> klass) {
        try {
            CompiledInterface compiledInterface = compiledInterfaceMap.get(klass);
            if (compiledInterface != null)
                return compiledInterface;

            if (!klass.isAnnotationPresent(RemoteAPI.class))
                return null;

            // compile methods
            Map<String, Integer> mNameMap = new HashMap<>(); // map to store encountered names
            List<CompiledInterface.FuncMethod> methods = new ArrayList<>();
            Map<Method, CompiledInterface.FuncMethod> methodMap = new HashMap<>();
            for (Method method : klass.getMethods()) {
                CompiledInterface.FuncMethod funcMethod = compileMethod(method, mNameMap);
                if (funcMethod == null)
                    continue;
                methods.add(funcMethod);
                methodMap.put(method, funcMethod);
                register(funcMethod.getFunction());
            }

            compiledInterface = new CompiledInterface(klass, methods, methodMap);
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
     * corresponding remote function and synchronously
     * wait for the result.
     *
     * @param channel The remote channel.
     * @param klass The interface class.
     * @param <T> The interface type.
     * @return The proxy.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T bindRemoteAwait(Channel channel, Class<T> klass) {
        final CompiledInterface compiledInterface = compileInterface(klass);
        if (compiledInterface == null)
            return null;
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ klass },
                ((proxy, method, args) -> {
                    CompiledInterface.FuncMethod fm =
                            compiledInterface.getMethodMap().get(method);
                    if (fm == null) {
                        return MethodUtils.invokeDefault(proxy, method, args);
                    }

                    RemoteFunction function = fm.getFunction();
                    CompletableFuture<Object> future = callRemote(function, channel, args);

                    return future.join();
                }));
    }

    /**
     * Creates a new proxy of the given interface
     * class, with all methods bound to call the
     * corresponding remote function,
     * voiding any result.
     *
     * @param channel The remote channel.
     * @param klass The interface class.
     * @param <T> The interface type.
     * @return The proxy.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T bindRemoteVoid(Channel channel, Class<T> klass) {
        final CompiledInterface compiledInterface = compileInterface(klass);
        if (compiledInterface == null)
            return null;
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ klass },
                ((proxy, method, args) -> {
                    CompiledInterface.FuncMethod fm =
                            compiledInterface.getMethodMap().get(method);
                    if (fm == null) {
                        return MethodUtils.invokeDefault(proxy, method, args);
                    }

                    RemoteFunction function = fm.getFunction();
                    callRemote(function, channel, args);
                    return null;
                }));
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
                    CompiledInterface.FuncMethod fm =
                            compiledInterface.getMethodMap().get(method);
                    if (fm == null) {
                        return MethodUtils.invokeDefault(proxy, method, args);
                    }

                    RemoteFunction function = fm.syncFunction();
                    CompletableFuture<Object> future = callRemote(function, channel, args);

                    if (fm.isAsync()) return future;
                    else return future.join();
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
            for (CompiledInterface.FuncMethod fm : compiledInterface.getMethods()) {
                if (fm.isAsync()) continue;
                Method base = fm.getMethod();
                Method impl = MethodUtils.findImplementation(handlerClass, base);
                if (impl == null)
                    continue;
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

}
