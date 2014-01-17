package com.yayo.warriors.basedb.model;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 	抽奖基础数据
 */
@Resource
public class LotteryConfig {

	/** 抽奖id */
	@Id
	private int id;
	/** 阶段 */
	private int type;
	/** 等级 */
	private int level;
	/** 抽奖次数 */
	private int times;
	/** 消耗的道具 */
	private int propsId;
	/** 消耗的道具数量 */
	private int num;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getTimes() {
		return times;
	}
	public void setTimes(int times) {
		this.times = times;
	}
	
	/** 是否够等级 */
	public boolean isLevelToLottery(int level) {
		return this.level <= level;
	}
	
	/**
	 * 随机一个奖励
	 * @return
	 */
	public LotteryGridRateConfig rollGird(List<LotteryGridRateConfig> lotteryGridRateConfigs) {
		final int maxRate = lotteryGridRateConfigs.get(0).getFullRate();
		int ranRate = Tools.getRandomInteger(maxRate);
		for(LotteryGridRateConfig lotteryGridRateConfig : lotteryGridRateConfigs){
			ranRate -= lotteryGridRateConfig.getRate() ;
			if(ranRate <= 0){
				return lotteryGridRateConfig ;
			}
		}
		return null;
	}
	public int getPropsId() {
		return propsId;
	}
	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	
}
