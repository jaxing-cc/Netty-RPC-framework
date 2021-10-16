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

