package com.yayo.warriors.basedb.model;

import com.yayo.warriors.type.GoodsType;

/**
 * 副本奖励物品
 * (小工具)
 * @author liuyuhua
 */
public class DungeonProps {
	
	/** 道具ID*/
	private int propsId;
	
	/** 物品类型  参考 {@link GoodsType}*/
	private int goodsType;
	
	/** 数量*/
	private int number;
	
	/**
	 * 构造方法
	 * @param propsId    道具ID
	 * @param goodsType  物品类型
	 * @param number     数量
	 * @return {@link DungeonProps}
	 */
	public static DungeonProps valueOf(int propsId,int goodsType,int number){
		DungeonProps dungeonProps = new DungeonProps();
		dungeonProps.propsId = propsId;
		dungeonProps.goodsType = goodsType;
		dungeonProps.number = number;
		return dungeonProps;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "DungeonProps [propsId=" + propsId + ", goodsType=" + goodsType
				+ ", number=" + number + "]";
	}

}
