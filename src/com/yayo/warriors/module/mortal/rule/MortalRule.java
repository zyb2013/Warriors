package com.yayo.warriors.module.mortal.rule;

import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import com.yayo.common.utility.Tools;

/**
 * 肉身规则类
 * 
 * @author huachaoping
 */
public class MortalRule {
	
	/** 用户开启肉身的最低等级 */
	public static final int USER_LEVEL = 38;
	
	/** 肉身最高级 */
	public static final int MAX_LEVEL = 50;
	
	/** 类型限制 */
	public static final int TYPE_LIMIT = 7;
	
	/** 变化的附加属性类型 */
	public static final int[] CHANGE_PARAM = {PHYSICAL_ATTACK, PHYSICAL_DEFENSE, THEURGY_ATTACK, THEURGY_DEFENSE, HP_MAX}; 
	
	
	/** 
	 * 成功机率
	 * @param value
	 * @return
	 */
	public static boolean successRatio(int value, int maxValue) {
		if (value > maxValue || value < 0) {
			return false;
		}
		if (Tools.getRandomInteger(maxValue) < value) {
			return true;
		}
		return false;
	}

	/**
	 * 自动购买所需元宝
	 * @param autoBuyCount
	 * @return
	 */
	public static int calAutoBuyCount(int autoBuyCount, int price) {
		return autoBuyCount * price;
	}
	
	/**
	 * 肉身属性加成等级
	 * @param minLevel      肉身最低等级
	 * @return
	 */
	public static int getMortalAddedLevel(int minLevel) {
		if (minLevel == 5) {                     // 特殊处理, 5级发公告
			return minLevel;
		}
		return (minLevel / 10) * 10;
	}
}
