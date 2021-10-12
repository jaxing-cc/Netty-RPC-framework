package jaxing.rpc.common.obj;

import lombok.Data;

import java.io.Serializable;


//响应对象
@Data
public class RpcResponse implements Serializable {
    private String requestId;
    private String error;
    private byte[] result;
}
