package jaxing.rpc.common.obj;

import lombok.Data;

import java.io.Serializable;

//请求对象
@Data
public class RpcRequest implements Serializable {
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private String version;
}
