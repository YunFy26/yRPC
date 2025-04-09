package org.example.loadbalance;

import org.example.remoting.dto.RpcRequest;
import org.example.spi.SPI;

import java.util.List;

@SPI
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
