package jaxing.rpc.customer.balance;

import jaxing.rpc.common.config.Constant;
import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.customer.connect.ConnectionPool;
import jaxing.rpc.customer.handler.RpcClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轮转负载均衡
 *
 */
public class RotatedLoadBalance implements LoadBalance{
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private ConcurrentHashMap<String,Integer> countMap;

    public RotatedLoadBalance(){
        countMap = new ConcurrentHashMap<>();
    }

    @Override
    public RpcClientHandler get(RpcRequest request, List<RpcClientHandler> target) {
        int size = target.size();
        if (size == 0){
            return null;
        }
        String key = request.getClassName() + Constant.FLAG + request.getVersion();
        Integer count = countMap.getOrDefault(key,0);
        System.out.println(count);
        RpcClientHandler handler = target.get(count);
        countMap.put(key,(count + 1) % size);
        logger.info("请求id:{},发送对象:{}",request.getRequestId(),handler);
        return handler;
    }
}
