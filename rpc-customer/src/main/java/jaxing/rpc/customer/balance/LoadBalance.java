package jaxing.rpc.customer.balance;

import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.customer.handler.RpcClientHandler;

import java.util.List;

public interface LoadBalance {

    RpcClientHandler get(RpcRequest request, List<RpcClientHandler> target);
}
