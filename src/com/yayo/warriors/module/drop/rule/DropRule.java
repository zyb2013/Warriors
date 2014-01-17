package com.yayo.warriors.module.drop.rule;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.DropConfig;
import com.yayo.warriors.basedb.model.DropConfig.DropInfo;
import com.yayo.warriors.module.drop.model.Drop;
import com.yayo.warriors.module.drop.model.DropResult;
import com.yayo.warriors.type.GoodsType;

/**
 * 奖励规则对象
 * 
 * @author Hyint
 */
public class DropRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(DropRule.class);
	
	/**
	 * 处理随机掉落信息
	 * 
	 * @param  configList			基础掉落奖励列表
	 * @param  totalRate			总概率
	 * @return {@link DropConfig}	奖励配置对象
	 */
	public static DropConfig doRandomReward(List<DropConfig> configList, int totalRate) {
		if(configList == null || configList.isEmpty() || totalRate <= 0){
			return null;
		}
		
		int currCount = 0;
		Collections.shuffle(configList);
		int random = Tools.getRandomInteger(totalRate) + 1;
		for(DropConfig config : configList){
			currCount = currCount + config.getRate();
			if(random <= currCount){
				return config;
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("掉落编号[{}]最大概率[{}], 概率随机数[{}], 没有获得奖励", 	new Object[]{ configList.get(0).getRewardNo(), totalRate, random });
		}
		return null;
	}

	/**
	 * 掉落奖励
	 * 
	 * @param  dropConfig			基础奖励对象
	 * @return {@link DropResult}	奖励结果对象
	 */
	public static DropResult createRewardResult(DropConfig dropConfig) {
		if (dropConfig == null) {	// 没有奖励, 抽下一轮
			return null;
		}
		
		int rewardId = dropConfig.getId();
		int amount = dropConfig.getAmount(); 
		int dropType = dropConfig.getType();
		if (dropType == GoodsType.NONE || amount <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("基础奖励对象ID: [{}] 为空奖励", rewardId);
			}
			return null;
		}

		List<DropInfo> rewardInfoList = dropConfig.getDropInfoList();
		if(rewardInfoList == null || rewardInfoList.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("基础奖励对象ID: [{}] 为空奖励", rewardId);
			}
			return null;
		}
		
		switch (dropType) {
			case GoodsType.EQUIP: 	return createDropResult(dropConfig); 			//奖励的是装备
			case GoodsType.PROPS: 	return createDropResult(dropConfig); 			//奖励的是道具
			case GoodsType.SILVER: 	return createMoneyDropResult(dropConfig); 		//奖励的是游戏币
			case GoodsType.GOLDEN: 	return createMoneyDropResult(dropConfig); 		//奖励的是货币
			default:			  	return null;
		}
	}
	
	/**
	 * 创建装备奖励返回值
	 * 
	 * @param dropConfig		掉落配置信息
	 */
	private static DropResult createMoneyDropResult(DropConfig dropConfig) {
		List<DropInfo> rewardInfos = dropConfig.getDropInfoList();
		if(rewardInfos == null || rewardInfos.isEmpty()) {
			return null;
		}
		
		int type = dropConfig.getType();										//掉落类型
		int rewardId = dropConfig.getId();										//奖励的ID
		int amount = dropConfig.getAmount();									//奖励的装备数量
		boolean noticeId = dropConfig.isNotice();								//公告ID
		long dieoutTime = dropConfig.getDieoutTimeMillis();						//掉落消失时间.
		DropInfo info1 = rewardInfos.size() > 0 ? rewardInfos.get(0) : null;
		DropInfo info2 = rewardInfos.size() > 1 ? rewardInfos.get(1) : null;
		int money1 = info1 != null ? info1.getInfo() : 0;
		int money2 = info2 != null ? info2.getInfo() : 0;
		DropResult dropResult = DropResult.valueOf(rewardId, noticeId, amount);	//掉落配置信息
		int dropValue = Math.max(0, Math.min(money1, money2)) + Tools.getRandomInteger(Math.abs(money1 - money2) + 1);
		if(dropValue > 0) {
			for (int count = 0; count < amount; count++) {
				dropResult.addDrops(Drop.valueOf(type, -1, dropValue, dieoutTime));
			}
		}
		return dropResult;
	}
	
	/**
	 * 创建装备奖励返回值
	 * 
	 * @param dropConfig		掉落配置信息
	 */
	private static DropResult createDropResult(DropConfig dropConfig) {
		if(dropConfig == null) {
			return null;
		}
		
		List<DropInfo> rewardInfos = dropConfig.getDropInfoList();
		if(rewardInfos == null || rewardInfos.isEmpty()) {
			return null;
		}
		
		int type = dropConfig.getType();												//掉落类型
		int rewardId = dropConfig.getId();												//奖励的ID
		int amount = dropConfig.getAmount();											//奖励的装备数量
		boolean noticeId = dropConfig.isNotice();										//公告ID
		long dieoutTime = dropConfig.getDieoutTimeMillis();								//掉落消失时间.
		DropResult dropResult = DropResult.valueOf(rewardId, noticeId, amount);			//掉落配置信息
		for (int count = 0; count < amount; count++) {
			int idListSize = rewardInfos.size();
			DropInfo dropInfo = rewardInfos.get(Tools.getRandomInteger(idListSize));
			dropResult.addDrops(Drop.valueOf(type, dropInfo.getInfo(), dieoutTime, dropInfo.isBinding()));
		}
		return dropResult;
	}
}
