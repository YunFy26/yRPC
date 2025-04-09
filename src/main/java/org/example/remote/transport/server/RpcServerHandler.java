//package org.example.remoting.transport.server;
//
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.timeout.IdleState;
//import io.netty.handler.timeout.IdleStateEvent;
//import io.netty.util.concurrent.DefaultEventExecutorGroup;
//import org.example.enums.CompressTypeEnum;
//import org.example.enums.RpcResponseCodeEnum;
//import org.example.enums.SerializationTypeEnum;
//import org.example.factory.SingletonFactory;
//import org.example.remoting.constants.RpcConstants;
//import org.example.remoting.dto.RpcMessage;
//import org.example.remoting.dto.RpcRequest;
//import org.example.remoting.dto.RpcResponse;
//import org.example.remoting.handler.RpcRequestHandler;
//import org.example.utils.concurrent.ThreadPoolFactoryUtil;
//
//public class RpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
//
//    private final RpcRequestHandler rpcRequestHandler;
//
//    private final DefaultEventExecutorGroup serviceHandlerGroup;
//
//    public RpcServerHandler(DefaultEventExecutorGroup defaultEventExecutorGroup){
//        rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
//        serviceHandlerGroup = defaultEventExecutorGroup;
//    }
//
//
//    @Override
//    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("channelRegistered: " + ctx.channel().remoteAddress());
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
//        byte msgType = msg.getMessageType();
//        RpcMessage rpcMessage = new RpcMessage();
//        rpcMessage.setCodecType(SerializationTypeEnum.HESSIAN.getCode());
//        rpcMessage.setCompressType(CompressTypeEnum.GZIP.getCode());
//
//        if (msgType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
//            rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
//            rpcMessage.setData(RpcConstants.PONG);
//            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
//        } else {
//            serviceHandlerGroup.submit(() -> {
//                try {
//                    RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
//                    Object result = rpcRequestHandler.handle(rpcRequest);
//                    RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
//                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
//                    rpcMessage.setData(rpcResponse);
//
//                    ctx.channel().eventLoop().execute(() -> {
//                        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
//                            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
//                        } else {
//                            System.out.println("Channel not writable. Request ID: " + rpcRequest.getRequestId());
//                        }
//                    });
//                } catch (Exception e) {
//                    RpcResponse<Object> errorResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
//                    rpcMessage.setData(errorResponse);
//                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
//                }
//            });
//        }
//    }
//
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent){
//            IdleState state = ((IdleStateEvent) evt).state();
//            if (state == IdleState.READER_IDLE){
//                // TODO：日志
//                ctx.close();
//            }
//        }else {
//            super.userEventTriggered(ctx, evt);
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        // TODO: 捕获到异常的处理
////        log.error("server catch exception", cause);
//        System.out.println("server catch exception：" + cause);
//        ctx.close();
//    }
//}

package org.example.remote.transport.server;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.example.enums.CompressTypeEnum;
import org.example.enums.RpcResponseCodeEnum;
import org.example.enums.SerializationTypeEnum;
import org.example.factory.SingletonFactory;
import org.example.remote.constants.RpcConstants;
import org.example.remote.dto.RpcMessage;
import org.example.remote.dto.RpcRequest;
import org.example.remote.dto.RpcResponse;
import org.example.remote.handler.RpcRequestHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Customize the ChannelHandler of the server to process the data sent by the client.
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 20:44:00
 */
@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    public RpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                System.out.println("server receive msg: [" + msg + "] ");
                byte messageType = ((RpcMessage) msg).getMessageType();
//                System.out.println(messageType);
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodecType(SerializationTypeEnum.HESSIAN.getCode());
                rpcMessage.setCompressType(CompressTypeEnum.GZIP.getCode());

                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // Execute the target method (the method the client needs to execute) and return the method result
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } catch (Exception e) {
            log.error("server catch exception", e);

        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
