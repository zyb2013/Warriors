package com.yayo.warriors.module.horse.rule;

import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.module.horse.entity.Horse;

/**
 * 坐骑规则
 * @author liuyuhua
 */
public class HorseRule {
	
	/** 初始化坐骑等级*/
	public static final int INIT_HORSE_LEVEL = 1;

	/** 坐骑最高等级*/
	public static final int MAX_HORSE_LEVEL = 100;
	
	/** 坐骑最小骑乘等级*/
	public static final int MIN_WINUP_LEVEL = 11;
	
	/** 获得坐骑玩家所需要的最小等级*/
	public static final int GET_HORSE_PLAYER_LEVEL = 5;
	
	/** 普通幻化所需要使用的物品个数*/
	public static final int SIMPLE_FANCY_PROPS_COUNT = 1;
	
	/** 元宝幻化价格(单价)*/
	public static final int GENERAL_GOLD_FANCY_PRICE = 10;
	
	/** 超级幻化次数*/
	public static final int SUPER_GOLD_FANCY_COUNT = 10;
	
	/** 元宝幻化,小暴率的经验乘积*/
	public static final int MIN_RATE_VALUE = 10;
	
	/** 顶级元宝幻化,坐骑等级最小等级限制*/
	public static final int SUPER_FANCY_HORSE_LEVEL_LIMIT = 60;
	
	/** 自定义元宝幻化最大次数*/
	public static final int MAX_DEFINE_FANCY_COUNT = 99;
	
	/** 坐骑经验丹基础ID*/
	public static final int HORSE_EXP_PROPS = 70001;
	
	
	/**
	 * 创建坐骑
	 * @param playerId   玩家的ID
	 * @param config     坐骑配置
	 * @return {@link Horse} 坐骑对象
	 */
	public static Horse createHorse(long playerId,HorseConfig config){
		int model = config.getModel();
		Horse horse = Horse.valueOf(playerId, model);
		horse.addHorseMount(config.getModel());
		return horse;
	}

	public static int getSuperFancyGoldPrice() {
		return GENERAL_GOLD_FANCY_PRICE * SUPER_GOLD_FANCY_COUNT;
	}
}
