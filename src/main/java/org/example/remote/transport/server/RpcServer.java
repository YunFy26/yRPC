package org.example.remote.transport.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import org.example.config.RpcServiceConfig;
import org.example.factory.SingletonFactory;
import org.example.provider.ServiceProvider;
import org.example.provider.impl.ZkServiceProvider;
import org.example.remote.transport.codec.RpcMessageDecoder;
import org.example.remote.transport.codec.RpcMessageEncoder;
import org.example.utils.concurrent.ThreadPoolFactoryUtil;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

//@Component
public class RpcServer {

    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);

    // TODO:RpcServer 为什么注册服务
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start(){
        // TODO: 启动前先清理之前的所有服务
        String host = InetAddress.getLocalHost().getHostAddress();
        IoHandlerFactory ioHandlerFactory = NioIoHandler.newFactory();
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(
                1, // 线程数
                ioHandlerFactory
        );
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(
                Runtime.getRuntime().availableProcessors() * 2, // 线程数
                ioHandlerFactory
        );

        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                Runtime.getRuntime().availableProcessors() * 2, // 线程数
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );


        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 设置半连接队列（SYN Queue）和全连接队列（Accept Queue）的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // TCP:关闭Nagle算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // TCP:启用长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch){
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            // 异步管理，不再显示传递线程池
//                            p.addLast("rpcServerHandler", new RpcServerHandler(serviceHandlerGroup));
                            p.addLast(serviceHandlerGroup, "rpcServerHandler", new RpcServerHandler());
                        }
                    });
            ChannelFuture f = server.bind(host, PORT).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            // TODO: 异常处理
            throw new RuntimeException(e);
        }finally {
            // TODO:日志
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }

}
