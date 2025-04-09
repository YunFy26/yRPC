package org.example.provider;

import org.example.config.RpcServiceConfig;

public interface ServiceProvider {

    void addService(RpcServiceConfig rpcServiceConfig);

    void publishService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);
}
