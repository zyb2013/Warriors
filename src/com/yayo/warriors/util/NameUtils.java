package com.yayo.warriors.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

/**
 * 名字工具类
 * 
 * @author Hyint
 */
public class NameUtils {

	/** 角色名字的最小长度 */
	public static final int MIN_PLAYER_NAME_LENTH = 1;
	/** 角色的名字最大长度 */
	public static final int MAX_PLAYER_NAME_LENTH = 10;

	/**
	 * 验证角色名字
	 * @param  playerName		角色名
	 * @return {@link Boolean}	true-可以使用, false-不可以使用
	 */
	public static boolean validPlayerName(String playerName) {
		if(validStrLength(playerName, MIN_PLAYER_NAME_LENTH, MAX_PLAYER_NAME_LENTH)) {
			return validNamePattern(playerName);
		}
		return false;
	}

	/** 限制的参数*/
	private static final String[] LIMIT_CHAR = { "%", ",", "*", "^", "#", "$", "&", ":", "_", "[", "]", "|" };
	/**
	 * 验证工会名字长度
	 * 
	 * @param  allianceName		工会名
	 * @return {@link Boolean}	true-可以使用, false-不可以使用
	 */
	public static boolean validAllianceName(String allianceName) {
		if(validStrLength(allianceName, MIN_PLAYER_NAME_LENTH, MAX_PLAYER_NAME_LENTH)) {
			return validNamePattern(allianceName);
		};
		return false;
	}

	/**
	 * 验证名字的格式
	 * 
	 * @param  name				需要验证的名字
	 * @return {@link Boolean}	true-可以使用, false-不可以使用
	 */
	private static boolean validNamePattern(String name) {
		if(StringUtils.isBlank(name)) {
			return false;
		}
		
		for (String element : LIMIT_CHAR) {
			if(name.contains(element)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 校验字符串的长度
	 * 
	 * @param  element			字符串元素
	 * @param  minLength		最小长度
	 * @param  maxLength		最大长度
	 * @return {@link Boolean}	true-合法的, false-不合法的
	 */
	private static boolean validStrLength(String element, int minLength, int maxLength) {
		element = StringUtils.defaultIfBlank(element, "");
		try {
			return validLenth(toEncode(element, "GB18030"), minLength, maxLength);
		} catch (UnsupportedEncodingException e) {
		}
		try {
			return validLenth(toEncode(element, "GB2312"), minLength, maxLength);
		} catch (UnsupportedEncodingException e) {
		}
		try {
			return validLenth(toEncode(element, "GBK"), minLength, maxLength);
		} catch (UnsupportedEncodingException e) {
		}
		return validLenth(toDefaultEncoding(element), minLength, maxLength);
	}

	/**
	 * 验证字符串长度是否在区域范围内
	 * 
	 * @param length
	 * @param min
	 * @param max
	 * @return
	 */
	private static boolean validLenth(int length, int min, int max) {
		return length >= min && length <= max;
	}

	/**
	 * 构建指定编码格式
	 * 
	 * @param  element			字符串
	 * @param  encoding			编码格式
	 * @return {@link Integer}	字符串长度
	 * @throws UnsupportedEncodingException
	 */
	private static int toEncode(String element, String encoding) throws UnsupportedEncodingException {
		return element.getBytes(encoding).length;
	}

	/**
	 * 构建指定编码格式
	 * 
	 * @param  element			字符串
	 * @return {@link Integer}	字符串长度
	 * @throws UnsupportedEncodingException
	 */
	private static int toDefaultEncoding(String element) {
		return element.getBytes().length;
	}

}
