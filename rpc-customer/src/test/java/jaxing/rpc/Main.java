package jaxing.rpc;

import jaxing.rpc.common.config.Constant;
import jaxing.rpc.common.obj.RpcRequest;
import jaxing.rpc.common.obj.RpcResponse;
import jaxing.rpc.common.zk.CuratorClient;
import jaxing.rpc.customer.connect.ServiceDiscovery;
import jaxing.rpc.customer.handler.RpcResult;
import org.junit.Test;

import java.sql.Time;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
//        RpcResult rpcResult = new RpcResult(new RpcRequest());
//
//        Thread threadA = new Thread(()->{
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            RpcResponse rpcResponse = new RpcResponse();
//            rpcResponse.setResult("hello world!!");
//            rpcResult.finish(rpcResponse);
//        });
//        Thread threadB = new Thread(()->{
//            try {
//                System.out.println(rpcResult.get(5, TimeUnit.SECONDS));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//        threadA.start();
//        threadB.start();

    }
}
