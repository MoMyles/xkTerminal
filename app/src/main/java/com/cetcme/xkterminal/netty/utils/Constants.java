package com.cetcme.xkterminal.netty.utils;

public class Constants {
	public static final int HEX = 16;

	public static final String COMMA_DELIMITER = ",";

	public static final String MESSAGE_START_SYMBOL = "$";
	public static final String MESSAGE_END_SYMBOL = "\r\n";
	public static final String MESSAGE_END_SYMBOL_ONE = ";";
	public static final int MESSAGE_HEADER_LENGTH = 3;
	public static final int COMMON_MESSAGE_CHECKSUM_LENGTH = 2;
	public static final int HISTORY_MESSAGE_CHECKSUM_LENGTH = 1;
	public static final String MESSAGE_CHECKSUM_DELIMITER = "*";
	public static final String COMMON_RESPONSE_MSG_FORMAT = "%sOK\r\n";

	public static final String RESPONSE_OK = "OK";
	public static final String RESPONSE_ERR = "ER";

	public static final long SYSTEM_USER_ID = 0l;

	public static final String NOTIFICATION_ALARM = "1";
	public static final String NOTIFICATION_COMMAND = "2";
	public static final String NOTIFICATION_ALARM_VIEW = "3";//滚动显示的报警
	public static final String NOTIFICATION_ALARM_VIEW_REMOVE = "4";//滚动显示的报警解除
	public static final String NOTIFICATION_ALARM_URGENT = "5";//紧急报警
	public static final String NOTIFICATION_ALARM_AIS = "6";//AIS报警

	public static final String MUSHROOM_ADDRESS = "372741";
	
	/**
	 * 日期单位枚举
	 */
	public static enum DateUnitType {
		// y(年)、M(月)、d(日)、h(时)、m(分)、s(秒)、ms(毫秒)
		YEAR("y"), MONTH("M"), DAY("d"), HOUR("h"), MINUTE("m"), SECOND("s"), MILLISECOND("ms");

		String unit;

		DateUnitType(String unit) {
			this.unit = unit;
		}

		@Override
		public String toString() {
			return unit;
		}

		public static DateUnitType getEnumByValue(String value) {
			for (DateUnitType e : DateUnitType.values()) {
				if (e.unit.equals(value)) {
					return e;
				}
			}
			return null;
		}
	}

	/**
	 * 日期格式枚举
	 */
	public static enum DatePattern {
		YYYYMMDDHHMMSS_("yyyy-MM-dd HH:mm:ss"), YYYYMMDDHHMMSS("yyyy/MM/dd HH:mm:ss"), YYYYMMDD("yyyy/MM/dd"),
		YYYYMMDDHHMMSSSS("yyyyMMddHHmmssSS"), UTC("yyyyMMdd.HHmmss"), UTC_NO_DOT("yyyyMMddHHmmss"),
		LOCAL("yyyyMMdd HHmmss");

		String pattern;

		DatePattern(String pattern) {
			this.pattern = pattern;
		}

		@Override
		public String toString() {
			return pattern;
		}
	}

	/**
	 * 消息类别枚举
	 */
	public enum MessageType {
		// 消息1(报设备主要状态)、消息2(报船舶登船人员)、消息3(报设备次要状态)、消息4(发送短信)、消息5(接收紧急报警)、 消息80(中心配置监管仪)、
		// 消息84(接收短信)、消息83(中心读取监管仪次要状态)、消息85(中心升级监管仪软件)、消息86(中心配置身份证读卡器ID)
		PRIMARY_STATUS_MSG("$01"), PUNCH_MSG("$02"), SECONDARY_STATUS_MSG("$03"), DEVICE_CHANNEL_RECEIVE_MSG("$04"), DEVICE_EMERGENCY_ALARM("$05"), 
		CONFIG_DEVICE_REQUEST("$80"), SECONDARY_STATUS_MSG_REQUEST("$83"), DEVICE_CHANNEL_SEND_MSG("$84"), BOOTLOADER_MSG_REQUEST("$85"), 
		CONFIG_ID_CARD_READER_REQUEST("$86"), CHANGE_DEVICE_IP_REQUEST("$87"), CHANGE_DEVICE_BD_ADDRESS("$88"), BOOTLOADER_WRITEFLASH_MSG("$90"), 
		BOOTLOADER_CHECKSUM_MSG("$91"), SERVER_CHANNEL_RECEIVE_MSG("$AA"), BD_SERVER_IP_HOST("$AB"), DEVICE_EMERGENCY_ALARM_VARIA("$AC"),
		DEVICE_MOBILE_SEND("$AM"), AUTOMATIC_DETECTION("$0A");

		private final String value;

		@Override
		public String toString() {
			return value;
		}

		MessageType(String value) {
			this.value = value;
		}

		public static MessageType getEnumByValue(String value) {
			for (MessageType e : MessageType.values()) {
				if (e.value.equals(value)) {
					return e;
				}
			}
			return null;
		}
	}
	
	public static enum CacheType {
		NETTY_APP_CTX;
	}

	/**
	 * 经度枚举
	 */
	public enum LongitudeHemisphere {
		// E:东经 、W:西经
		EAST("E"), WEST("W");

		private final String value;

		@Override
		public String toString() {
			return value;
		}

		LongitudeHemisphere(String value) {
			this.value = value;
		}

		public static LongitudeHemisphere getEnumByValue(String value) {
			for (LongitudeHemisphere e : LongitudeHemisphere.values()) {
				if (e.value.equals(value)) {
					return e;
				}
			}
			return null;
		}
	}

	/**
	 * 纬度半球枚举
	 */
	public enum LatitudeHemisphere {
		// N:北纬 、S:南纬
		NORTH("N"), SOUTH("S");

		private final String value;

		@Override
		public String toString() {
			return value;
		}

		LatitudeHemisphere(String value) {
			this.value = value;
		}

		public static LatitudeHemisphere getEnumByValue(String value) {
			for (LatitudeHemisphere e : LatitudeHemisphere.values()) {
				if (e.value.equals(value)) {
					return e;
				}
			}
			return null;
		}
	}

	/**
	 * HEX记录类型
	 */
	public enum HexRecordType {
		// 00(数据记录) 01(文件结束记录) 02(扩展段地址记录) 03(开始段地址记录) 04(扩展线性地址记录)
		// 05(开始线性地址记录)
		DATA("00"), EOF("01"), DAY("02"), HOUR("03"), EXT_LIN("04"), START_LIN("05");

		String type;

		HexRecordType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}

		public static HexRecordType getEnumByValue(String value) {
			for (HexRecordType e : HexRecordType.values()) {
				if (e.type.equals(value)) {
					return e;
				}
			}
			return null;
		}
	}
	
}
