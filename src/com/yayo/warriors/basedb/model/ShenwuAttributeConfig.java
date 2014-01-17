package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 神武属性配置表
 *  
 * @author Hyint
 */
@Resource
public class ShenwuAttributeConfig {

	/** 神武属性配置表 */
	@Id
	private int id;
	
	/** 神武ID */
	@Index(name=IndexName.EQUIP_SHENWUID_ATTRIBUTE, order=0)
	private int shenwuId;
	
	/** 装备类型 */
	@Index(name=IndexName.EQUIP_SHENWUID_ATTRIBUTE, order=1)
	private int equipType;
	
	/** 装备需求的职业 */
	@Index(name=IndexName.EQUIP_SHENWUID_ATTRIBUTE, order=2)
	private int job;
	
	/** 属性类型 */
	private int attribute;
	
	/** 属性值 */
	private int attrValue;
	
	/** 最大属性值 */
	private int maxAttrValue;
	
	/** 最大属性值 */
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

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public int getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(int attrValue) {
		this.attrValue = attrValue;
	}

	public int getMaxAttrValue() {
		return maxAttrValue;
	}

	public void setMaxAttrValue(int maxAttrValue) {
		this.maxAttrValue = maxAttrValue;
	}

	public int getMaxTempoValue() {
		return maxTempoValue;
	}

	public void setMaxTempoValue(int maxTempoValue) {
		this.maxTempoValue = maxTempoValue;
	}

	public int getEquipType() {
		return equipType;
	}

	public void setEquipType(int equipType) {
		this.equipType = equipType;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	@Override
	public String toString() {
		return "ShenwuAttributeConfig [id=" + id + ", shenwuId=" + shenwuId + ", equipType=" + equipType 
				+ ", job=" + job + ", attribute=" + attribute + ", attrValue=" + attrValue + ", maxAttrValue=" 
				+ maxAttrValue + ", maxTempoValue=" + maxTempoValue	+ "]";
	}

	
}
