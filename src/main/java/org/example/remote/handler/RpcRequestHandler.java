package org.example.remote.handler;

import org.example.exception.RpcException;
import org.example.factory.SingletonFactory;
import org.example.provider.ServiceProvider;
import org.example.provider.impl.ZkServiceProvider;
import org.example.remote.dto.RpcRequest;

import java.lang.reflect.Method;

public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler(){
        serviceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);
    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeService(rpcRequest, service);
    }

    /**
     * 反射调用目标方法
     * @param rpcRequest rpc请求
     * @param service 服务名
     * @return
     */
    private Object invokeService(RpcRequest rpcRequest, Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
        }catch (Exception e){
            // TODO:异常捕获 ｜ NoSuchMethod ｜ IllegalArgument ...
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }

}
