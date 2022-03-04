package jaxing.rpc.common.obj;

import jaxing.rpc.common.config.Constant;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

@Getter
@Setter
public class RpcProducer implements Serializable {
    //ip
    private String host;
    //端口
    private int port;
    //提供的服务
    private HashMap<String , RpcService> services;

    private int hashcode;

    public RpcProducer(){
        this.services = new HashMap<>();
        hashcode = -1;
    }

    public void put(RpcService rpcService){
        services.put(rpcService.makeName(),rpcService);
    }


    public boolean contains(String interfaceName,String version){
        RpcService rpcService = services.get(interfaceName + Constant.FLAG + version);
        return rpcService != null && rpcService.getVersion() != null && rpcService.getVersion().equals(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RpcProducer)) return false;
        RpcProducer producer = (RpcProducer) o;
        return Objects.equals(this.host,producer.host) && this.port == producer.port;
    }

    @Override
    public String toString() {
        return host+":"+port;
    }

    @Override
    public int hashCode() {
        return hashcode == -1 ? hashcode = (toString().hashCode()) : hashcode;
    }
}
