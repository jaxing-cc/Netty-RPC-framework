package jaxing.rpc.customer.balance;

import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.customer.connect.ConnectionPool;
import jaxing.rpc.customer.handler.RpcClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

//随机负载均衡
public class RandomLoadBalance implements LoadBalance {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static Random random = new Random();
    @Override
    public RpcClientHandler get(RpcRequest request, List<RpcClientHandler> target) {
            int size = target.size();
            if (size <= 0){
                return null;
            }
        RpcClientHandler handler = target.get(random.nextInt(size));
        logger.info("请求id:{},发送对象:{}",request.getRequestId(),handler);
        return handler;
    }
}
