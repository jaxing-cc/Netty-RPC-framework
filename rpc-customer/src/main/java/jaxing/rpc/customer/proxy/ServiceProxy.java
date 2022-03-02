package jaxing.rpc.customer.proxy;

import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.customer.config.RpcConfig;
import jaxing.rpc.customer.connect.ConnectionPool;
import jaxing.rpc.customer.handler.RpcClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ServiceProxy<T> implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProxy.class);
    private Class<T> clazz;
    private String version;
    public ServiceProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcRequest request = createRequest(method, args);
        RpcClientHandler handler = ConnectionPool.getInstance().findConnection(request,method.getDeclaringClass().getName(),version);
        if (handler == null){
            logger.error("无法找到符合条件的客户端, 接口:{} 版本:{}",clazz.getName(),version);
            return null;
        }
        RpcConfig config = ConnectionPool.getInstance().getConfig();
        Class<?> returnType = method.getReturnType();
        byte[] data = handler.sendRequest(request).get(config.getMaxResponseTime(), config.getMaxResponseTimeUnit());
        logger.info("请求id {}, 返回值 {}",request.getRequestId(), data == null ? null : new String(data));
        return config.getSerializer().deserialize(data,returnType);
    }

    private RpcRequest createRequest(Method method, Object[] args){
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);
        return request;
    }
}
