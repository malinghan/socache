package com.so.socache.core;

import org.springframework.stereotype.Component;

import com.so.socache.SoCachePlugin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * so cache server
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-12
 */
@Component
public class SoCacheServer implements SoCachePlugin {

    int port = 6377;

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    Channel channel;


    @Override
    public void init() {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("redis-boss"));
        workerGroup = new NioEventLoopGroup(16, new DefaultThreadFactory("redis-work"));
    }

    @Override
    public void startup() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_RCVBUF, 32 * 1024) //receive buffer
                    .childOption(ChannelOption.SO_SNDBUF, 32 * 1024) //send buffer
                    .childOption(EpollChannelOption.SO_REUSEPORT, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG)) //log
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new SoCacheDecoder()); //decoder
                            ch.pipeline().addLast(new SoCacheHandler()); //handler
                        }
                    });

                channel = b.bind(port).sync().channel();
                System.out.println("starting netty so cache service, port is: " + port);
                channel.closeFuture().sync();
        } catch (Exception e) {
                throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void shutdown() {
        if (this.channel  != null) {
            this.channel.close();
            this.channel = null;
        }
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
            this.bossGroup = null;
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
    }
}
