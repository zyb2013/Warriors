package com.yayo.warriors.basedb.model;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.type.IndexName;

@Resource
public class LotteryGridRateConfig {
	/** 日志 */
	private final static Logger logger = LoggerFactory.getLogger(LotteryGridRateConfig.class);
	
	@Id
	private int id;
	/** 抽奖规则id */
	@Index(name = IndexName.LOTTERYGRIDRATE_CONFIGID)
	private int lotteryConfigId;
	/** 格子数 */
	private int gridId;
	/** 概率 */
	private int rate;
	/** 概率满值 */
	private int fullRate;
	/** 物品生成id */
	private int propsRollId;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLotteryConfigId() {
		return lotteryConfigId;
	}
	public void setLotteryConfigId(int lotteryConfigId) {
		this.lotteryConfigId = lotteryConfigId;
	}
	public int getGridId() {
		return gridId;
	}
	public void setGridId(int gridId) {
		this.gridId = gridId;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public int getFullRate() {
		return fullRate;
	}
	public void setFullRate(int fullRate) {
		this.fullRate = fullRate;
	}
	public int getPropsRollId() {
		return propsRollId;
	}
	public void setPropsRollId(int propsRollId) {
		this.propsRollId = propsRollId;
	}
	public boolean myPropsRateConfig(LotteryPropsRateConfig lotteryPropsRateConfig) {
		return lotteryPropsRateConfig.getPropsRollId() == this.propsRollId;
	}
	
	public LotteryPropsRateConfig rollProps(List<LotteryPropsRateConfig> lotteryPropsRateConfigs) {
		final int maxRate = lotteryPropsRateConfigs.get(0).getFullRate();
		int ranRate = Tools.getRandomInteger(maxRate);
		for(LotteryPropsRateConfig config : lotteryPropsRateConfigs){
			ranRate -= config.getRate() ;
			if(ranRate <= 0){
				return config ;
			}
		}
		logger.error("概率没有满值, 物品随机掉落id:[{}]", this.propsRollId);
		return null;
	}
	
}
