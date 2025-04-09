package org.example.remoting.transport.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import org.example.enums.CompressTypeEnum;
import org.example.enums.SerializationTypeEnum;
import org.example.enums.ServiceDiscoverEnum;
import org.example.factory.SingletonFactory;
import org.example.register.ServiceDiscover;
import org.example.remoting.constants.RpcConstants;
import org.example.remoting.dto.RpcMessage;
import org.example.remoting.dto.RpcRequest;
import org.example.remoting.dto.RpcResponse;
import org.example.remoting.transport.RpcRequestTransport;

import org.example.remoting.transport.codec.RpcMessageEncoder;
import org.example.remoting.transport.codec.RpcMessageDecoder;
import org.example.spi.ExtensionLoader;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RpcClient implements RpcRequestTransport {

    private final ServiceDiscover serviceDiscovery;
    private final UnprocessRequests unprocessRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public RpcClient() {
        // eventLoopGroup = new NioEventLoopGroup() 已弃用
        // 新的实例化方式如下
        IoHandlerFactory ioHandlerFactory = NioIoHandler.newFactory();
        eventLoopGroup = new MultiThreadIoEventLoopGroup(
                Runtime.getRuntime().availableProcessors() * 2, // 线程数
                ioHandlerFactory
        );
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new RpcClientHandler());
                    }
                });
        serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscover.class).getExtension(ServiceDiscoverEnum.ZK.getName());
        unprocessRequests = SingletonFactory.getInstance(UnprocessRequests.class);
        channelProvider = SingletonFactory.getInstance(ChannelProvider.class);

    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 异步连接远程服务器
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                completableFuture.complete(future.channel());
            }else {
                throw new IllegalStateException();
            }
        });
        // 阻塞等待
        return completableFuture.get(5, TimeUnit.SECONDS);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if (channel == null){
            // TODO: if channel is null, print more message
            throw new RuntimeException("///");
//            log.error();
        }else {
            if(channel.isActive()){
                unprocessRequests.put(rpcRequest.getRequestId(), resultFuture);
                RpcMessage rpcMessage = RpcMessage.builder()
                        .data(rpcRequest)
                        .codecType(SerializationTypeEnum.HESSIAN.getCode())
                        .compressType(CompressTypeEnum.GZIP.getCode())
                        .messageType(RpcConstants.REQUEST_TYPE)
                        .build();
                System.out.println("Channel is Active");
                channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()){
                        // TODO:future is success, print more message
                        System.out.println("Client send message:" + rpcMessage);
                    }else {
                        future.channel().close();
                        resultFuture.completeExceptionally(future.cause());
//                        log.error("Send failed");
                        System.out.println("Send failed");
                    }
                });
            }else {
                // TODO:channel in not active, print more message
                throw new IllegalStateException();
            }
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel == null){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }
}
