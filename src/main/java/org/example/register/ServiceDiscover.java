package org.example.register;

import org.example.spi.SPI;
import org.example.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscover {
    /**
     * lookup service by rpcServiceName
     * @param rpcRequest rpc request
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
