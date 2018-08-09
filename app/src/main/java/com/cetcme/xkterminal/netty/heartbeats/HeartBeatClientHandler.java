package com.cetcme.xkterminal.netty.heartbeats;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cetcme.xkterminal.GPSLocation;
import com.cetcme.xkterminal.netty.service.MessageService;
import com.cetcme.xkterminal.netty.utils.CacheUtil;
import com.cetcme.xkterminal.netty.utils.SendMsg;
import com.cetcme.xkterminal.netty.utils.Constants.CacheType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
 
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(HeartBeatClientHandler.class);
    String deviceNo = "66666666"; //"77777777";

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("激活时间是："+new Date());
        System.out.println("HeartBeatClientHandler channelActive");
        ctx.fireChannelActive();
        CacheUtil.put(CacheType.NETTY_APP_CTX, "nettyAppCtx", ctx);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SendMsg sm = SendMsg.getSendMsg();
                sm.send03(ctx, deviceNo);

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                double longGPS = GPSLocation.getLon();
                double latGPS = GPSLocation.getLat();
                SendMsg sm1 = SendMsg.getSendMsg();
                sm1.send01(ctx, deviceNo, longGPS, latGPS);
            }
        }).start();
    }
 
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("停止时间是："+new Date());
        System.out.println("HeartBeatClientHandler channelInactive");
        //TODO 重启netty
    }
 
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("循环触发时间："+new Date());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                //参数应从全局变量中获得
                double longGPS = GPSLocation.getLon();
                double latGPS = GPSLocation.getLat();
                SendMsg sm = SendMsg.getSendMsg();
                sm.send01(ctx, deviceNo, longGPS, latGPS);
            }
        }
    }
 
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	// Discard the received data silently.
		String host = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
		int port = ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
		try {
			ByteBuf in = (ByteBuf) msg;
			byte[] buff = new byte[in.readableBytes()];
			in.readBytes(buff);
			StringBuilder sb = new StringBuilder();
			for(byte b:buff){
				sb.append(b);
				sb.append(",");
			}
			logger.info(host+":"+port+"|收到指令2:" + sb.toString());
			String message = "";
			try {
				message = new String(buff, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("接收数据出错:" + e.getMessage());
			}

			new MessageService().processTcp(buff, message, ctx);
		} finally {
			ReferenceCountUtil.release(msg); // (2)
		}
    }
}
