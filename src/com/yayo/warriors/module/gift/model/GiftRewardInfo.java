package com.yayo.warriors.module.gift.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.type.GoodsType;

/**
 * 礼包奖励信息
 * 
 * @author huachaoping
 */
public class GiftRewardInfo {

	/** 基础物品ID, 如果是钱则此项为-1*/
	private int baseId = -1;
	
	/** 奖励的类型 @link {@link GoodsType} */
	private int goodsType;
	
	/** 奖励物品的数量 */
	private int count;
	
	/**
	 * 构造方法
	 * 
	 * @param baseId
	 * @param goodsType
	 * @param count
	 * @return GiftRewardInfo
	 */
	public static GiftRewardInfo propsReward(int baseId, int count) {
		GiftRewardInfo info = new GiftRewardInfo();
		info.baseId = baseId;
		info.goodsType = GoodsType.PROPS;
		info.count = count;
		return info;
	}
	
	public static GiftRewardInfo equipReward(int baseId, int count) {
		GiftRewardInfo info = new GiftRewardInfo();
		info.baseId = baseId;
		info.goodsType = GoodsType.EQUIP;
		info.count = count;
		return info;
	}
	
	public static GiftRewardInfo goldenReward(int count) {
		GiftRewardInfo info = new GiftRewardInfo();
		info.goodsType = GoodsType.GOLDEN;
		info.count = count;
		return info;
	}
	
	public static GiftRewardInfo silverReward(int count) {
		GiftRewardInfo info = new GiftRewardInfo();
		info.goodsType = GoodsType.SILVER;
		info.count = count;
		return info;
	}
	
	public static GiftRewardInfo couponReward(int count) {
		GiftRewardInfo info = new GiftRewardInfo();
		info.goodsType = GoodsType.COUPON;
		info.count = count;
		return info;
	}
	
	/**
	 * 获得奖励列表
	 * 
	 * @param propsList
	 * @return {@link List<GiftRewardInfo>}
	 */
	public static List<GiftRewardInfo> propsRewardList(Map<Integer, Integer> propsList) {
		List<GiftRewardInfo> infoList = new ArrayList<GiftRewardInfo>();
		if (propsList != null && !propsList.isEmpty()) {
			for (Map.Entry<Integer, Integer> entry : propsList.entrySet()) {
				infoList.add(propsReward(entry.getKey(), entry.getValue()));
			}
		}
		return infoList;
	}

	
	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	
	@Override
	public String toString() {
		return "GiftRewardInfo [baseId=" + baseId + ", goodsType=" + goodsType + ", count=" + count + "]";
	}
	
}
