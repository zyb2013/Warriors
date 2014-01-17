package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 洗练属性配置对象
 * 
 * @author Hyint
 */
@Resource
public class WashAttributeConfig {
	
	/** 属性自增ID */
	@Id
	private int id;
	
	/** 装备的品质 */
	private int quality;
	
	/** 装备ID */
	private int level;
	
	/** 属性类型 */
	private int attribute;
	
	/** 随机出的最小值  */
	private int minValue;

	/** 随机出的最大值  */
	private int maxValue;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	@Override
	public String toString() {
		return "WashAttributeConfig [id=" + id + ", quality=" + quality + ", level=" + level
				+ ", attribute=" + attribute + ", minValue=" + minValue + ", maxValue=" + maxValue + "]";
	}
	
	/**
	 * 获得随机附加属性
	 * 
	 * @return {@link Integer}		随机附加属性值
	 */
	public int getRandomAddValue() {
		return this.minValue + Tools.getRandomInteger(this.maxValue - this.minValue + 1);
	}
	
}
