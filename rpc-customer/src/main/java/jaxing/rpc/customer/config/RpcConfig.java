package jaxing.rpc.customer.config;


import jaxing.rpc.common.config.Constant;
import jaxing.rpc.common.serializer.JsonSerializer;
import jaxing.rpc.common.serializer.Serializer;
import jaxing.rpc.customer.balance.LoadBalance;
import jaxing.rpc.customer.balance.RandomLoadBalance;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
public class RpcConfig {
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
    @Setter
    private Serializer serializer;
    //负载均衡
    private LoadBalance loadBalance;
    //最大响应时间
    private int maxResponseTime;
    //最大响应时间单位
    private TimeUnit maxResponseTimeUnit;
    public static RpcConfig getConfig(String zkAddress){
        return new RpcConfig().configZk(zkAddress,
                Constant.nameSpace,
                Constant.registry,
                5000,
                5000)
                .configLoadBalance(new RandomLoadBalance())
                .configMaxResponseTime(5000,TimeUnit.SECONDS)
                .configSerializer(new JsonSerializer());
    }
    public RpcConfig configZk(String zkAddress,String zkNameSpace,String zkRegisterNameSpace,int zkSessionTimeOut,int zkConnectionTimeOut){
        this.zkAddress = zkAddress;
        this.zkNameSpace = zkNameSpace;
        this.zkRegisterNameSpace = zkRegisterNameSpace;
        this.zkSessionTimeOut = zkSessionTimeOut;
        this.zkConnectionTimeOut = zkConnectionTimeOut;
        return this;
    }

    public RpcConfig configSerializer(Serializer serializer){
        this.serializer = serializer;
        return this;
    }

    public RpcConfig configLoadBalance(LoadBalance loadBalance){
        this.loadBalance = loadBalance;
        return this;
    }
    public RpcConfig configMaxResponseTime(int maxResponseTime,TimeUnit maxResponseTimeUnit){
        this.maxResponseTime = maxResponseTime;
        this.maxResponseTimeUnit = maxResponseTimeUnit;
        return this;
    }
}
