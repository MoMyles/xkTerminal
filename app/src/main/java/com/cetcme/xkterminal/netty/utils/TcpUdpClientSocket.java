package com.cetcme.xkterminal.netty.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TcpUdpClientSocket {
	
	long delay;
	long sendCount;
	long recvCount;
	long sendMills;
	boolean IsRecv=true;
	private DatagramSocket udpSocket = null; 
	private Socket tcpSocket=null;
	boolean bUdpNetTcp=true;
	String serverIp;
	int port;
	
	public TcpUdpClientSocket(boolean bUdpNetTcp,String serverIp,int port) throws Exception{
		this.bUdpNetTcp=bUdpNetTcp;
		this.serverIp=serverIp;
		this.port=port;
		if(bUdpNetTcp){
			udpSocket=new DatagramSocket();
		}else{
			tcpSocket=new Socket(serverIp, port);
		}
	}
	
	public void send(byte[] bytes) throws IOException{
		if(bUdpNetTcp){
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress  
	                .getByName(serverIp), port);  
			udpSocket.send(dp);
		}else{
			OutputStream outputStream=tcpSocket.getOutputStream();
			outputStream.write(bytes);
			outputStream.flush();
		}
	}
	
	public String receive() throws Exception{
		String info="";
		if(bUdpNetTcp){
			byte[] buffer=new byte[1024];
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);  
			udpSocket.receive(dp);  
	        info = new String(dp.getData(), 0, dp.getLength());  
		}else{
			InputStream inputStream=tcpSocket.getInputStream();
			byte[] bts=new byte[1024];
			int idx=0;
			int len=inputStream.available();
			while(len>0){
				inputStream.read(bts,idx,len);
				idx+=len;
				len=inputStream.available();
			}
			
			info=new String(bts,0,idx);
		}
		return info;
	}
	
	public static byte[] getIDStr(String idstr)
	{
		if(idstr.length() != 18)
		{
            Log.e("Netty", "getIDStr: 身份证格式有误");
			return null;
		}
		if(idstr.endsWith("x") || idstr.endsWith("X")){
			idstr = idstr.replace('x','A');
			idstr = idstr.replace('X','A');
			return bcdStr2Bytes(idstr);
		}
		else
		{
			return bcdStr2Bytes(idstr);
		}
		
	}
	
	public static byte[] getLongStr(double ll) {
		double fen = (ll - (int) ll) * 60;
		String fenStr = Long.toString(Math.round(fen * 1000));
		if(fen==0){
			fenStr="00000";
		}
		fenStr=rightPad(fenStr, 5, "0");
		String duStr = Integer.toString((int) ll);
		duStr=leftPad(duStr, 3, "0");
		fenStr = duStr + fenStr;
		
		return bcdStr2Bytes(fenStr);
	}

	public static  byte[] getLatStr(double ll) {
		double fen = (ll - (int) ll) * 60;
		String fenStr = Long.toString(Math.round(fen * 1000));
		if(fen==0){
			fenStr="00000";
		}
		fenStr=rightPad(fenStr, 5, "0");
		String duStr = Integer.toString((int) ll);
		duStr=leftPad(duStr, 2, "0");
		fenStr = duStr + fenStr;
		fenStr = "1" + fenStr;
		
		return bcdStr2Bytes(fenStr);
	}
	
	public static byte[] bcdStr2Bytes(String bcd) {
		int num = bcd.length() / 2;
		byte[] ret = new byte[num];
		for (int i = 0; i < num; i++) {
			String v = bcd.substring(i * 2, i * 2 + 2);
			ret[i] = (byte) Integer.parseInt(v, 16);
		}
		
		return ret;
	}
	
	public static byte[] utc2Bytes(Date time) {
		String dateStr= getUtcTime(time);
		byte[] ret=bcdStr2Bytes(dateStr);
		
		return ret;
	}
	
	public static  String getUtcTime(Date time) {
		// 1��ȡ�ñ���ʱ�䣺
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTimeInMillis(time.getTime());
		// 2��ȡ��ʱ��ƫ������
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);

		// 3��ȡ������ʱ�
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);

		// 4���ӱ���ʱ����۳���Щ������������ȡ��UTCʱ�䣺
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

		Date curTime = new Date(cal.getTimeInMillis());

		SimpleDateFormat utcFormater = new SimpleDateFormat("yyMMddHHmmss");

		return utcFormater.format(curTime);
	}
	
	public static  byte[] makeSum(byte[] buffer) {
		byte[] ret = new byte[buffer.length + 3];
		System.arraycopy(buffer, 0, ret, 0, buffer.length);
		int sum = getBytesSum(buffer, 3, buffer.length - 3);
		ret[buffer.length] = (byte) sum;
		ret[buffer.length + 1] = (byte) '\r';
		ret[buffer.length + 2] = (byte) '\n';
		return ret;
	}

	public static  int getBytesSum(byte[] buffer, int beginIdx, int len) {
		int sum = 0;
		for (int i = 0; i < len; i++) {
			int c = buffer[beginIdx + i] & 0xff;
			sum += c;
			sum &= 0xFF;
		}
		return sum;
	}
	
	public static String leftPad(String src,int len,String symbol){
		String ret=src;
		int num=len-src.length();
		for(int i=0;i<num;i++){
			ret+=symbol;
		}
		return ret;
	}
	
	public static String rightPad(String src,int len,String symbol){
		String ret=src;
		int num=len-src.length();
		for(int i=0;i<num;i++){
			ret=symbol+ret;
		}
		return ret;
	}
	
}
