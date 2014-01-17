package com.yayo.warriors.module.onhook.rule;

/**
 * 挂机规则类
 * 
 * @author huachaoping
 */
public class TrainRule {
	
	/** 邀请次数限制 */
	public static final int INVITE_LIMIT = 5;
	
	/** 打坐等级限制, 11级开启 */
	public static final int OPEN_LEVEL = 11;
	
	/** 打坐冷却时间(毫秒) */
	public static final long COOL_TIME = 1000;
	
	/** 打坐状态领取奖励时间(毫秒)*/
	public static final long RECEIVE_TIME = 4900;
	
	/** 真气最大值 */
	public static final int MAX_VALUE = 10000;
	
	/** 闭关时间限制(12小时,毫秒) */
	public static final int TIME_LIMIT = 43200 * 1000;
	
	/** 5分钟的毫秒数, 闭关每5分钟结算一次 */
	public static final int FIVE_MIN = 300 * 1000;
	
	/**
	 * 计算时间间隔
	 * 
	 * @param curSecond          当前时间
	 * @param startSecond        开始时间
	 * @return {@link Integer}   时间间隔
	 */
	public static long lagTime(long curSecond, long startSecond) {
		return curSecond - startSecond;
	}
	
	
	/**
	 * 计算闭关时间
	 * 
	 * @param lagTime            时间间隔
	 * @return {@link Long}
	 */
	public static long trainTime(long lagTime) {
		lagTime = lagTime >= TIME_LIMIT ? TIME_LIMIT : lagTime;
		return (lagTime / FIVE_MIN) * FIVE_MIN;
	}
	
}
