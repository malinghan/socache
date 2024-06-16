package com.so.socache.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 命令行处理
 * https://redis.com.cn/topics/protocol.html
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-12
 */
@Slf4j
public class SoCacheHandler extends SimpleChannelInboundHandler<String> {

    private static final String CRLF = "\r\n";
    private static final String STRING_PREFIX = "+";
    private static final String BULK_PREFIX = "$";
    private static final String OK ="OK";
    public static final String INFO =
            "SoCache server, [v1.0.0], created by malinghan." + CRLF
            + "Mock Redis Server, at 2024-06-12, Shenzhen" + CRLF;

    public static final SoCache cache = new SoCache();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {

        String[] args = message.split(CRLF);
        System.out.println("SoCacheHandler => " + String.join(",",  args));

        String cmd = args[2].toUpperCase();
        System.out.println("SoCacheHandler => cmd : " +  cmd);

        //1. command
        if ("COMMAND".equals(cmd)) {
            writeByteBuf(ctx, "*2"
                    + CRLF + "$7"
                    + CRLF + "COMMAND"
                    + CRLF + "$4"
                    + CRLF + "PING"
                    + CRLF);
        } else if ("PING".equals(cmd)) { //input:  *1,$4,ping
            String ret = "PONG";
            if (args.length >= 5) {
                //*2,$4,PING,$1,A
                //ret = A
                ret = args[4];
            }
            simpleString(ctx, ret);
        } else if ("INFO".equals(cmd)) {
            //SoCache server, [v1.0.0], created by malinghan.
            //Mock Redis Server, at 2024-06-12, Shenzhen
            bulkString(ctx, INFO);
        } else if ("SET".equals(cmd)) {
            //SET a a
            //*3,$3,SET,$1,a,$1,a
            //args[4] = a args[6] = a
            if (args.length < 7) {
                error(ctx, "SET must have key and value.");
                return;
            }
            cache.set(args[4], args[6]);
            simpleString(ctx, OK);
        } else if ("GET".equals(cmd)) {
            //GET a
            //*2,$3,GET,$1,a
            //GET aaa (aaa不存在)
            //*2,$3,GET,$3,aaa
            //value = null
            String value = cache.get(args[4]);
            bulkString(ctx, value);
        } else if ("STRLEN".equals(cmd)) {
            String value = cache.get(args[4]);
            integerWriteByteBuf(ctx, value == null ? 0 : value.length());
        } else if ("DEL".equals(cmd)) {
            //*2,$3,DEL,$1,a
            int len = (args.length-3)/2;
            String[] keys = new String[len];
            for(int i=0; i<len; i++) {
                keys[i] = args[4+i*2];
            }
            int del = cache.del(keys);
            integerWriteByteBuf(ctx, del);
        }
        else if ("EXISTS".equals(cmd)) {
            int len = (args.length - 3) / 2;
            String[] keys = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = args[4 + i * 2];
            }
            integerWriteByteBuf(ctx, cache.exists(keys));
        }
        else if ("MGET".equals(cmd)) {
//            SoCacheHandler => *4,$4,MGET,$1,a,$1,b,$1,c
//            SoCacheHandler => cmd : MGET
//            wrap byte buffer and reply: *3
//            $-1
//            $-1
//            $-1
            int len = (args.length - 3) / 2;
            String[] keys = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = args[4 + i * 2];
            }
            //输出是一个数组
            array(ctx, cache.mget(keys));
        }
        else if ("MSET".equals(cmd)) {
            int len = (args.length - 3) / 4;
            String[] keys = new String[len];
            String[] vals = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = args[4 + i * 4];
                vals[i] = args[6 + i * 4];
            }
            cache.mset(keys, vals);
            simpleString(ctx, OK);
        }
        else if ("INCR".equals(cmd)) {
            String key = args[4];
            try {
                integerWriteByteBuf(ctx, cache.incr(key));
            } catch (NumberFormatException nfe) {
                error(ctx, "NFE " + key + " value[" + cache.get(key) + "] is not an integer.");
            }
        }
        else if("DECR".equals(cmd)) {
           String key = args[4];
                try {
                    integerWriteByteBuf(ctx, cache.decr(key));
                } catch (NumberFormatException nfe) {
                    error(ctx, "NFE " + key + " value is not an integer.");
                }
        }
        else {
            simpleString(ctx, OK);
        }
    }

    /**
     * output length of content
     * @param ctx
     * @param content
     */
    private void bulkString(ChannelHandlerContext ctx, String content) {
        writeByteBuf(ctx, bulkStringEncoding(content));
    }

    /**
     * 该函数用于根据输入的字符串content生成相应的bulk string格式的字符串。
     * bulk string是一种Redis协议中使用的数据格式，
     * 用于表示一个可选的字符串值。函数返回的字符串以`$
     * @param content
     * @param content
     */
    private String bulkStringEncoding(String content) {
        String ret;
        if ( content == null) {
            ret = "$-1"  + CRLF;
        } else if (content.isEmpty()) {
            ret = "$0" + CRLF;
        } else {
            ret = BULK_PREFIX + content.getBytes().length + CRLF + content + CRLF;
        }
        return ret;
    }

    /**
     * echo input
     * @param ctx
     * @param content
     */
    private void simpleString(ChannelHandlerContext ctx, String content) {
        writeByteBuf(ctx, stringEncode(content));
    }

    private void error(ChannelHandlerContext ctx, String msg) {
        writeByteBuf(ctx, errorEncode(msg));
    }

    private static String errorEncode(String msg) {
        return "-" + msg + CRLF;
    }

    private static String stringEncode(String content) {
        String ret;
        if ( content == null) {
            ret = "$-1";
        } else if (content.isEmpty()) {
            ret = "$0";
        } else {
            ret = STRING_PREFIX +  content;
        }
        return ret + CRLF;
    }



//    private void writeByteBuf(ChannelHandlerContext ctx, String content) {
//        System.out.println("wrap byte buffer and reply " + content);
//        ctx.writeAndFlush(content);
//    }

    private void writeByteBuf(ChannelHandlerContext ctx, String content){
        System.out.println("wrap byte buffer and reply: " + content);
        ByteBuf buffer = Unpooled.buffer(128);
        buffer.writeBytes(content.getBytes());
        ctx.writeAndFlush(buffer);
    }

   // example : 1 -> :1
    private void integerWriteByteBuf(ChannelHandlerContext ctx, int i) {
        writeByteBuf(ctx, integerEncode(i));
    }


    //example : 1 -> :1
    private static String integerEncode(int i) {
        return ":" + i + CRLF;
    }

    private void array(ChannelHandlerContext ctx, String[] array) {
        writeByteBuf(ctx, arrayEncode(array));
    }

    private static String arrayEncode(Object[] array) {
        StringBuilder sb = new StringBuilder();
        if(array == null) {
            sb.append("*-1" + CRLF);
        } else if(array.length == 0) {
            sb.append("*0" + CRLF);
        } else {
            sb.append("*" + array.length + CRLF);
            for(int i=0; i<array.length; i++) {
                Object obj = array[i];
                if(obj == null) {
                    sb.append("$-1" + CRLF);
                } else {
                    if(obj instanceof Integer) {
                        sb.append(integerEncode((Integer) obj));
                    } else if(obj instanceof String) {
                        sb.append(bulkStringEncode((String) obj));
                    } else if(obj instanceof Object[] objs){
                        sb.append(arrayEncode(objs));
                    }
                }
            }
        }
        return sb.toString();
    }

    //批量操作
    private static String bulkStringEncode(String content) {
        String ret;
        if (content == null) {
            ret = "$-1";
        } else if (content.isEmpty()) {
            ret = "$0" + CRLF;
        } else {
            ret = BULK_PREFIX + content.getBytes().length + CRLF + content;
        }
        return ret + CRLF;
    }
}
