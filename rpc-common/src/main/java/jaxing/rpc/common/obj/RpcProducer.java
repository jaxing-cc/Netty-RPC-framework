package jaxing.rpc.common.obj;

import jaxing.rpc.common.config.Constant;
import lombok.Data;
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

    public RpcProducer(){
        this.services = new HashMap<>();
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
        if (o == null || getClass() != o.getClass()) return false;
        RpcProducer producer = (RpcProducer) o;
        String host = producer.getHost();
        return Objects.equals(host,producer.host) && Objects.equals(port,producer.port);
    }

    @Override
    public String toString() {
        return host+"="+port;
    }


}
