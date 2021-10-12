package jaxing.rpc.producer.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import jaxing.rpc.common.codec.RpcDecoder;
import jaxing.rpc.common.codec.RpcEncoder;
import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.common.obj.RpcResponse;
import jaxing.rpc.producer.config.RpcServerConfig;

import java.util.Map;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private RpcServerConfig config;
    private Map<String, Object> serviceMap;
    public RpcServerInitializer(Map<String, Object> serviceMap, RpcServerConfig config) {
        this.serviceMap = serviceMap;
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcRequest.class, config.getSerializer()));
        cp.addLast(new RpcEncoder(RpcResponse.class, config.getSerializer()));
        cp.addLast(new RpcServerHandler(serviceMap,config.getSerializer()));
    }
}
