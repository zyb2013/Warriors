package com.yayo.warriors.module.lottery.domain;

/**
 * 	抽奖物品
 */
public class  GainGoodsInfo{

	/** 基础物品id */
	private int baseId ;
	/** 数量 */
	private int gainNum ;

	public GainGoodsInfo(int baseId , int gainNum){
		this.baseId = baseId ;
		this.gainNum = gainNum ;
	}
	
	public GainGoodsInfo(LotteryGoodsInfo goodsInfo) {
		this.baseId = goodsInfo.getBaseId();
		this.gainNum = goodsInfo.getNum();
	}

	public int getBaseId() {
		return this.baseId;
	}

	public int getGainNum() {
		return this.gainNum;
	}
}