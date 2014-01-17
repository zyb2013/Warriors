package com.yayo.warriors.module.lottery.vo;

import java.io.Serializable;

/**
 * 抽奖物品VO对象
 */
public class LotteryRewardVo implements Serializable{

	private static final long serialVersionUID = 2757374699492685127L;
	/** 物品基础id */
	private int propId ;
	/** 数量 */
	private int num ;
	/** 是否绑定 */
	private boolean banding;
	/** 道具类型  0 道具 1装备*/
	private int propsType ;
	
	public LotteryRewardVo(int propId , int type , int num, boolean banding) {
		this.propId = propId ;
		this.propsType = type ;
		this.num = num ;
		this.banding = banding;
	}

	public int getPropId() {
		return propId;
	}

	public int getNum() {
		return num;
	}

	/**
	 * 修改数目
	 * @param gainNum
	 */
	public void alterNum(int gainNum) {
		this.num += gainNum ;
	}

	public boolean isBanding() {
		return banding;
	}

	public void setBanding(boolean banding) {
		this.banding = banding;
	}

	public boolean equipType() {
		return this.propsType != 0;
	}
	public boolean propsType() {
		return this.propsType == 0;
	}

	public int getPropsType() {
		return propsType;
	}

	public void setPropsType(int propsType) {
		this.propsType = propsType;
	}

	public void setPropId(int propId) {
		this.propId = propId;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
}
