package com.cetcme.xkterminal.netty.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class CustomDecoder extends ByteToMessageDecoder {

	private final int frameLength;
    
    private final static String PRIMARY_STATUS_MSG = "364849";//$01
    private final static String PUNCH_MSG = "364850";//$02
    private final static String SECONDARY_STATUS_MSG = "364851";//$03
    private final static String DEVICE_CHANNEL_RECEIVE_MSG = "364852";//$04
    private final static String DEVICE_EMERGENCY_ALARM = "364853";//$05
    private final static String CONFIG_DEVICE_REQUEST = "365648";//$80
    private final static String DEMO_TEST = "365650";//$82
    private final static String SECONDARY_STATUS_MSG_REQUEST = "365651";//$83
    private final static String DEVICE_CHANNEL_SEND_MSG = "365652";//$84
    private final static String BOOTLOADER_MSG_REQUEST = "365653";//$85
    private final static String CONFIG_ID_CARD_READER_REQUEST = "365654";//$86
    private final static String CHANGE_DEVICE_IP_REQUEST = "365655";//$87
    private final static String CHANGE_DEVICE_BD_ADDRESS = "365656";//$87
    private final static String BOOTLOADER_WRITEFLASH_MSG = "365748";//$90
    private final static String BOOTLOADER_CHECKSUM_MSG = "365749";//$91
    
    private final static String SERVER_CHANNEL_RECEIVE_MSG = "366565";//$AA
    private final static String BD_SERVER_IP_HOST = "366566";//$AB
    private final static String DEVICE_EMERGENCY_ALARM_VARIA = "366567";//$AC
    private final static String DEVICE_MOBILE_SEND = "366577";//$AM
    
    @SuppressWarnings("serial")
	private final static Map<String, String> commandMap = new HashMap<String, String>() {
    	{
    		put(PRIMARY_STATUS_MSG, "1");
    		put(PUNCH_MSG, "1");
    		put(SECONDARY_STATUS_MSG, "1");
    		put(DEVICE_CHANNEL_RECEIVE_MSG, "1");
    		put(DEVICE_EMERGENCY_ALARM, "1");
    		put(CONFIG_DEVICE_REQUEST, "1");
    		put(SECONDARY_STATUS_MSG_REQUEST, "1");
    		put(DEVICE_CHANNEL_SEND_MSG, "1");
    		put(BOOTLOADER_MSG_REQUEST, "1");
    		put(CONFIG_ID_CARD_READER_REQUEST, "1");
    		put(CHANGE_DEVICE_IP_REQUEST, "1");
    		put(CHANGE_DEVICE_BD_ADDRESS, "1");
    		put(BOOTLOADER_WRITEFLASH_MSG, "1");
    		put(BOOTLOADER_CHECKSUM_MSG, "1");
    		
    		put(SERVER_CHANNEL_RECEIVE_MSG, "1");
    		put(BD_SERVER_IP_HOST, "1");
    		put(DEVICE_EMERGENCY_ALARM_VARIA, "1");
    		put(DEVICE_MOBILE_SEND, "1");
    		
    		put(DEMO_TEST, "1");
    	}
    };
    
	public CustomDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException(
                    "frameLength must be a positive integer: " + frameLength);
        }
        this.frameLength = frameLength;
    }
	
	@Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(in.readableBytes() > 3){
			int FixedLength = this.decodeRemove(in);
			if(FixedLength > 0){
				Object decoded = decodeFLFD(ctx, in, FixedLength);
				if (decoded != null) {
					out.add(decoded);
					in.skipBytes(2);//跳过回车和换行
				}
			}
    	}
    }
	
	/**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param   ctx             the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param   in              the {@link ByteBuf} from which to read data
     * @return  frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     *                          be created.
     */
    protected Object decodeFLFD(ChannelHandlerContext ctx, ByteBuf in, int FixedLength) throws Exception {
    	System.out.println("decodeFLFD==================="+in.readableBytes()+":"+FixedLength);
    	if (in.readableBytes() < FixedLength) {
            return null;
        }else if (frameLength < FixedLength) {
        	return in.readSlice(frameLength).retain();
        } else {
            return in.readSlice(FixedLength-2).retain();//不截取回车和换行
        }
    }
    
    public static int byteToInt(byte b) {  
        return b & 0xFF;  
    }
    
    public static int indexOf(ByteBuf in, int forType) {
    	System.out.println("indexOf==================="+in.readerIndex()+":"+in.writerIndex());
    	int readerIndex = in.readerIndex();
    	for (int i = readerIndex; i < in.readableBytes() + readerIndex - 2; i++) {
    		StringBuilder sb = new StringBuilder();
			sb.append(in.getByte(i));
			sb.append(in.getByte(i+1));
			sb.append(in.getByte(i+2));
    		String header = sb.toString();
			if(commandMap.get(header) != null){
				System.out.println("commandMap==================="+header+":"+i);
				return i - readerIndex;
			}
        }
    	
        return -1;
    }
    
    private int decodeRemove(ByteBuf in){
    	System.out.println("header===================1:"+in.readableBytes()+"||"+in.getByte(0)+in.getByte(1)+in.getByte(2));
    	int FixedLength = 0;
    	in.markReaderIndex();//备份read指针到markRead对象中
    	int position = indexOf(in, 1);
    	in.resetReaderIndex();//还原read指针位置
    	System.out.println("position===================1:"+position);
    	if(position > 0){
    		in.skipBytes(position).retain();//去除没有开头的部分
    	}else if(position == -1){
    		String message = "";
    		for(int i=in.readerIndex();i<in.writerIndex();i++) {
				int multiple = in.readByte();
				message += multiple + ",";
			}
			System.out.println("messageA========"+message);
    		in.skipBytes(in.readableBytes()).retain();//去除没有开头的全部
    	}
    	System.out.println("header===================2:"+in.readableBytes());
    	if(in.readableBytes() == 0) {
    		return 0;
    	}
    	in.markReaderIndex();//备份read指针到markRead对象中
    	in.skipBytes(3);
    	int positionAgain = indexOf(in, 2);
    	in.resetReaderIndex();//还原read指针位置
    	int againFlag = positionAgain;
    	if(positionAgain == -1){
    		positionAgain = in.readableBytes();
    	}
    	System.out.println("header===================3:"+positionAgain);
    	boolean prevAgreement = false;boolean prevAgreementOK = false;
    	if(positionAgain > 2){
    		in.markReaderIndex();//备份read指针到markRead对象中
    		in.skipBytes(positionAgain - 2);
    		StringBuilder sb = new StringBuilder();
    		sb.append(in.readByte());
    		sb.append(in.readByte());
    		String agreement = sb.toString();
    		if("1310".equals(agreement)) {
    			if(positionAgain == 40){
    				prevAgreement = true;
    			}
    			prevAgreementOK = true;
    		}
    		in.resetReaderIndex();//还原read指针位置
    	}
    	System.out.println("header===================4:"+positionAgain);
    	in.markReaderIndex();//备份read指针到markRead对象中
    	if(in.readableBytes() > 3){
    		//清除$开始符号的部分后
    		StringBuilder sb = new StringBuilder();
    		sb.append(in.readByte());
    		sb.append(in.readByte());
    		sb.append(in.readByte());
    		String header = sb.toString();
    		int maxLength = 0;
    		System.out.println("header===================5:"+positionAgain);
    		if(positionAgain >= 12){
    			System.out.println("header==================="+header);
    			switch (header) {
	    			case PRIMARY_STATUS_MSG:
	    				if(prevAgreement){
	    					maxLength = 37;
	    				}else{
	    					in.skipBytes(10);
	    					if(positionAgain > 13) {
	    						byte multiple = in.readByte();
	    						maxLength = 14 + byteToInt(multiple) * 25;
	    					}else {
	    						maxLength = 39;
	    					}
	    				}
	    				break;
	    			case PUNCH_MSG:
	    				maxLength = 62;
	    				break;
	    			case DEVICE_CHANNEL_RECEIVE_MSG:
	    				in.skipBytes(11);
    					byte multiple = in.readByte();
    					int lengthOne = Integer.parseInt(Integer.toBinaryString((multiple & 0x3F) + 0x100).substring(1), 2);
    					System.out.println("decodeRemove04========"+lengthOne);
    					maxLength = lengthOne + 16;
	    				break;
	    			case DEVICE_MOBILE_SEND:
	    				in.skipBytes(12);
    					byte multiple1 = in.readByte();
    					int lengthOne1 = Integer.parseInt(Integer.toBinaryString((multiple1 & 0x3F) + 0x100).substring(1), 2);
    					System.out.println("decodeRemoveAM========"+lengthOne1);
    					maxLength = lengthOne1 + 17;
	    				break;
	    			case SECONDARY_STATUS_MSG:
	    			case SECONDARY_STATUS_MSG_REQUEST:
	    				maxLength = 59;
	    				break;
	    			case CONFIG_DEVICE_REQUEST:
	    			case BOOTLOADER_MSG_REQUEST:
	    			case CONFIG_ID_CARD_READER_REQUEST:
	    			case CHANGE_DEVICE_IP_REQUEST:
	    			case CHANGE_DEVICE_BD_ADDRESS:
	    			case BOOTLOADER_CHECKSUM_MSG:
	    			case DEVICE_CHANNEL_SEND_MSG:
	    			case BD_SERVER_IP_HOST:
	    			case DEMO_TEST:
						maxLength = 12;
						if (header.equals(DEVICE_CHANNEL_SEND_MSG) && in.readableBytes() > 13) {
							in.skipBytes(11);
							multiple = in.readByte();
							lengthOne = Integer.parseInt(Integer.toBinaryString((multiple & 0x3F) + 0x100).substring(1), 2);
							System.out.println("decodeRemove04========" + lengthOne);
							maxLength = lengthOne + 16;
						}
						break;
	    			case BOOTLOADER_WRITEFLASH_MSG:
	    				maxLength = 15;
	    				break;
	    			case SERVER_CHANNEL_RECEIVE_MSG:
	    				in.skipBytes(6);
    					byte multiple2 = in.readByte();
    					int lengthOne2 = Integer.parseInt(Integer.toBinaryString((multiple2 & 0x3F) + 0x100).substring(1), 2);
    					System.out.println("decodeRemove========"+lengthOne2);
    					maxLength = lengthOne2 + 11;
	    				break;
	    			case DEVICE_EMERGENCY_ALARM_VARIA:
	    				maxLength = 12;
	    				break;
	    			default:
	    				break;
    			}
    		}else if(positionAgain == 9 && DEVICE_EMERGENCY_ALARM.equals(header)){
    			maxLength = 6;
    		}else if(prevAgreementOK && BD_SERVER_IP_HOST.equals(header)) {
    			maxLength = 4;
    		}
    		
    		System.out.println(positionAgain+"=========decodeRemove========"+maxLength);
    		if(positionAgain >= maxLength && maxLength > 0){
    			FixedLength = maxLength + 3;
    		}
    	}
		in.resetReaderIndex();//还原read指针位置
		
		if(FixedLength == 0) {
			in.markReaderIndex();
			String message = "";
			if(againFlag > 0) {
				positionAgain = positionAgain + 6;
			}
			for(int i=0;i<positionAgain;i++) {
				int multiple = in.readByte();
				message += multiple + ",";
			}
			System.out.println("message========"+message);
			in.resetReaderIndex();//还原read指针位置
			
			if(againFlag > 0) {
				in.skipBytes(againFlag).retain();
			}
		}
		
		return FixedLength;
    }

}
