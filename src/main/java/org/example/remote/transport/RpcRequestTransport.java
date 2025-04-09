package org.example.remote.transport;

import org.example.spi.SPI;
import org.example.remote.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server
     * @param rpcRequest message body
     * @return result called
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
