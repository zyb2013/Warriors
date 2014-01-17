package com.yayo.warriors.module.task.model;

import java.util.List;

import com.yayo.warriors.module.logger.model.LoggerGoods;


public class QuestObject <T> {
	
	private T rewardInfos;
	
	private LoggerGoods[] goodsInfos = null;

	public T getRewardInfos() {
		return rewardInfos;
	}

	public LoggerGoods[] getGoodsInfos() {
		return goodsInfos;
	}

	private QuestObject() {
	}

	public static <T> QuestObject<T> valueOf(T value) {
		QuestObject<T> questObject = new QuestObject<T> ();
		questObject.rewardInfos = value;
		questObject.goodsInfos = new LoggerGoods[0];
		return questObject;
	}

	public static <T> QuestObject<T> valueOf(T rewardInfos, List<LoggerGoods> goodsInfos) {
		QuestObject<T> questObject = new QuestObject<T> ();
		questObject.rewardInfos = rewardInfos;
		questObject.goodsInfos = goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]);
		return questObject;
	}
}
