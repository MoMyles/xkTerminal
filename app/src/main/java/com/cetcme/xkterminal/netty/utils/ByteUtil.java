package com.cetcme.xkterminal.netty.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;

public class ByteUtil {

	public static byte[] intToByte4(int i) {
		byte[] targets = new byte[4];
		targets[3] = (byte) (i & 0xFF);
		targets[2] = (byte) (i >> 8 & 0xFF);
		targets[1] = (byte) (i >> 16 & 0xFF);
		targets[0] = (byte) (i >> 24 & 0xFF);
		return targets;
	}

	public static int indexOf(byte[] array, byte c) {
		for (int i = 0; i < array.length; ++i) {
			if (array[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public static byte[] subBytes(byte[] array, int from, int to) {
		if (from < 0 || to < 0 || to > array.length || from >= to) {
			throw new IllegalArgumentException("Invalid from, to");
		}

		byte[] bytes = new byte[to - from];
		for (int c = 0, i = from; i < to; ++c, ++i) {
			bytes[c] = array[i];
		}
		return bytes;
	}

	public static byte[] subBytes(byte[] array, int from) {
		return subBytes(array, from, array.length);
	}

	public static byte[] getByte(String strValue) {
		try {
			return strValue.getBytes("GB2312");
		} catch (UnsupportedEncodingException e) {
			Log.e("Netty", "字符串转换成BYTE数组出错");
			e.getStackTrace();
		}
		return null;
	}

	public static byte[] byteMerger(byte[] data1, byte[] data2) {
		byte[] result = new byte[data1.length + data2.length];
		System.arraycopy(data1, 0, result, 0, data1.length);
		System.arraycopy(data2, 0, result, data1.length, data2.length);
		return result;
	}
	
	public static int checkSum(byte[] data, int start, int end) {
		int sum = 0;
		for (int i = start; i < end; i++) {
			sum += (data[i] + 256) % 256;
		}
		return (sum & 0xFF);
	}
}
