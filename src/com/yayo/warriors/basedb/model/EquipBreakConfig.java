package com.yayo.warriors.basedb.model;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;

/**
 * 装备分解信息表对象
 * 
 * @author Hyint
 */
@Resource
public class EquipBreakConfig {

	/** 自增ID */
	@Id
	private int id;
	
	/** 装备的星级 */
	private int level;
	 
	/** 价格系数 */
	private double rate;
		
	/** 装备的品质 */
	private int quality;
	
	/** 分解可以获得的道具1 */
	private int itemId1;
	
	/** 分解可以获得的道具2 */
	private int itemId2;
	
	/** 分解可以得到的道具1的数量 */
	private String itemCount1;

	/** 分解可以得到的道具2的数量 */
	private String itemCount2;
	
	/** 合成可以得到的道具1 */
	@JsonIgnore
	private PropsConfig propsConfig1;
	/** 合成可以得到的道具1 */
	@JsonIgnore
	private PropsConfig propsConfig2;
	/** 可以获得的道具数量1 */
	@JsonIgnore
	private int[] count1Array = null;
	/** 可以获得的道具数量2 */
	@JsonIgnore
	private int[] count2Array = null;
	
	private int[] getCount1Array() {
		if(this.count1Array != null) {
			return this.count1Array;
		}

		synchronized (this) {
			if(this.count1Array != null) {
				return this.count1Array;
			}
			this.count1Array = new int[2];
			if(StringUtils.isBlank(this.itemCount1) || itemId1 <= 0) {
				return this.count1Array;
			}

			String[] array = itemCount1.split(Splitable.ATTRIBUTE_SPLIT);
			this.count1Array[0] = Integer.valueOf(array[0]);
			this.count1Array[1] = Integer.valueOf(array[1]);
		}
		return this.count1Array;
	}

	private int[] getCount2Array() {
		if(this.count2Array != null) {
			return this.count2Array;
		}
		
		synchronized (this) {
			if(this.count2Array != null) {
				return this.count2Array;
			}
			this.count2Array = new int[2];
			if(StringUtils.isBlank(this.itemCount2) || itemId2 <= 0) {
				return this.count2Array;
			}
			
			String[] array = itemCount2.split(Splitable.ATTRIBUTE_SPLIT);
			this.count2Array[0] = Integer.valueOf(array[0]);
			this.count2Array[1] = Integer.valueOf(array[1]);
		}
		return this.count2Array;
	}
	
	/**
	 * 获得随机道具数量1 
	 * 
	 * @return {@link Integer}
	 */
	public int getRandomItemCount1() {
		int[] array = this.getCount1Array();
		return array[0] + Tools.getRandomInteger(array[1] + 1);
	}
	
	/**
	 * 获得随机道具数量1 
	 * 
	 * @return {@link Integer}
	 */
	public int getRandomItemCount2() {
		int[] array = this.getCount2Array();
		return array[0] + Tools.getRandomInteger(array[1] + 1);
	}
	
	public PropsConfig getPropsConfig1() {
		return propsConfig1;
	}

	public void setPropsConfig1(PropsConfig propsConfig1) {
		this.propsConfig1 = propsConfig1;
	}

	public PropsConfig getPropsConfig2() {
		return propsConfig2;
	}

	public void setPropsConfig2(PropsConfig propsConfig2) {
		this.propsConfig2 = propsConfig2;
	}

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

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getItemId1() {
		return itemId1;
	}

	public void setItemId1(int itemId1) {
		this.itemId1 = itemId1;
	}

	public int getItemId2() {
		return itemId2;
	}

	public void setItemId2(int itemId2) {
		this.itemId2 = itemId2;
	}

	public String getItemCount1() {
		return itemCount1;
	}

	public void setItemCount1(String itemCount1) {
		this.itemCount1 = itemCount1;
	}

	public String getItemCount2() {
		return itemCount2;
	}

	public void setItemCount2(String itemCount2) {
		this.itemCount2 = itemCount2;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public int calcCostSilver(int currentSilver) {
		return (int) (this.rate * currentSilver);
	}
	
}
