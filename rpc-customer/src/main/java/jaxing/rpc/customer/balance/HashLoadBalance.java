package jaxing.rpc.customer.balance;

import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.customer.connect.ConnectionPool;
import jaxing.rpc.customer.handler.RpcClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * hash负载均衡
 * 即使有多个相同的生产者服务，同一个接口也总是访问一个生产者
 */
public class HashLoadBalance implements LoadBalance{
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    @Override
    public RpcClientHandler get(RpcRequest request, List<RpcClientHandler> target) {
        int size = target.size();
        if (size == 0){
            return null;
        }
        RpcClientHandler handler = target.get(request.getClassName().hashCode() % size);
        logger.info("请求id:{},发送对象:{}",request.getRequestId(),handler);
        return handler;
    }
}
