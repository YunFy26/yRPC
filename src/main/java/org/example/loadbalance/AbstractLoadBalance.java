package org.example.loadbalance;

import org.example.remoting.dto.RpcRequest;
import org.example.utils.CollectionUtil;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);
}
