package com.so.socache.core;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 将输入的字节流解码为字符串
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-12
 */
public class SoCacheEncoder extends MessageToByteEncoder<String> {

    // cache处理指令计数器
    AtomicLong counter = new AtomicLong();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("SoCacheEncoder added to pipeline.");
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, String s, ByteBuf out) throws Exception {
        System.out.println("SoCacheEncoder wrap byte buffer and reply: " + s);
        System.out.println("SoCacheEncoder encodeCount:" + counter.incrementAndGet());
        System.out.println("SoCacheEncoder s:" + s);
//        ByteBuf buffer = Unpooled.buffer(128);
//        buffer.writeBytes(s.getBytes());
//        ctx.writeAndFlush(buffer);
        if (s == null || s.isEmpty()) {
            return; // 如果消息为空或长度为零，则不进行编码
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.writeBytes(bytes);
//        ctx.writeAndFlush(out);
    }
}
