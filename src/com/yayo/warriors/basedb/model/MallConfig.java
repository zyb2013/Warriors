
package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 基础商城对象
 * 
 * @author Hyint
 */
@Resource
public class MallConfig {

	/** 商城物品视图ID */
	@Id
	private int id;
	
	/** 商城类型 */
	private int mallType;
	
	/** 出售的道具ID */
	@Index(name = IndexName.MALL_PROPSID)
	private int propsId;
	
	/** 物品类型 */
	private int goodsType;
	
	
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

	public int getMallType() {
		return mallType;
	}

	public void setMallType(int mallType) {
		this.mallType = mallType;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}
	
}
