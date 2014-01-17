package com.yayo.warriors.module.meridian;

import com.yayo.common.utility.Tools;

/**
 * 经脉规则类
 * 
 * @author huachaoping
 */
public class MeridianRule {
	
	/** 经脉类型限制*/
	public static final int TYPE_LIMIT = 15;
	
	/** 最大冲脉机率数值*/
	public static final int PERCENT = 100;
	
	/** 突破瓶颈所需真气*/
	public static final int BREAK_VALUE = 5000;
	
	/** 突破瓶颈所需道具数量*/
	public static final int COUNT = 8;
	
	/** 冲穴使用加概率道具的最大数量*/
	public static final int LUCKITEM = 20;
	
	/** 可分享经验次数*/
	public static final int TIMES = 24;
	
	/** 经脉开启等级 */
	public static final int OPEN_LEVEL = 25;
	
	
	// 成功机率
	public static boolean successRatio(int value) {
		if (value > PERCENT || value < 0) {
			return false;
		}
		if (Tools.getRandomInteger(PERCENT) < value) {
			return true;
		}
		return false;
	}
}
