package jaxing.rpc.customer;

import jaxing.rpc.common.annotation.RpcConsumer;
import jaxing.rpc.customer.config.RpcClientConfig;
import jaxing.rpc.customer.connect.ConnectionPool;
import jaxing.rpc.customer.connect.ServiceDiscovery;
import jaxing.rpc.customer.proxy.ServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;


public class RpcClient implements ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    private final RpcClientConfig config;
    private ServiceDiscovery serviceDiscovery;
    public RpcClient(RpcClientConfig config){
        this.config = config;
        serviceDiscovery = new ServiceDiscovery(config);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createService(Class<T> interfaceClass,String version) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass}
                ,new ServiceProxy(interfaceClass,version));
    }

    @Override
    public void destroy() throws Exception {
        serviceDiscovery.stop();
        ConnectionPool.getInstance().stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        HashMap<String,Object> map = new HashMap<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Field[] fields = bean.getClass().getDeclaredFields();
            try {
                for (Field field : fields) {
                    RpcConsumer consumer = field.getAnnotation(RpcConsumer.class);
                    if (consumer != null) {
                        String version = consumer.version();
                        field.setAccessible(true);
                        Class<?> typeClass = field.getType();
                        String key = typeClass.getName() + "@" + version;
                        if (map.containsKey(key)){
                            field.set(bean, map.get(key));
                        }else{
                            Object service = createService(typeClass, version);
                            map.put(key,service);
                            field.set(bean, service);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(e.toString());
            }
        }
    }
}
