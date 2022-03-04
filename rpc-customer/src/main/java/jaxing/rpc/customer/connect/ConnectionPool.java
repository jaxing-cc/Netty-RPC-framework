package jaxing.rpc.customer.connect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import jaxing.rpc.common.obj.RpcProducer;
import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.common.obj.RpcService;
import jaxing.rpc.customer.config.RpcClientConfig;
import jaxing.rpc.customer.handler.RpcClientHandler;
import jaxing.rpc.customer.handler.RpcClientInitializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static ConnectionPool connectionPool = new ConnectionPool();
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
    private ThreadPoolExecutor threadPool;
    private CopyOnWriteArraySet<RpcProducer> producerSet;
    private ConcurrentHashMap<RpcProducer, RpcClientHandler> handlerMap;
    @Setter
    @Getter
    private RpcClientConfig config;
    private volatile boolean isRunning = true;

    private ConnectionPool() {
        handlerMap = new ConcurrentHashMap<>();
        producerSet = new CopyOnWriteArraySet<>();
        threadPool = new ThreadPoolExecutor(4,
                8,
                300L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
    }

    public static ConnectionPool getInstance() {
        return connectionPool;
    }

    public void updateConnectStatus(Set<RpcProducer> set) {
        for (RpcProducer producer : set) {
            if (!producerSet.contains(producer)) {
                if (config.isLazyConnect()){
                    logger.info("发现新连接，lazyConnection = true，使用时才会建立连接 " + producer);
                    //在使用到该连接时才发生连接
                    producerSet.add(producer);
                }else{
                    logger.info("开始建立新连接 " + producer);
                    connectProducer(producer);
                }
            }
        }
        for (RpcProducer producer : producerSet) {
            if (!set.contains(producer)) {
                logger.info("删除无效连接 " + producer);
                removeAndCloseHandler(producer);
            }
        }
    }

    public void updateConnectStatus(RpcProducer producer, PathChildrenCacheEvent.Type type) {
        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !producerSet.contains(producer)) {
            if (config.isLazyConnect()){
                producerSet.add(producer);
            }else{
                connectProducer(producer);
            }
        } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            removeAndCloseHandler(producer);
            if (config.isLazyConnect()){
                producerSet.add(producer);
            }else{
                connectProducer(producer);
            }
        } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            removeAndCloseHandler(producer);
        } else {
            throw new IllegalArgumentException("错误的类型: " + type);
        }
    }

    public void removeAndCloseHandler(RpcProducer producer) {
        RpcClientHandler handler = handlerMap.get(producer);
        if (handler != null) {
            handler.close();
        }
        producerSet.remove(producer);
        handlerMap.remove(producer);
    }

    private void connectProducer(RpcProducer rpcProducer) {
        connectProducer(rpcProducer,false);
    }

    private void connectProducer(RpcProducer rpcProducer,boolean syncFlag) {
        if (rpcProducer.getServices().values().isEmpty()) {
            logger.info(rpcProducer + " 中没有可用服务,注册失败! ");
            return;
        }
        producerSet.add(rpcProducer);
        if (syncFlag){
            connect(rpcProducer,true);
        }else{
            threadPool.submit(() -> connect(rpcProducer, false));
        }
    }

    private void connect(RpcProducer rpcProducer,boolean syncFlag){
        InetSocketAddress remote = new InetSocketAddress(rpcProducer.getHost(), rpcProducer.getPort());
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new RpcClientInitializer(config));
        ChannelFuture channelFuture = b.connect(remote);
        channelFuture.addListener((future) -> {
            //连接完毕
            if (future.isSuccess()) {
                logger.info(rpcProducer + " 主机注册成功! ");
                for (RpcService service : rpcProducer.getServices().values()) {
                    logger.debug("(" + service + " )服务被注册...");
                }
                RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                handlerMap.put(rpcProducer, handler);
                handler.setProducer(rpcProducer);
            } else {
                logger.error("远程主机: [" + remote + "] 连接失败!");
            }
        });
        if (syncFlag){
            try {
                channelFuture.sync();
            } catch (InterruptedException e) {
                logger.error(e.toString());
            }
        }
    }

    public void stop() {
        isRunning = false;
        for (RpcProducer producer : producerSet) {
            removeAndCloseHandler(producer);
        }
        eventLoopGroup.shutdownGracefully();
        threadPool.shutdown();
        logger.info("连接池中的连接已关闭...");
    }

    public void removeHandler(RpcProducer rpcProducer) {
        producerSet.remove(rpcProducer);
        handlerMap.remove(rpcProducer);
    }

    public RpcClientHandler findConnection(RpcRequest request, String interfaceName, String version) {
        //找不到对应handler，有可能是建立链接
        Set<RpcProducer> targetSet = producerSet.stream().filter(rpcProducer -> rpcProducer.contains(interfaceName, version)).collect(Collectors.toSet());
        List<RpcClientHandler> list = new ArrayList<>();
        for (RpcProducer producer : targetSet) {
            if (!handlerMap.containsKey(producer)){
                connectProducer(producer,true);
            }
        }
        handlerMap.forEach((k, v) -> {
            if (k.contains(interfaceName, version)) {
                list.add(v);
            }
        });
        //负载均衡
        return config.getLoadBalance().get(request, list);
    }
}
