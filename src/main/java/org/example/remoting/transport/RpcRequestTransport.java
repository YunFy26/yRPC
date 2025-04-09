package org.example.remoting.transport;

import org.example.spi.SPI;
import org.example.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server
     * @param rpcRequest message body
     * @return result called
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
