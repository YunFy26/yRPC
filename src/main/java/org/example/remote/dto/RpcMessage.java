package org.example.remote.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    /**
     * message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codecType;
    /**
     * compress type
     */
    private byte compressType;
    /**
     * request id
     */
    private int requestId;
    /**
     * data：rpcRequest or rpcResponse
     */
    private Object data;
}
