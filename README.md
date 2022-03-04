# 基于Netty的RPC服务调用框架

## 项目简介

- 项目使用TCP协议进行数据传输，Zookeeper做服务注册，可以和Spring框架进行整合
- 客户端使用长连接，以免频繁连接断连的性能损耗,
- 对于有同样服务的生产者们，可以使用负载均衡操作
    - HashLoadBalance:同一个接口总是访问同一个服务器
    - RandomLoadBalance:随机访问
    - RotatedLoadBalance:轮转访问
- 两个主要对象: RpcClient(消费者) , RpcServer(生产者)
- SpringBoot项目可以在pom排除本项目的Log依赖，否则会冲突
```xml
    <exclusions>
          <exclusion>
               <groupId>org.slf4j</groupId>
               <artifactId>slf4j-log4j12</artifactId>
          </exclusion>
    </exclusions>
```
## 关于序列化

本项目将bean注册到zookeeper中时使用了Jackson，因为考虑到需要一定的可读性，所以这块是写死的。
在数据参数与返回之的传输过程中的序列化方式是可配置的，目前提供了三个可供选择:

- protostuff (基于protobuf的一种序列化方式)
- kryo
- Jackson

我也针对这三种方法进行了性能测试，测试结果点击 [这里](https://jaxingxcr.gitee.io/2022/03/02/Java%E5%BA%8F%E5%88%97%E5%8C%96%E6%A1%86%E6%9E%B6%E4%B9%8B%E7%AE%80%E5%8D%95%E4%BD%BF%E7%94%A8%E4%B8%8E%E6%80%A7%E8%83%BD%E6%B5%8B%E8%AF%95/)
## 使用方法

- 服务提供者

1. 配置
```java
@Configuration
public class Config {
    @Bean
    public RpcServer buildRpcServer(){
        //RpcServerConfig是配置对象，具体可以点进去看下
        return new RpcServer(RpcServerConfig.getConfig("localhost:2181"));//zk地址
    }
}
```
2. 注册进入zookeeper

```java
@Service
@RpcProducerBean(value = UserService.class , version = "1.0")//注册服务
public class UserServiceImpl implements UserService {
    @Override
    public User getUserByName(String name) {
        User user = new User();
        user.setName(name);
        user.setAge(100);
        return user;
    }
}
```

- 服务消费者

1. 配置

```java
@Configuration
public class Config {
    @Configuration
    public class Config {
        @Bean
        public RpcClient buildRpcServer(){
            //RpcConfig与RpcServerConfig类似
            return new RpcClient(RpcConfig.getConfig("localhost:2181"));//zk地址
        }
    }
}
```

2. 调用消费者

```java
@Service
public class CallService {
    @RpcConsumer(version = "1.0") //注入代理对象
    private UserService userService;

    public User findUserByName(String name){
        return userService.getUserByName(name);
    }
}
```

