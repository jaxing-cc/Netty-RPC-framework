package jaxing.rpc.producer.config;


import jaxing.rpc.common.config.Constant;
import jaxing.rpc.common.serializer.JsonSerializer;
import jaxing.rpc.common.serializer.Serializer;
import lombok.Getter;
import lombok.Setter;

@Getter
public class RpcServerConfig {
    //zk命名空间地址
    private String zkNameSpace;
    //zk注册空间地址
    private String zkRegisterNameSpace;
    //zk连接地址
    private String zkAddress;
    //zk会话超时 ms
    private int zkSessionTimeOut;
    //zk连接超时 ms
    private int zkConnectionTimeOut;
    //序列化
    private Serializer serializer;
    //服务启动端口
    private int port;
    //服务IP地址
    private String host;
    public static RpcServerConfig getConfig(String zkAddress){
        return new RpcServerConfig().configZk(
                zkAddress,
                Constant.nameSpace,
                Constant.registry,
                5000,
                5000)
                .configSocket("localhost",9527)
                .configSerializer(new JsonSerializer());
    }
    public RpcServerConfig configZk(String zkAddress, String zkNameSpace, String zkRegisterNameSpace, int zkSessionTimeOut, int zkConnectionTimeOut){
        this.zkAddress = zkAddress;
        this.zkNameSpace = zkNameSpace;
        this.zkRegisterNameSpace = zkRegisterNameSpace;
        this.zkSessionTimeOut = zkSessionTimeOut;
        this.zkConnectionTimeOut = zkConnectionTimeOut;
        return this;
    }

    public RpcServerConfig configSocket(String host,int port){
        this.port = port;
        this.host = host;
        return this;
    }
    public RpcServerConfig configSerializer(Serializer serializer){
        this.serializer = serializer;
        return this;
    }

}
