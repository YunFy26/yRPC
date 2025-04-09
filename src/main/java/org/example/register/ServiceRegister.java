package org.example.register;

import org.example.spi.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegister {
    /**
     *
     * @param rpcServiceName rpc service name
     * @param inetSocketAddress service address
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
