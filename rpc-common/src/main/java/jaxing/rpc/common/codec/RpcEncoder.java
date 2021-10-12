package jaxing.rpc.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import jaxing.rpc.common.serializer.JsonSerializer;
import jaxing.rpc.common.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//编码器
public class RpcEncoder extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(RpcDecoder.class);
    private Class<?> genericClass;
    private Serializer serializer;
    public RpcEncoder(Class<?> genericClass) {
        this(genericClass, JsonSerializer.getInstance());
    }
    public RpcEncoder(Class<?> genericClass,Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            try {
                byte[] data = serializer.serialize(o);
                byteBuf.writeInt(data.length);
                byteBuf.writeBytes(data);
            } catch (Exception ex) {
                logger.error("编码异常: " + ex.toString());
            }
        }
    }
}
