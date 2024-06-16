package com.so.socache.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 将输入的字节流解码为字符串
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-12
 */
public class SoCacheDecoder extends ByteToMessageDecoder {

    // cache处理指令计数器
    AtomicLong counter = new AtomicLong();

    @Override
    protected void decode(ChannelHandlerContext ctx,
                        ByteBuf in, List<Object> out)  throws Exception {

        System.out.println("SoCacheDecoder decodeCount:" + counter.incrementAndGet());

        if (in.readableBytes() <= 0) {
            return;
        }
        int count = in.readableBytes();
        int index = in.readerIndex();
        System.out.println("SoCacheDecoder count:" + count + ",index:" + index);

        byte[] bytes = new byte[count];
        in.readBytes(bytes);
        String ret = new String(bytes);
        System.out.println("SoCacheDecoder ret: " + ret);

        out.add(ret);
    }
}
