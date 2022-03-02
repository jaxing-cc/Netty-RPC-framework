package jaxing.rpc.producer.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jaxing.rpc.common.annotation.RpcProducerBean;
import jaxing.rpc.common.config.Constant;
import jaxing.rpc.producer.config.RpcServerConfig;
import jaxing.rpc.producer.handler.RpcServerInitializer;
import jaxing.rpc.producer.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

public class RpcServer implements ApplicationContextAware, InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private Thread thread;
    private RpcServerConfig config;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> serviceMap;

    public RpcServer(RpcServerConfig config) {
        this.config = config;
        this.serviceRegistry = new ServiceRegistry(config);
        serviceMap = new HashMap<>();
    }

    public void addService(String interfaceName, String version, Object serviceBean) {
        logger.info("添加了新服务, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
        serviceMap.put(interfaceName + Constant.FLAG + version, serviceBean);
    }

    public void start() {
        thread = new Thread(()->{
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new RpcServerInitializer(serviceMap,config))
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = bootstrap.bind(config.getPort()).sync();
                if (serviceRegistry != null) {
                    serviceRegistry.registerService(config.getHost(), config.getPort(), serviceMap);
                }
                logger.info("服务器启动在 {}", config.getPort());
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    logger.error("Rpc服务器远程连接异常:" + e.getMessage());
                }
            } finally {
                try {
                    serviceRegistry.stop();
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        });
        thread.start();
    }

    public void stop() {
        // 停止线程
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcProducerBean.class);
        if (serviceBeanMap != null) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcProducerBean nettyRpcService = serviceBean.getClass().getAnnotation(RpcProducerBean.class);
                String interfaceName = nettyRpcService.value().getName();
                String version = nettyRpcService.version();
                addService(interfaceName, version, serviceBean);
            }
        }
    }
}
