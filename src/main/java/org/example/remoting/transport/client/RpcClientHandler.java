package org.example.remoting.transport.client;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.example.enums.CompressTypeEnum;
import org.example.enums.SerializationTypeEnum;
import org.example.factory.SingletonFactory;
import org.example.remoting.constants.RpcConstants;
import org.example.remoting.dto.RpcMessage;
import org.example.remoting.dto.RpcResponse;

import java.net.InetSocketAddress;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private final UnprocessRequests unprocessedRequests;

    private final RpcClient rpcClient;

    public RpcClientHandler() {
        unprocessedRequests = SingletonFactory.getInstance(UnprocessRequests.class);
        rpcClient = SingletonFactory.getInstance(RpcClient.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        // TODO: read事件就绪时，打印日志
//        log.info("client receive msg: [{}]", msg);
        System.out.println("client receive msg: [" + msg + "]");
        byte messageType = msg.getMessageType();
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
//            log.info("heartbeat response received: [{}]", msg.getData());
            System.out.println("heartbeat response received: [" +  msg.getData() + "]");
        } else if (messageType == RpcConstants.RESPONSE_TYPE) {
            RpcResponse<Object> rpcResponse = (RpcResponse<Object>) msg.getData();
            unprocessedRequests.complete(rpcResponse);
        }
    }

    /**
     * 心跳机制
     * @param ctx ChannelHandlerContext
     * @param evt 用户事件
     * @throws Exception TODO:心跳机制中的异常处理
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE){
//                log.info("write idle detected for [{}]", ctx.channel().remoteAddress());
                Channel channel = rpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());

                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodecType(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompressType(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);

                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // TODO:RpcClientHandler中捕获到异常的处理
//        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
