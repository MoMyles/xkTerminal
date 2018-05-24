package com.cetcme.xkterminal.DataFormat.Util;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

//import org.apache.commons.lang3.StringUtils;


public class ConvertUtil {

	// 经纬度度分秒转换为小数
	public static double convertToDouble(double du, double fen, double miao) {
		if (du < 0) {
			return -(Math.abs(du) + (Math.abs(fen) + (Math.abs(miao) / 60)) / 60);
		}
		return Math.abs(du) + (Math.abs(fen) + (Math.abs(miao) / 60)) / 60;
	}

	/*
	 * 格式化经纬度
	 */
	public static String parseLonAndLat(String value, String type) {
		int pointIndex = value.indexOf(".");
		String degree = value.substring(0, pointIndex - 2);
		String minute = value.substring(pointIndex - 2);
		String resultValue = Double
				.toString(convertToDouble(Double.parseDouble(degree), Double.parseDouble(minute), 0.0));
		if ("W".equals(type) || "S".equals(type)) {
			resultValue = "-" + resultValue;
		}
		return resultValue;
	}

	public static String utc2Local(String utcTime) {
		return utc2Local(utcTime, DateUtil.DatePattern.UTC, DateUtil.DatePattern.LOCAL);
	}

	public static Date utc2LocalDate(String utcTime) {
		return utc2LocalDate(utcTime, DateUtil.DatePattern.UTC, DateUtil.DatePattern.LOCAL);
	}

	public static Date utc2LocalDate(String utcTime, DateUtil.DatePattern utcTimePatten, DateUtil.DatePattern localTimePatten) {
		SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten.toString());
		utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));// 时区定义并进行时间获取
		Date gpsUTCDate = null;
		try {
			gpsUTCDate = utcFormater.parse(utcTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten.toString());
		localFormater.setTimeZone(TimeZone.getDefault());
		return gpsUTCDate;
	}

	/*
	 * UTC格式时间转本地时区
	 */
	public static String utc2Local(String utcTime, DateUtil.DatePattern utcTimePattern, DateUtil.DatePattern localTimePattern) {
		Date date = utc2LocalDate(utcTime, utcTimePattern, localTimePattern);
		return DateUtil.parseDateToString(date);
	}

	private static byte uniteBytes(String src0, String src1) {
		byte b0 = Byte.decode("0x" + src0).byteValue();
		b0 = (byte) (b0 << 4);
		byte b1 = Byte.decode("0x" + src1).byteValue();
		byte ret = (byte) (b0 | b1);
		return ret;
	}

	/**
	 * 十六进制字符串转换成bytes
	 */
	public static byte[] hexStr2Bytes(String src) {
		int m = 0, n = 0;
		int l = src.length() / 2;
		System.out.println(l);
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
		}

		return ret;
	}

	/**
	 * String的字符串转换成unicode的String
	 */
	public static String str2Unicode(String strText) {
		char c;
		String strRet = "";
		int intAsc;
		String strHex;
		for (int i = 0; i < strText.length(); i++) {
			c = strText.charAt(i);
			intAsc = c;
			strHex = StringUtils.leftPad(Integer.toHexString(intAsc), 2, "0");
			strRet += strHex;
			// if (intAsc > 128) {
			// strRet += "//u" + strHex;
			// } else {
			// // 低位在前面补00
			// strRet += "//u00" + strHex;
			// }
		}
		return strRet;
	}

	public static String bcd2Bytes(String str) {

		String arr[] = str.split("");
		String retStr = "";
		try {
			for (int i = 1; i < arr.length; i++) {
				String tempStr;

				tempStr = str2Unicode(arr[i]);

				retStr = retStr + tempStr;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retStr;

	}

	/**
	 * 十六进制转二进制
	 *
	 * @param hex
	 * @return
	 */
	public static String hexToBinary(String hex) {
		return Integer.toBinaryString(Integer.valueOf(hex, 16));
	}

	public static String fen2Miao(String fen) {
		String retStr = "";
		retStr = Float.toString(Float.parseFloat(fen) * (float) 60.0);
		int pointIndex = retStr.indexOf(".");
		if (pointIndex > 0 && retStr.substring(pointIndex).length() > 3) {
			retStr = retStr.substring(0, pointIndex + 2);
		}
		return retStr;
	}

	/**
	 *
	 * 将String转换成为Double
	 *
	 * @param strValue
	 * @return
	 * @author
	 */
	public static Double string2Double(String strValue) {
		if (CommonUtil.isDouble(strValue)) {
			return Double.parseDouble(strValue);
		}
		return 0.0;
	}

	/**
	 *
	 * 将String转换成为Double
	 *
	 * @param strValue
	 * @return
	 * @author
	 */
	public static Integer string2Int(String strValue) {
		if (StringUtils.isNumeric(strValue)) {
			return Integer.parseInt(strValue);
		}
		return 0;
	}

	public static Integer hexStr2Int(String strValue) {
		return Integer.valueOf(strValue, 16);
	}

	public static java.sql.Date utilDate2SqlDate(Date date) {
		return new java.sql.Date(date.getTime());
	}

	public static Timestamp utilDate2SqlTimestamp(Date date) {
		return new Timestamp(date.getTime());
	}

	/**
	 * @功能: BCD码转为10进制串(阿拉伯数据)
	 * @参数: BCD码
	 * @结果: 10进制串
	 */
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
	}

	/**
	 * @功能: 10进制串转为BCD码
	 * @参数: 10进制串
	 * @结果: BCD码
	 */
	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;
		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}
		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}
		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;
		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}
			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}
			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	/*
	 * 把16进制字符串转换成字节数组
	 *
	 * @param hex
	 *
	 * @return
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/** */
	/**
	 * 把字节数组转换成16进制字符串
	 *
	 * @param bArray
	 * @return
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	public static String turnIdbytesToString(byte[] buffer, int beginIdx, int len) {
		String id = "";
		for (int i = 0; i < len; i++) {
			int c = buffer[beginIdx + i];
			if (c < 0)
				c += 256;
			id += Integer.toHexString(c >> 4);
			int last = c & 0xf;
			if (last == 0xa) {
				id += "X";
			} else {
				id += Integer.toHexString(last);
			}
		}
		return id;
	}

	public static String turnHexBytesToString(byte[] buffer, int beginIdx, int len) {
		String id = "";
		for (int i = 0; i < len; i++) {
			int c = buffer[beginIdx + i];
			if (c < 0)
				c += 256;
			id += Integer.toHexString(c >> 4);
			int last = c & 0xf;
			id += Integer.toHexString(last);
		}
		return id;
	}

	public static String turnNameBytesToString(byte[] buffer, int beginIdx) throws UnsupportedEncodingException {
		String name = "";
		int i = 0;
		for (; i < 30; i++) {
			// 取空格前的字段
			int highByte = buffer[beginIdx + i];
			int lowByte = buffer[beginIdx + i + 1];
			if (highByte == 32 && lowByte == 0) {
				break;
			}
			i = i + 2;
		}
		if (i >= 0 && i % 2 == 0) {
			byte[] data = new byte[i];
			for (int j = 0; j < i; j += 2) {
				data[j] = buffer[beginIdx + j + 1];
				data[j + 1] = buffer[beginIdx + j];
			}
			name = new String(data, "UTF-16BE");
			name = new String(name.getBytes("UTF-8"));
		}
		return name;
	}

	public static String stringToAscii(String value) {
		StringBuffer sbu = new StringBuffer();
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i != chars.length - 1) {
				sbu.append((int) chars[i]).append(",");
			} else {
				sbu.append((int) chars[i]);
			}
		}
		return sbu.toString();
	}

	public static String asciiToString(String value) {
		StringBuffer sbu = new StringBuffer();
		String[] chars = value.split(",");
		for (int i = 0; i < chars.length; i++) {
			sbu.append((char) Integer.parseInt(chars[i]));
		}
		return sbu.toString();
	}

	public static String turnBytesToString(byte[] data, int start, int len) {
		StringBuffer buff = new StringBuffer();
		buff.append("[");
		for (int i = start; i < start + len; i++) {
			int c = data[i] & 0xff;
			String hexStr = StringUtils.leftPad(Integer.toHexString(c), 2, '0');
			if (i > start) {
				buff.append(" ");
			}
			buff.append(hexStr);
		}
		buff.append("]");
		return buff.toString();
	}

	public static byte[] stringToBytes(String txt) throws Exception{
		byte[] data=null;

		String[] strings=txt.split(" ");
		data=new byte[strings.length];
		for(int i=0;i<strings.length;i++){
			int v=Integer.parseInt(strings[i],16);
			data[i]=(byte)v;
		}

		return data;
	}

	/**
	 * RC4加密方法
	 *
	 * @param aInput
	 * @param aKey
	 * @return
	 */
	public static String HloveyRC4(String aInput,String aKey) {
		int[] iS = new int[256];
		byte[] iK = new byte[256];

		for (int i=0;i<256;i++)
			iS[i]=i;

		int j = 1;

		for (short i= 0;i<256;i++)
		{
			iK[i]=(byte)aKey.charAt((i % aKey.length()));
		}

		j=0;

		for (int i=0;i<255;i++)
		{
			j=(j+iS[i]+iK[i]) % 256;
			int temp = iS[i];
			iS[i]=iS[j];
			iS[j]=temp;
		}


		int i=0;
		j=0;
		char[] iInputChar = aInput.toCharArray();
		char[] iOutputChar = new char[iInputChar.length];
		for(short x = 0;x<iInputChar.length;x++)
		{
			i = (i+1) % 256;
			j = (j+iS[i]) % 256;
			int temp = iS[i];
			iS[i]=iS[j];
			iS[j]=temp;
			int t = (iS[i]+(iS[j] % 256)) % 256;
			int iY = iS[t];
			char iCY = (char)iY;
			iOutputChar[x] =(char)( iInputChar[x] ^ iCY) ;
		}

		return new String(iOutputChar);
	}

	/**
	 * 生成唯一的10位字符串
	 *
	 * @return
	 */
	public static String rc4ToHex() {
		Calendar Cld = Calendar.getInstance();
		int MI = Cld.get(Calendar.MILLISECOND);
		String rc4 = HloveyRC4(String.format("%05d", MI), UUID.randomUUID().toString()).toUpperCase();
		String keyOne = ConvertUtil.stringToHexString(rc4);
		if(keyOne.length() == 10){
			return keyOne;
		}else if(keyOne.length() < 10){
			String repairZero ="0000000000";
			keyOne = repairZero.substring(0, 10 - keyOne.length()) + keyOne;
			return keyOne;
		}else {
			return rc4ToHex();
		}
	}

	/**
	 * 字符串转换为16进制字符串
	 *
	 * @param s
	 * @return
	 */
	public static String stringToHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}
}
