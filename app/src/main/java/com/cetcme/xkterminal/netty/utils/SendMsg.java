package com.cetcme.xkterminal.netty.utils;

import android.os.Bundle;
import android.os.Message;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.DataHandler;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.netty.utils.Constants.CacheType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class SendMsg {

    private DataHandler dataHandler;

	private static volatile SendMsg sendMsg = null;
    
    private SendMsg(){}
     
    public static SendMsg getSendMsg(){
        if(sendMsg == null){
            synchronized (SendMsg.class){
                if(sendMsg == null){
                	sendMsg = new SendMsg();
                }
            }
        }
        
        return sendMsg;
    }
    
    //类似向串口发送数据
    public String sendMsg(byte[] bytes) {
        if (dataHandler == null) dataHandler = MyApplication.getInstance().getHandler();
		synchronized (this) {
			ChannelHandlerContext ctx = (ChannelHandlerContext) CacheUtil.get(CacheType.NETTY_APP_CTX, "nettyAppCtx");
			String header = TcpUtil.getHeader(bytes);
			Constants.MessageType messageType = Constants.MessageType.getEnumByValue(header);
			byte[] bytesNew = null;
			switch (messageType) {
				case PRIMARY_STATUS_MSG:
					//对$01的回复
					bytesNew = this.sendR1();
					//发送给串口
					break;
				case PUNCH_MSG:
				case SECONDARY_STATUS_MSG:
					//对$02和$03的回复
					bytesNew = this.sendR2();
					//发送给串口
					break;
				case AUTOMATIC_DETECTION:
					break;
				default:
					if(ctx != null) {
						ByteBuf responseMsgBuf = Unpooled.copiedBuffer(bytes);
						ctx.channel().writeAndFlush(responseMsgBuf);
                        bytesNew = this.sendR4();
					}else {
						return "找不到服务器地址";
					}
					break;
			}

			try {
				TimeUnit.MICROSECONDS.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			processBuffer(bytesNew);
			return "发送成功";
		}
	}
	
	public void responseTcpOk(ChannelHandlerContext ctx, String header, String deviceNo) {
		synchronized (this) {
			String str = header + (deviceNo!=null?deviceNo:"");
			String responseMsg = String.format(Constants.COMMON_RESPONSE_MSG_FORMAT, str);
			ByteBuf responseMsgBuf = Unpooled.copiedBuffer(responseMsg.getBytes());
			ctx.channel().writeAndFlush(responseMsgBuf);
			try {
				TimeUnit.MICROSECONDS.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send01(ChannelHandlerContext ctx, String deviceNo, double longGPS, double latGPS) {
		synchronized (this) {
			byte[] bytes = getVirtualinfo(deviceNo, longGPS, latGPS);
			bytes = TcpUdpClientSocket.makeSum(bytes);
			ByteBuf responseMsgBuf = Unpooled.copiedBuffer(bytes);
			ctx.channel().writeAndFlush(responseMsgBuf);
			try {
				TimeUnit.MICROSECONDS.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] sendR1() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		synchronized (this) {
			String header = "$R112345678";
			byte[] headerB = header.getBytes();
			byte year = (byte) (cal.get(Calendar.YEAR) - 2000);
			byte month = (byte) (cal.get(Calendar.MONTH) + 1);
			byte day = (byte) cal.get(Calendar.DAY_OF_MONTH);
			byte hour = (byte) cal.get(Calendar.HOUR_OF_DAY);
			byte minute = (byte) cal.get(Calendar.MINUTE);
			byte second = (byte) cal.get(Calendar.SECOND);
			byte[] timsB = {year, month, day, hour, minute, second};
			byte[] buffer3 = ByteUtil.byteMerger(headerB, timsB);
			byte[] beidouNo = ByteUtil.intToByte4(747474);
			byte[] buffer2 = ByteUtil.byteMerger(buffer3, beidouNo);
			byte[] flag = {0x01};
			byte[] buffer1 = ByteUtil.byteMerger(buffer2, flag);
			int checkSum = TcpUtil.computeCheckSum(buffer1, 3, buffer1.length);
			byte[] checkSum1 = new byte[] {(byte) checkSum};
			byte[] checkSumA = ByteUtil.byteMerger("*".getBytes(), checkSum1);
			byte[] checkSumB = ByteUtil.byteMerger(checkSumA, Constants.MESSAGE_END_SYMBOL_ONE.getBytes());
			byte[] buffer = ByteUtil.byteMerger(buffer1, checkSumB);
			return buffer;
		}
	}

	public byte[] sendR2() {
		synchronized (this) {
			String header = "$R212345678OK";
			byte[] buffer1 = header.getBytes();
			int checkSum = TcpUtil.computeCheckSum(buffer1, 3, buffer1.length);
			byte[] checkSum1 = new byte[] {(byte) checkSum};
			byte[] checkSumA = ByteUtil.byteMerger("*".getBytes(), checkSum1);
			byte[] checkSumB = ByteUtil.byteMerger(checkSumA, Constants.MESSAGE_END_SYMBOL_ONE.getBytes());
			byte[] buffer = ByteUtil.byteMerger(buffer1, checkSumB);
			return buffer;
		}
	}

    public byte[] sendR4() {
        synchronized (this) {
            String header = "$R412345678OK";
            byte[] buffer1 = header.getBytes();
            int checkSum = TcpUtil.computeCheckSum(buffer1, 3, buffer1.length);
            byte[] checkSum1 = new byte[] {(byte) checkSum};
            byte[] checkSumA = ByteUtil.byteMerger("*".getBytes(), checkSum1);
            byte[] checkSumB = ByteUtil.byteMerger(checkSumA, Constants.MESSAGE_END_SYMBOL_ONE.getBytes());
            byte[] buffer = ByteUtil.byteMerger(buffer1, checkSumB);
            return buffer;
        }
    }
	
	private static byte[] getVirtualinfo(String deviceNo, double longGPS, double latGPS) {
		byte[] ret = new byte[1024];
		int len = 0;
		Random random = new Random();
		double lat = latGPS + random.nextDouble() / 100;
		double lon = longGPS + random.nextDouble() / 100;
		String text = "$01,";
		text += deviceNo;
		text += ",";
		byte[] src = text.getBytes();
		int tfn = 2;
		System.arraycopy(src, 0, ret, len, src.length);
		len += src.length;
		ret[len++] = (byte) tfn;
		Calendar cal = Calendar.getInstance();

		for(int i=0;i<tfn;i++)
		{
			ret[len++] = (byte) 0x2C;
			src = TcpUdpClientSocket.getLongStr(lon);
			System.arraycopy(src, 0, ret, len, src.length);
			len += src.length;
			src = TcpUdpClientSocket.getLatStr(lat);
			System.arraycopy(src, 0, ret, len, src.length);
			len += src.length;
			if(tfn == 0) {
				cal.add(Calendar.MINUTE, -1);
				src = TcpUdpClientSocket.utc2Bytes(cal.getTime());
				System.arraycopy(src, 0, ret, len, src.length);
			}else {
				src = TcpUdpClientSocket.utc2Bytes(cal.getTime());
				System.arraycopy(src, 0, ret, len, src.length);
			}
			len += src.length;
			// 航速
			ret[len++] = (byte) 0x01;
			ret[len++] = (byte) 0x85;
			// 航向
			ret[len++] = (byte) 0x12;
			ret[len++] = (byte) 0x38;
			// GPRS信号强度
			ret[len++] = (byte) 0x1a;
			// CPU温度
			ret[len++] = (byte) 0x0a;
			// 锂电池电压
			ret[len++] = (byte) 0xa8;
			// 光伏电池电压
			ret[len++] = (byte) 0xa8;
			// 报警信息
			ret[len++] = (byte) 0x00;
			ret[len++] = (byte) 0x07;
		}
		
		byte[] data = new byte[len];
		System.arraycopy(ret, 0, data, 0, len);
		return data;
	}
	
	public void send02(ChannelHandlerContext ctx, String deviceNo) {
		synchronized (this) {
			byte[] bytes = getVirtualIDinfo(deviceNo);
			byte[] ret = new byte[bytes.length+5];
			System.arraycopy(bytes, 0, ret, 0, bytes.length);
			int s = TcpUdpClientSocket.getBytesSum(bytes,3,bytes.length-3);
			String sum;
			if(s<10){
				sum = "0" +Integer.toHexString(s);
			}else{
				sum = Integer.toHexString(s)+"";
			}
			byte[] hh = sum.getBytes();
			ret[bytes.length] = (byte) 0x2A;
			ret[bytes.length+1] = hh[0];
			ret[bytes.length+2] = hh[1];
			ret[bytes.length+3] = (byte) '\r';
			ret[bytes.length+4] = (byte) '\n';
			ByteBuf responseMsgBuf = Unpooled.copiedBuffer(ret);
			ctx.channel().writeAndFlush(responseMsgBuf);
			try {
				TimeUnit.MICROSECONDS.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private byte[] getVirtualIDinfo(String deviceNo){
		byte[] ret = new byte[1024];
		
		int len=0;
		String text = "$02,";
		text += deviceNo;
		text += ",";
		byte[] src = text.getBytes();
		
		System.arraycopy(src, 0, ret, len, src.length);
		len += src.length;
		src = TcpUdpClientSocket.getIDStr("330402198511200635");
		System.arraycopy(src, 0, ret, len, src.length);
		len += src.length;
		ret[len++] = (byte) 0x2C;
		
		src = TcpUdpClientSocket.utc2Bytes(new Date());
		System.arraycopy(src, 0, ret, len, src.length);
		len += src.length;
		ret[len++] = (byte) 0x2C;
		
		byte[] src30 = new byte[30];
		try {
			System.arraycopy("海电院".getBytes("UnicodeLittleUnmarked"), 0, src30, 0, "海电院".getBytes().length);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.arraycopy(src30, 0, ret, len, src30.length);
		len += src30.length;
		
		byte[] data = new byte[len];
		System.arraycopy(ret, 0, data, 0, len);
		return data;
	}
	
	public void send03(ChannelHandlerContext ctx, String deviceNo) {
		synchronized (this) {
			byte[] bytes = getVirtualinfo(deviceNo);
			ByteBuf responseMsgBuf = Unpooled.copiedBuffer(bytes);
			ctx.channel().writeAndFlush(responseMsgBuf);
			try {
				TimeUnit.MICROSECONDS.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private byte[] getVirtualinfo(String deviceNo){
		StringBuffer bd = new StringBuffer();
		bd.append("$03,");
		bd.append(deviceNo);
		bd.append(",0000018,2.5,06,838300,0078,0078,0078,0000,FF");
		byte[] bytes = bd.toString().getBytes();
		int sum = ByteUtil.checkSum(bytes, 3, bytes.length);
		String sumStr = Integer.toHexString(sum).toUpperCase();
		bd.append("*");
		bd.append(sumStr);
		bd.append("\r\n");
		String reString = bd.toString();
		byte[] resData = reString.getBytes();
		return resData;
	}
	
	public static void main(String[] args) throws Exception {}

    public void processBuffer(byte[] serialBuffer) {
        int serialCount = serialBuffer.length;
        if (serialBuffer[serialCount - 2] == (byte) 0x0D && serialBuffer[serialCount - 1] == (byte) 0x0A) {
            System.out.println("收到包：" + ConvertUtil.bytesToHexString(serialBuffer));
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putByteArray("bytes", serialBuffer);
            switch (Util.bytesGetHead(serialBuffer, 3)) {
                case "$04":
                    // 接收短信
                    message.what = DataHandler.SERIAL_PORT_RECEIVE_NEW_MESSAGE;
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                default:
                    break;
            }
        } else if (serialBuffer[serialCount - 1] == (byte) 0x3B) {
            System.out.println("收到包：" + ConvertUtil.bytesToHexString(serialBuffer));
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putByteArray("bytes", serialBuffer);
            switch (Util.bytesGetHead(serialBuffer, 3)) {
                case "$04":
                    // 接收短信 如果短信内容有分号0x3B 将会进入此处 返回继续接收数据
                    return;
                case "$R4":
                    // 短信发送成功
                    message.what = DataHandler.SERIAL_PORT_MESSAGE_SEND_SUCCESS;
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                case "$R1":
                    // 接收时间
                    if (serialCount == 25) {
                        message.what = DataHandler.SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM;
                    } else if (serialCount == 20) {
                        message.what = DataHandler.SERIAL_PORT_TIME;
                    }
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                case "$R2":
                    // 接收时间
                    message.what = DataHandler.SERIAL_PORT_ID_EDIT_OK;
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                case "$R5":
                    if (serialCount == 14) {
                        // 紧急报警成功
                        message.what = DataHandler.SERIAL_PORT_ALERT_SEND_SUCCESS;
                    } else if (serialCount == 15) {
                        // 显示报警activity
                        message.what = DataHandler.SERIAL_PORT_SHOW_ALERT_ACTIVITY;
                    } else if (serialCount == 16) {
                        // 增加报警记录，显示收到报警
                        message.what = DataHandler.SERIAL_PORT_RECEIVE_NEW_ALERT;
                    }
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                case "$R0":
                    // 接收身份证信息
                    message.what = DataHandler.SERIAL_PORT_RECEIVE_NEW_SIGN;
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                case "$R6":
                    // 调节背光
                    message.what = DataHandler.SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS;
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                case "$R7":
                    // 关机
                    message.what = DataHandler.SERIAL_PORT_SHUT_DOWN;
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                    break;
                case "$R8":
                    if (serialBuffer[3] == 0x01) {
                        System.out.println("报警中");
                        // 报警中
                        message.what = DataHandler.SERIAL_PORT_ALERT_START;
                        message.setData(bundle);
                        dataHandler.sendMessage(message);
                    } else if (serialBuffer[3] == 0x02) {
                        System.out.println("报警失败");
                        // 报警失败
                        message.what = DataHandler.SERIAL_PORT_ALERT_FAIL;
                        message.setData(bundle);
                        dataHandler.sendMessage(message);
                    }
                    break;
                case "$RA":
                    // 自检
                    message.what = DataHandler.SERIAL_PORT_CHECK;
                    message.setData(bundle);
                    dataHandler.sendMessage(message);
                default:
                    break;
            }
        }
    }
}
