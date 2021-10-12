package jaxing.rpc.customer.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import jaxing.rpc.common.codec.RpcDecoder;
import jaxing.rpc.common.codec.RpcEncoder;
import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.common.obj.RpcResponse;
import jaxing.rpc.customer.config.RpcConfig;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    private final RpcConfig config;

    public RpcClientInitializer(RpcConfig config) {
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new RpcEncoder(RpcRequest.class,config.getSerializer()))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(new RpcDecoder(RpcResponse.class,config.getSerializer()))
                .addLast(new RpcClientHandler());
    }
}
