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

我也针对这三种方法进行了性能测试，**测试对象**的build方法与类代码如下:

```java
    //num = 树的深度， 我测试用的3，大概 (5)^ 3个节点 
    public static TestTarget getTestTarget(int num) {
            if (num == 0){
                return null;
            }
            TestTarget testTarget = new TestTarget();
            testTarget.setAge((int) (Math.random() * 10000));
            testTarget.setName(UUID.randomUUID().toString());
            testTarget.setMap(getRandomMap());
            ArrayList<TestTarget> objects = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                objects.add(getTestTarget(num - 1)); //树型构造
            }
            testTarget.setChild(objects);
            return testTarget;
        }
    
        private static HashMap<Long,String> getRandomMap(){
            HashMap<Long, String> longStringHashMap = new HashMap<>();
            int i = (int) (Math.random() * 200);
            for (int j = 0; j < i; j++) {
                longStringHashMap.put((long) (Math.random() * 200),UUID.randomUUID().toString());
            }
            return longStringHashMap;
        }
```
TestTarget class:

```java

@Getter
@Setter
public class TestTarget {
    private String name;
    private Integer age;
    private List<TestTarget> child;
    private HashMap<Long,String> map;
}

```
这里直接放测试结果(多次测试结果相差不大):

```text
kryo :测试序列化: [148]ms
kryo :测试反序列化: [18]ms
kryo :序列化大小 [94889]
protostuff :测试序列化: [3]ms
protostuff :测试反序列化: [3]ms
protostuff :序列化大小 [98459]
json :测试序列化: [75]ms
json :测试反序列化: [115]ms
json :序列化大小 [133037]
```

结论: protobuf性能好,序列化后的大小也不多,使用protostuff刚好解决静态编译的问题,非常好用。

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

