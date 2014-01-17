package com.yayo.warriors.module.market.model;

/**
 * 售卖内容
 * @author liuyuhua
 *
 */
public class Sell {
	
	/** 类型*/
	private int type;
	
	/** 原型ID*/
	private int baseId;
	
	/** 商品名称 */
	private String goodsName;
	
	/**
	 * 构造函数
	 * @param type      类型
	 * @param baseId    原型ID
	 * @return
	 */
	public static Sell valueOf(int type, int baseId, String name){
		Sell sell = new Sell();
		sell.type   = type;
		sell.baseId = baseId;
		sell.goodsName = name;
		return sell;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + baseId;
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sell other = (Sell) obj;
		if (baseId != other.baseId)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
