package jaxing.rpc.customer.balance;

import jaxing.rpc.customer.handler.RpcClientHandler;

import java.util.List;

public interface LoadBalance {

    RpcClientHandler get(String requestId,List<RpcClientHandler> target);
}
