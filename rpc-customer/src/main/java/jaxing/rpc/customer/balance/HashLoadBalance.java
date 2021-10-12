package jaxing.rpc.customer.balance;

import jaxing.rpc.customer.handler.RpcClientHandler;

import java.util.List;


public class HashLoadBalance implements LoadBalance{
    @Override
    public RpcClientHandler get(String requestId, List<RpcClientHandler> target) {
        int size = target.size();
        if (size <= 0){
            return null;
        }
        return target.get(requestId.hashCode() % size);
    }
}
