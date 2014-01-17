package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 神武基础表
 * 
 * @author Hyint
 */
@Resource
public class ShenwuConfig {
	
	/** 自增的ID */
	@Id
	private int id;
	
	/** 神武属性表ID */
	@Index(name=IndexName.EQUIP_SHENWUID_CONFIG, order=0)
	private int shenwuId;
	
	/** 装备的类型 */
	@Index(name=IndexName.EQUIP_SHENWUID_CONFIG, order=1)
	private int equipType;
	
	/** 装备的等级 */
	private int level;
	
	/** 装备的突破道具 */
	private int tempoItemId;

	/** 装备的提升属性道具 */
	private int attributeItemId;

	/** 装备提升属性道具的消耗数量 */
	private int attributeCount;
	
	/** 装备突破道具的消耗数量 */
	private int tempoItemCount;
	
	/** 单次突破增加值 */
	private int addTempoValue;
	
	/** 最大突破增加值 */
	private int maxTempoValue;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getShenwuId() {
		return shenwuId;
	}

	public void setShenwuId(int shenwuId) {
		this.shenwuId = shenwuId;
	}

	public int getEquipType() {
		return equipType;
	}

	public void setEquipType(int equipType) {
		this.equipType = equipType;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getTempoItemId() {
		return tempoItemId;
	}

	public void setTempoItemId(int tempoItemId) {
		this.tempoItemId = tempoItemId;
	}

	public int getAttributeItemId() {
		return attributeItemId;
	}

	public void setAttributeItemId(int attributeItemId) {
		this.attributeItemId = attributeItemId;
	}

	public int getAttributeCount() {
		return attributeCount;
	}

	public void setAttributeCount(int attributeCount) {
		this.attributeCount = attributeCount;
	}

	public int getTempoItemCount() {
		return tempoItemCount;
	}

	public void setTempoItemCount(int tempoItemCount) {
		this.tempoItemCount = tempoItemCount;
	}

	public int getAddTempoValue() {
		return addTempoValue;
	}

	public void setAddTempoValue(int addTempoValue) {
		this.addTempoValue = addTempoValue;
	}

	public int getMaxTempoValue() {
		return maxTempoValue;
	}

	public void setMaxTempoValue(int maxTempoValue) {
		this.maxTempoValue = maxTempoValue;
	}

	@Override
	public String toString() {
		return "ShenwuConfig [id=" + id + ", shenwuId=" + shenwuId + ", equipType=" + equipType
				+ ", level=" + level + ", tempoItemId=" + tempoItemId + ", attributeItemId="
				+ attributeItemId + ", attributeCount=" + attributeCount + ", tempoItemCount="
				+ tempoItemCount + ", addTempoValue=" + addTempoValue + ", maxTempoValue="
				+ maxTempoValue + "]";
	}
}
