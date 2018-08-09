package com.cetcme.xkterminal.netty.utils;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;


public class TcpUtil {

	// 取得消息的头信息
	public static String getHeader(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(byte b : bytes){
			if(count > 2)break;
			sb.append(new String(new byte[] {b}));
			count++;
		}
		return sb.toString();
	}

	// 取得消息的头信息
	public static String getHeader(String message) {
		return StringUtils.left(message, Constants.MESSAGE_HEADER_LENGTH);
	}

	public static int computeCheckSum(byte[] buf, int start, int end) {
		int sum = 0;
		for (int i = start; i < end; i++) {
			sum += (buf[i] + 256) % 256;
		}
		return (sum & 0xFF);
	}

	public static int computeCheckSum2(byte[] buf, int start, int end) {
		int sum = 0;
		for (int i = start; i < end; i++) {
			sum += (buf[i] + 256) % 256;
		}
		return (sum & 0xFFFF);
	}

	public static int computeCheckSum(String value) {
		int sum = 0;
		for (int i = 0; i < value.length(); i++) {
			sum += value.charAt(i) & 0xFF;
		}
		return (sum & 0xFF);
	}

	/*
	 * 消息校验
	 */
	public static boolean isValidMessage(byte[] data, int start, int end, int checkSum) {

		// 根据该消息的数据段计算出校验和
		int computedCheckSum = computeCheckSum(data, start, end);

		// 如果计算出的校验和与接收到的校验和相等，则说明该消息是有效的
		return computedCheckSum == checkSum;
	}

	/**
	 * 
	 * @param date
	 * @param delayTime
	 * @return
	 */
	public static boolean isInvalidTime(final Date date, final long delayTimeMs) {

		if (date == null) {
			return true;
		}

		Date current = new Date();

		// 如果时间超过丢弃时间，则为超时无效
		Date minDate;
		Date maxDate;
		if (delayTimeMs > Integer.MAX_VALUE) {
			int delayTimeDays = (int) (delayTimeMs / DateUtils.MILLIS_PER_DAY);
			minDate = DateUtils.addDays(current, -delayTimeDays);
			maxDate = DateUtils.addDays(current, delayTimeDays);
		} else {
			minDate = DateUtils.addMilliseconds(current, (int) -delayTimeMs);
			maxDate = DateUtils.addMilliseconds(current, (int) delayTimeMs);
		}

		return date.before(minDate) || date.after(maxDate);
	}

}
