package jaxing.rpc.customer.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jaxing.rpc.common.obj.RpcProducer;
import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.common.obj.RpcResponse;
import jaxing.rpc.customer.connect.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
    private ConcurrentHashMap<String,RpcResult> pendingMap = new ConcurrentHashMap<>();
    private RpcProducer rpcProducer;
    private volatile ChannelHandlerContext ctx;


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        logger.debug("接收到响应: " + rpcResponse);
        String requestId = rpcResponse.getRequestId();
        RpcResult rpcResult = pendingMap.get(requestId);
        if (rpcResult != null){
            pendingMap.remove(requestId);
            rpcResult.finish(rpcResponse);
        }else{
            logger.debug("不能找到对应的id:" + requestId);
        }
    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelRegistered(ctx);
    }
    public void setProducer(RpcProducer rpcProducer) {
        this.rpcProducer = rpcProducer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Rpc客户端出现异常: " + cause.getMessage());
        ctx.close();
    }

    public void close() {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConnectionPool.getInstance().removeHandler(rpcProducer);
    }

    public RpcResult sendRequest(RpcRequest request) throws InterruptedException {
        RpcResult rpcResult = new RpcResult(request);
        pendingMap.put(request.getRequestId(),rpcResult);
        try {
            ChannelFuture channelFuture = ctx.writeAndFlush(request).sync();
            if (!channelFuture.isSuccess()) {
                logger.error("发送RPC请求失败,RequestId: ", request.getRequestId());
            }
        } catch (InterruptedException e) {
            logger.error("发送请求出现异常: " + e.getMessage());
        }
        return rpcResult;
    }
}
