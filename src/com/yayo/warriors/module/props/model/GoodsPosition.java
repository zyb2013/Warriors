package com.yayo.warriors.module.props.model;

/**
 * 物品背包对象
 * 
 * @author Hyint
 */
public class GoodsPosition implements Comparable<GoodsPosition> {
	
	/** 物品的ID */
	private long id;

	/** 物品的类型 */
	private int goodsType;

	/** 物品在背包中的陈列顺序 */
	private Integer backpackSort = 0;;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public Integer getBackpackSort() {
		return backpackSort;
	}

	public void setBackpackSort(Integer backpackSort) {
		this.backpackSort = backpackSort;
	}

	/**
	 * 构建背包实体参数
	 * 
	 * @param  goodsId					物品ID
	 * @param  goodsType				物品类型
	 * @param  backpackSort				物品陈列顺序
	 * @return {@link GoodsPosition}	物品位置对象
	 */
	public static GoodsPosition valueOf(long goodsId, int goodsType, int backpackSort) {
		GoodsPosition goodsPosition = new GoodsPosition();
		goodsPosition.id = goodsId;
		goodsPosition.goodsType = goodsType;
		goodsPosition.backpackSort = backpackSort;
		return goodsPosition;
	}
	
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + goodsType;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		GoodsPosition other = (GoodsPosition) obj;
		return goodsType == other.goodsType && id == other.id;
	}

	
	public int compareTo(GoodsPosition o) {
		if(o != null) {
			return this.backpackSort.compareTo(o.backpackSort);
		}
		return -1;
	}

	
	public String toString() {
		return "GoodsPosition [id=" + id + ", goodsType=" + goodsType + ", backpackSort=" + backpackSort + "]";
	}
	
	
}
