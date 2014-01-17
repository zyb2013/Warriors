package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

@Resource
public class MallSpecialConfig {
	
	/** 商城限购ID */
	@Id
	private int id;
	
	/** 出售的道具ID */
	private int propsId;
	
	/** 限购活动ID */
	@Index(name=IndexName.MALL_ACTIVE_PROPS, order=0)
	private int activeId;
	
	/** 购买等级 */
	private int buyLevel;
	
	/** 道具类型 */
	private int goodsType;
	
	/** 购买上限 */
	private int buyCountLimit;
	
	/** 限购商品价格 */
	private int specialPrice;
	
	/** 商品出售数量*/
	private int totalCount;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	public int getBuyLevel() {
		return buyLevel;
	}

	public void setBuyLevel(int buyLevel) {
		this.buyLevel = buyLevel;
	}

	public int getBuyCountLimit() {
		return buyCountLimit;
	}

	public void setBuyCountLimit(int buyCountLimit) {
		this.buyCountLimit = buyCountLimit;
	}

	public int getSpecialPrice() {
		return specialPrice;
	}

	public void setSpecialPrice(int specialPrice) {
		this.specialPrice = specialPrice;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	public int getActiveId() {
		return activeId;
	}

	public void setActiveId(int activeId) {
		this.activeId = activeId;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}
	
//	public int getCalSpecailPrice(int golden) {
//		return (int) Math.ceil(golden * specialPrice * 0.01);
//	}

}
