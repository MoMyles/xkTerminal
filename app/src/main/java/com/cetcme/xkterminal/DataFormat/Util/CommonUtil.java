package com.cetcme.xkterminal.DataFormat.Util;

import java.util.regex.Pattern;

public class CommonUtil {

	private static final Pattern IP_ADDRESS = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

	public static boolean isTrue(byte value) {
		return (char) value == '1';
	}

	public static boolean isTrue(int value) {
		return value == 1;
	}

	/*
	 * 判断是否为整数
	 * 
	 * @param str 传入的字符串
	 * 
	 * @return 是整数返回true,否则返回false
	 */
	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	/*
	 * 判断是否为浮点数，包括double和float
	 * 
	 * @param str 传入的字符串
	 * 
	 * @return 是浮点数返回true,否则返回false
	 */
	public static boolean isDouble(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
		return pattern.matcher(str).matches();
	}

	public static int getBytesSum(byte[] buffer, int beginIdx, int len) {
		int sum = 0;
		for (int i = 0; i < len; i++) {
			int c = buffer[beginIdx + i];
			if (c < 0) {
				c += 256;
			}
			sum += c;
		}
		sum &= 0xFF;
		return sum;
	}

	public static boolean isIPAddress(String ipAddress) {
		return IP_ADDRESS.matcher(ipAddress).find();
	}

}
