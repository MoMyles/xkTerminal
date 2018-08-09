package com.cetcme.xkterminal.netty.service;

import android.os.Bundle;
import android.os.Message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.DataHandler;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.netty.utils.ByteUtil;
import com.cetcme.xkterminal.netty.utils.Constants;
import com.cetcme.xkterminal.netty.utils.SendMsg;
import com.cetcme.xkterminal.netty.utils.Constants.MessageType;
import com.cetcme.xkterminal.netty.utils.TcpUtil;

import io.netty.channel.ChannelHandlerContext;

public class MessageService {

	Logger logger = LoggerFactory.getLogger(MessageService.class);
	
	DataHandler dataHandler;

	public void processTcp(byte[] buffer, String message, ChannelHandlerContext ctx) {
		try {
			String header = TcpUtil.getHeader(message);
			MessageType messageType = MessageType.getEnumByValue(header);

			if (messageType == null) {
				logger.info(header+":"+message+"=====无效的消息");
			}

//			String deviceNo = StringUtils.substring(message, 4, 12);
//			logger.info("---{} 消息处理 START {} ------", deviceNo,
//					DateUtil.parseDateToString(new Date(), DatePattern.YYYYMMDDHHMMSSSS));

			if (messageType == MessageType.PRIMARY_STATUS_MSG
					|| messageType == MessageType.PUNCH_MSG
					|| messageType == MessageType.SECONDARY_STATUS_MSG
					|| messageType == MessageType.DEVICE_CHANNEL_RECEIVE_MSG
					|| messageType == MessageType.DEVICE_EMERGENCY_ALARM) {
				if(StringUtils.contains(message, Constants.RESPONSE_OK)) {
					buffer = null;
				}
			}else if(messageType == MessageType.DEVICE_CHANNEL_SEND_MSG) {
				if(StringUtils.contains(message, Constants.RESPONSE_OK)) {
					buffer = null;
				}else {
					String headerOne = "$04";
					byte[] head04 = headerOne.getBytes();
					byte[] bufferPage = ByteUtil.subBytes(buffer, 3, buffer.length - 2);
					byte[] buffer1 = ByteUtil.byteMerger(head04, bufferPage);
					int checkSum = TcpUtil.computeCheckSum(buffer1, 3, buffer1.length);
					byte[] checkSum1 = new byte[] {(byte) checkSum};
					byte[] checkSumA = ByteUtil.byteMerger("*".getBytes(), checkSum1);
					byte[] checkSumB = ByteUtil.byteMerger(checkSumA, Constants.MESSAGE_END_SYMBOL.getBytes());
					buffer = ByteUtil.byteMerger(buffer1, checkSumB);
					
					SendMsg sm = SendMsg.getSendMsg();
	                sm.responseTcpOk(ctx, messageType.toString(), null);
				}
			}else if(messageType == MessageType.CONFIG_DEVICE_REQUEST
					|| messageType == MessageType.DEVICE_CHANNEL_SEND_MSG
					|| messageType == MessageType.BOOTLOADER_MSG_REQUEST
					|| messageType == MessageType.CONFIG_ID_CARD_READER_REQUEST
					|| messageType == MessageType.CHANGE_DEVICE_IP_REQUEST
					|| messageType == MessageType.CHANGE_DEVICE_BD_ADDRESS){
				String deviceNo = StringUtils.substring(message, 4, 12);
				SendMsg sm = SendMsg.getSendMsg();
                sm.responseTcpOk(ctx,  messageType.toString(), deviceNo);
                
                buffer = null;
			}else if(messageType == MessageType.SECONDARY_STATUS_MSG_REQUEST) {
				String deviceNo = StringUtils.substring(message, 4, 12);
				SendMsg sm = SendMsg.getSendMsg();
                sm.send03(ctx, deviceNo);
                
                buffer = null;
			}
//			logger.info("---{} 消息处理 END {} ----", deviceNo,
//					DateUtil.parseDateToString(new Date(), DatePattern.YYYYMMDDHHMMSSSS));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//执行串口输出方法
		if(buffer != null) {
			if (dataHandler == null) dataHandler = MyApplication.getInstance().getHandler();
			processBuffer(buffer);
		}
	}
	
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
