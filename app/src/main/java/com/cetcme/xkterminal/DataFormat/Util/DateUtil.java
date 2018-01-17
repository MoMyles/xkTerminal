package com.cetcme.xkterminal.DataFormat.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static final long MILLIS_PER_SECOND = 1000;
	public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
	public static final long MILLIS_PER_YEAR = 365 * MILLIS_PER_DAY;
	public static final long MILLIS_PER_MONTH = 30 * MILLIS_PER_DAY;

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

	protected DateUtil() {
	}

	private static final DateUtil dateUtil = new DateUtil();

	public static DateUtil getInstance() {
		return dateUtil;
	}

	public static Date parseStringToDate(String arg, DatePattern pattern) {
		Date date = null;
		try {
			date = new SimpleDateFormat(pattern.toString()).parse(arg);
		} catch (ParseException e) {
			return null;
		}
		return date;
	}

	public static String parseDateToString(Date arg, DatePattern pattern) {
		return arg == null ? "" : new SimpleDateFormat(pattern.toString()).format(arg);
	}

	public static String parseDateToString(Date arg) {
		return parseDateToString(arg, DatePattern.YYYYMMDDHHMMSS);
	}

	public static Date parseStringToDate(String arg) {
		return parseStringToDate(arg, DatePattern.YYYYMMDDHHMMSS);
	}

	public static Double convertToMiliSecond(final DateUnitType unit, final String strValue) {
		Double doubleValue = Double.valueOf(strValue);
		long ratio = 1;
		switch (unit) {
		case YEAR:
			ratio = MILLIS_PER_YEAR;
			break;
		case MONTH:
			ratio = MILLIS_PER_MONTH;
			break;
		case DAY:
			ratio = MILLIS_PER_DAY;
			break;
		case HOUR:
			ratio = MILLIS_PER_HOUR;
			break;
		case MINUTE:
			ratio = MILLIS_PER_MINUTE;
			break;
		case SECOND:
			ratio = MILLIS_PER_SECOND;
			break;
		default:
			break;
		}
		return doubleValue * ratio;
	}
}
