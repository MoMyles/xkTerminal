package com.cetcme.xkterminal.DataFormat.Util;


public class ByteUtil {


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

	public static byte[] byteMerger(byte[] data1, byte[] data2) {
		byte[] result = new byte[data1.length + data2.length];
		System.arraycopy(data1, 0, result, 0, data1.length);
		System.arraycopy(data2, 0, result, data1.length, data2.length);
		return result;
	}

	public static int computeCheckSum(byte[] buf, int start, int end) {
		int sum = 0;
		for (int i = start; i < end; i++) {
			sum += (buf[i] + 256) % 256;
		}
		return (sum & 0xFF);
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

	public static String byte2Str(byte b) {
		String hexStr = Integer.toString(b, 16);
		if (hexStr.length() == 1) {
			hexStr = "0" + hexStr;
		} else if (hexStr.length() > 2) {
			hexStr = hexStr.substring(hexStr.length() - 2);
		}
		return hexStr;
	}
}
