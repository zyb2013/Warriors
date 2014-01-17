package com.yayo.warriors.module.props.type;

/**
 * 时效性类型
 * 
 * <pre>
 * 0-即刻生效, 即刻起+时长(分钟)
 * 1-即刻生效, 固定结束时间(内容: 年月日时分秒. 例如: 2011年12月1日 12:00:00-> 20111201120000)
 * 2-使用时生效.即刻起+时效时长(分钟) 	
 * </pre>
 * @author Hyint
 */
public enum ExpirateType {
	
	/** 0-即刻生效, 即刻起+时长(分钟) */
	NOW_EXPIRATE,
	
	/** 1-即刻生效, 固定结束时间(内容: 年月日时分秒) */
	FIXED_EXPIRATE,
	
	/** 2-使用时生效.即刻起+时效时长(分钟) */
	USE_TRIGGER_EXPIRATE,
}
