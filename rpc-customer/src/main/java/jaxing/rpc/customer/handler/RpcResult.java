package jaxing.rpc.customer.handler;

import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.common.obj.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class RpcResult  {
    private static final Logger logger = LoggerFactory.getLogger(RpcResult.class);
    private RpcRequest request;
    private volatile RpcResponse response;
    private long startTime;
    private Semaphore semaphore = new Semaphore(1,true);
    private volatile boolean done = false;
    public RpcResult(RpcRequest rpcRequest) throws InterruptedException {
        this.request = rpcRequest;
        startTime = System.currentTimeMillis();
        semaphore.acquire(1);
    }

    public synchronized boolean isDone() {
        return done;
    }

    public byte[] get() {
        return response == null?null:response.getResult();
    }

    public byte[] get(long timeout, TimeUnit unit) throws InterruptedException {
        boolean success = semaphore.tryAcquire(timeout,unit);
        if (success){
            return this.response == null ? null : this.response.getResult();
        }else{
            logger.error("服务器响应超时");
            throw new RuntimeException("服务器响应时间超时!");
        }
    }

    public synchronized void finish(RpcResponse rpcResponse) {
        done = true;
        response = rpcResponse;
        semaphore.release();
    }
}
