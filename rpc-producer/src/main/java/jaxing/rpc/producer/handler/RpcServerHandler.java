package jaxing.rpc.producer.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import jaxing.rpc.common.config.Constant;
import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.common.obj.RpcResponse;
import jaxing.rpc.common.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    private static final EventExecutorGroup executors = new DefaultEventExecutorGroup(8);
    private final Map<String, Object> map;
    private final Serializer serializer;

    public RpcServerHandler(Map<String, Object> map, Serializer serializer){
        this.serializer = serializer;
        this.map = map;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        //异步执行方法
        executors.submit(()->{
            String requestId = rpcRequest.getRequestId();
            logger.info("接收到 request " + requestId);
            RpcResponse response = new RpcResponse();
            response.setRequestId(requestId);
            try {
                Object result = invokeMethod(rpcRequest);
                response.setResult(result == null? null: serializer.serialize(result));
            } catch (Throwable t) {
                response.setError(t.toString());
                logger.error("RPC 服务器处理请求异常: ", t);
            }
            ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> logger.info("响应请求成功，requestId: " + rpcRequest.getRequestId()));
        });
    }

    private Object invokeMethod(RpcRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String className = request.getClassName();
        String version = request.getVersion();
        String key = className + Constant.FLAG + version;
        Object serviceBean = map.get(key);
        if (serviceBean == null) {
            logger.error("无法找到对应的bean {},处理失败", key);
            throw new RuntimeException("无法找到对应的bean,处理失败");
        }
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        Class<?> serviceBeanClass = serviceBean.getClass();
        logger.debug(serviceBeanClass.getName() + "=>" +methodName);
        Method method = serviceBeanClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean,parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("RPC服务器异常: " + cause.getMessage());
        ctx.close();
    }
}
