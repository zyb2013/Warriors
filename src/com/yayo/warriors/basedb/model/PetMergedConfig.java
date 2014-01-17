package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 家将契合度基础配置
 * @author liuyuhua
 */
@Resource
public class PetMergedConfig {
	
	/** 契合度等级*/
	@Id
	private int id;
	
	/** 品质最小下线*/
	private int quality;
	
	/** 契合值*/
	private float mergedValue;

	/** 升级道具*/
	private int propsId;
	
	/** 升级消耗道具数量*/
	private int number;
	
	/** 基础成功率*/
	private int basePercent;
	
	/** 满值区间*/
	private int fullValue;
	
	/** 单词增加祝福值*/
	private int singleBless;
	
	/** 单词增加几率*/
	private int singlePercent;
	
	/** 祝福值上限*/
	private int blessLimit;
	
	/** 祝福值提升底数*/
	private int baseBless;
	
	//Getter and Setter....

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public float getMergedValue() {
		return mergedValue;
	}

	public void setMergedValue(float mergedValue) {
		this.mergedValue = mergedValue;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getBasePercent() {
		return basePercent;
	}

	public void setBasePercent(int basePercent) {
		this.basePercent = basePercent;
	}

	public int getFullValue() {
		return fullValue;
	}

	public void setFullValue(int fullValue) {
		this.fullValue = fullValue;
	}

	public int getSingleBless() {
		return singleBless;
	}

	public void setSingleBless(int singleBless) {
		this.singleBless = singleBless;
	}

	public int getSinglePercent() {
		return singlePercent;
	}

	public void setSinglePercent(int singlePercent) {
		this.singlePercent = singlePercent;
	}

	public int getBlessLimit() {
		return blessLimit;
	}

	public void setBlessLimit(int blessLimit) {
		this.blessLimit = blessLimit;
	}

	public int getBaseBless() {
		return baseBless;
	}

	public void setBaseBless(int baseBless) {
		this.baseBless = baseBless;
	}

	@Override
	public String toString() {
		return "PetMergedConfig [id=" + id + ", quality=" + quality
				+ ", mergedValue=" + mergedValue + ", propsId=" + propsId
				+ ", number=" + number + ", basePercent=" + basePercent
				+ ", fullValue=" + fullValue + ", singleBless=" + singleBless
				+ ", singlePercent=" + singlePercent + ", blessLimit="
				+ blessLimit + ", baseBless=" + baseBless + "]";
	}
	
}
