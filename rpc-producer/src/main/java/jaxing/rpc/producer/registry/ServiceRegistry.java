package jaxing.rpc.producer.registry;

import jaxing.rpc.common.config.Constant;
import jaxing.rpc.common.obj.RpcProducer;
import jaxing.rpc.common.obj.RpcService;
import jaxing.rpc.common.serializer.JsonSerializer;
import jaxing.rpc.common.serializer.Serializer;
import jaxing.rpc.common.zk.CuratorClient;
import jaxing.rpc.producer.config.RpcServerConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
    private CuratorClient curatorClient;
    private Serializer serializer;
    private RpcServerConfig config;
    private HashSet<String> set;
    public ServiceRegistry(RpcServerConfig config) {
        set = new HashSet<>();
        this.config = config;
        this.curatorClient = new CuratorClient(config.getZkAddress(),
                config.getZkNameSpace(),
                config.getZkSessionTimeOut(),
                config.getZkConnectionTimeOut());
        serializer = config.getSerializer();
    }

    public void registerService(String host, int port, Map<String, Object> serviceMap) {
        HashMap<String,RpcService> map = new HashMap<>();
        serviceMap.forEach((k,v)->{
            String[] serviceInfo = k.split(Constant.FLAG);
            if (serviceInfo.length > 0) {
                RpcService service = new RpcService();
                service.setTargetName(v.getClass().getName());
                service.setInterfaceName(serviceInfo[0]);
                if (serviceInfo.length == 2) {
                    service.setVersion(serviceInfo[1]);
                } else {
                    service.setVersion("");
                }
                logger.info("注册了消费者服务: {} ", k);
                map.put(k,service);
            }
        });
        RpcProducer rpcProducer = new RpcProducer();
        rpcProducer.setHost(host);
        rpcProducer.setPort(port);
        rpcProducer.setServices(map);
        try {
            byte[] data = serializer.serialize(rpcProducer);
            String pathData = config.getZkRegisterNameSpace() + "/" + rpcProducer.toString();
            logger.info("注册路径: " + pathData);
            pathData = curatorClient.createPathData(pathData, data);
            set.add(pathData);
            logger.info("生产者注册完成,ip:{} port:{} 服务数: ",  host, port,set.size());
        }catch (Exception e){
            logger.error("生产者注册异常:{}",e.getMessage());
        }
        curatorClient.addConnectionStateListener((curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                logger.info("生产者与zk连接发生变化: {}", connectionState);
                registerService(host, port, serviceMap);
            }
        });
    }

    public void stop(){
        logger.info("Unregister all service");
        for (String path : set) {
            try {
                this.curatorClient.deletePath(path);
            } catch (Exception ex) {
                logger.error("生产者注销zk数据时出现异常: " + ex.getMessage());
            }
        }
        this.curatorClient.close();
    }
}
