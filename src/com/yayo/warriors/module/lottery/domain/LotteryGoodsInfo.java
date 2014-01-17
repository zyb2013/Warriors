package com.yayo.warriors.module.lottery.domain;

import com.yayo.common.utility.Splitable;

public class LotteryGoodsInfo {

	/**物品编号*/
	private int baseId ;
	/**获得数量*/
	private int gainNum ;
	/**获得概率*/
	private int rate ;
	
	public LotteryGoodsInfo(String goodsInfo) {
		String [] params = goodsInfo.split(Splitable.ATTRIBUTE_SPLIT);
		baseId = Integer.parseInt(params[0]);
		gainNum = Integer.parseInt(params[1]);
		rate = Integer.parseInt(params[2]);
	}

	/**
	 * 获取物品抽奖概率
	 * @return
	 */
	public int getRate() {
		return this.rate;
	}

	public int getBaseId() {
		return this.baseId;
	}

	public int getNum() {
		return this.gainNum ;
	}

}
