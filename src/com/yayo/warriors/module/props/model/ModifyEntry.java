package com.yayo.warriors.module.props.model;

import java.io.Serializable;

import com.yayo.warriors.module.props.entity.BackpackEntry;

/**
 * 修改后的背包实体对象
 * 
 * @author Hyint
 */
public class ModifyEntry implements Serializable {
	private static final long serialVersionUID = 5491496527719051769L;
	
	/** 物品ID */
	private long id;

	/** 基础道具ID */
	private int baseId;
	
	/** 道具数量 */
	private int count;
	
	/** 物品下标 */
	private int index;
	
	/** 背包号 */
	private int backpack;

	/** 物品类型 */
	private int goodsType;
	
	/** 修改类型, true-增加, false-移除 */
	private boolean modifyType;

	
	/**
	 * 增加背包实体
	 * 
	 * @param  backpackEntry			背包实体对象
	 * @param  backpack					背包号
	 * @return {@link ModifyEntry}		背包修改实体
	 */
	public static ModifyEntry add(BackpackEntry backpackEntry, int backpack){
		ModifyEntry entry = new ModifyEntry();
		entry.modifyType = true;
		entry.backpack = backpack;
		entry.id = backpackEntry.getId();
		entry.count = backpackEntry.getCount();
		entry.index = backpackEntry.getIndex();
		entry.baseId = backpackEntry.getBaseId();
		entry.goodsType = backpackEntry.getGoodsType();
		return entry;
	}

	
	/**
	 * 移除背包实体
	 * 
	 * @param  backpackEntry			背包实体对象
	 * @param  backpack					背包号
	 * @return {@link ModifyEntry}		背包修改实体
	 */
	public static ModifyEntry remove(BackpackEntry backpackEntry, int backpack){
		ModifyEntry entry = new ModifyEntry();
		entry.modifyType = false;
		entry.backpack = backpack;
		entry.id = backpackEntry.getId();
		entry.count = backpackEntry.getCount();
		entry.index = backpackEntry.getIndex();
		entry.baseId = backpackEntry.getBaseId();
		entry.goodsType = backpackEntry.getGoodsType();
		return entry;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBackpack() {
		return backpack;
	}

	public void setBackpack(int backpack) {
		this.backpack = backpack;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public boolean isModifyType() {
		return modifyType;
	}

	public void setModifyType(boolean modifyType) {
		this.modifyType = modifyType;
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


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}
}
