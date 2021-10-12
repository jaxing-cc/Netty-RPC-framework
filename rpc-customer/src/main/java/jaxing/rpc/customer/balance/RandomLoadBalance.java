package jaxing.rpc.customer.balance;

import jaxing.rpc.customer.handler.RpcClientHandler;

import java.util.List;
import java.util.Random;

//随机负载均衡
public class RandomLoadBalance implements LoadBalance {
    private Random random = new Random();
    @Override
    public RpcClientHandler get(String requestId,List<RpcClientHandler> target) {
            int size = target.size();
            if (size <= 0){
                return null;
            }
            return target.get(random.nextInt(size));

    }
}
