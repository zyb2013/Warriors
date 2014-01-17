package com.yayo.warriors.module.logger.model;
import static com.yayo.common.utility.DatePattern.*;
import static com.yayo.common.utility.Splitable.*;

import java.util.Date;

import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.logger.type.Orient;

/**
 * 日志的条目
 * 
 * @author Hyint
 */
public class Term {
	
	/**
	 * 条目名称
	 */
	private String name;
	
	/**
	 * 条目值
	 */
	private Object value;
	
	/**
	 * 构建条目对象
	 * 
	 * @param name
	 * @param value
	 */
	private Term(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * 构建一个日志子项
	 * 
	 * @param name 条目名称
	 * @param value 条目值
	 */
	public static Term valueOf(String name, Object value) {
		return new Term(name, value);
	}

	/**
	 * 将若干个{@link Term}信息变成: 名字1:信息1 的字符串
	 * 
	 * @return {@link String}	返回构造后的字符串
	 */
	@Override
	public String toString() {
		return new StringBuffer().append(name).append(DELIMITER_ARGS).append(value).toString();
	}
	
	/**
	 * 货币收入
	 * 
	 * @return {@link Term}	
	 */
	public static Term moneyIncomeTerm() {
		return new Term(Orient.MONEY_INCOME.getName(), Orient.MONEY_INCOME.getCode());
	}
	
	/**
	 * 货币支出
	 * 
	 * @return {@link Term}	
	 */
	public static Term moneyOutcomeTerm() {
		return new Term(Orient.MONEY_OUTCOME.getName(), Orient.MONEY_OUTCOME.getCode());
	}
	
	/**
	 * 获得时间的日志断
	 * 
	 * @return {@link Term}	日子子类
	 */
	public static Term getTimeTerm() {
		return Term.valueOf("time", DateUtil.date2String(new Date(), PATTERN_YYYYMMDDHHMMSS));
	}
	
}