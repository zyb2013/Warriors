package com.yayo.warriors.module.mail.model;

/**
 * 条件类型
 * 
 * @author huachaoping
 */
public interface ConditionType {
	
	/** 大于 > */
	String ABOVE = "0";
	
	/** 等于 == */
	String EQUAL = "1";
	
	/** 小于 < */
	String LESS = "2";
	
	/** 大于等于 >= */
	String ABOVE_OR_EQUAL = "3";
	
	/** 小于等于 <= */
	String LESS_OR_EQUAL = "4";
}
