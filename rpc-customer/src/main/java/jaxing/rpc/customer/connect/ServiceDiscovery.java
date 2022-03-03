package jaxing.rpc.customer.connect;

import jaxing.rpc.common.obj.RpcProducer;
import jaxing.rpc.common.serializer.JsonSerializer;
import jaxing.rpc.common.zk.CuratorClient;
import jaxing.rpc.customer.config.RpcClientConfig;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private final RpcClientConfig config;
    public CuratorClient curatorClient;

    public ServiceDiscovery(RpcClientConfig config) {
        ConnectionPool.getInstance().setConfig(config);
        this.config = config;
        this.curatorClient = new CuratorClient(config.getZkAddress(),
                config.getZkNameSpace(),
                config.getZkSessionTimeOut(),
                config.getZkConnectionTimeOut());
        discoveryService();
    }

    private void discoveryService() {
        try {
            logger.info("服务发现...");
            getServiceAndUpdateServer();
            curatorClient.watchPathChildrenNode(config.getZkRegisterNameSpace(), (curatorFramework, pathChildrenCacheEvent) -> {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                ChildData childData = pathChildrenCacheEvent.getData();
                logger.info("zk监听... : " + type);
                switch (type) {
                    case CONNECTION_RECONNECTED:
                        getServiceAndUpdateServer();
                        break;
                    case CHILD_ADDED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                        break;
                    case CHILD_UPDATED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                        break;
                    case CHILD_REMOVED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                        break;
                }
            });
        } catch (Exception ex) {
            logger.error("监听出现异常: " + ex.toString());
        }
    }
    private void getServiceAndUpdateServer() {
        try {
            List<String> nodeList = curatorClient.getChildren(config.getZkRegisterNameSpace());
            Set<RpcProducer> dataList = new HashSet<>();
            StringBuilder sb = new StringBuilder();
            for (String name : nodeList) {
                sb.append(config.getZkRegisterNameSpace()).append("/").append(name);
                logger.info("服务路径: " + sb);
                byte[] bytes = curatorClient.getData(sb.toString());
                RpcProducer producer = JsonSerializer.getInstance().deserialize(bytes,RpcProducer.class);
                dataList.add(producer);
                sb.delete(0,sb.length());
            }
            UpdateConnectedServer(dataList);
        } catch (Exception e) {
            logger.error("注册服务时出现异常: " + e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        byte[] data = childData.getData();
        logger.info("Child data updated, path:{},type:{},data:{},", path, type, data);
        RpcProducer rpcProtocol =  JsonSerializer.getInstance().deserialize(data,RpcProducer.class);
        updateConnectedServer(rpcProtocol, type);
    }

    private void UpdateConnectedServer(Set<RpcProducer> dataList) {
        ConnectionPool.getInstance().updateConnectStatus(dataList);
    }


    private void updateConnectedServer(RpcProducer rpcProducer, PathChildrenCacheEvent.Type type) {
        ConnectionPool.getInstance().updateConnectStatus(rpcProducer,type);
    }

    public void stop() {
        logger.info("zk服务发现对象已关闭...");
        this.curatorClient.close();
    }
}
