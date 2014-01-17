package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

@Resource
public class PetQualityConfig implements Comparable<PetQualityConfig>{

	@Id
	private int id;
	
	/** 编号{@link PetConfig#getAptitudeNo()}*/
	@Index(name="aptitudeNo", order = 0)
	private int aptitudeNo;
	
	/** 资质下限*/
	private int minQuality;
	
	/** 资质上限*/
	private int maxQuality;
	
	/** 概率*/
	private int rate;
	
	/** 满值区间*/
	private int fullRate;

	//Getter and Setter...
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAptitudeNo() {
		return aptitudeNo;
	}

	public void setAptitudeNo(int aptitudeNo) {
		this.aptitudeNo = aptitudeNo;
	}

	public int getMinQuality() {
		return minQuality;
	}

	public void setMinQuality(int minQuality) {
		this.minQuality = minQuality;
	}

	public int getMaxQuality() {
		return maxQuality;
	}

	public void setMaxQuality(int maxQuality) {
		this.maxQuality = maxQuality;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getFullRate() {
		return fullRate;
	}

	public void setFullRate(int fullRate) {
		this.fullRate = fullRate;
	}
	
	public int getRandomQuality() {
		return this.minQuality + Tools.getRandomInteger(this.maxQuality - this.minQuality + 1);
	}

	
	public String toString() {
		return "PetQualityConfig [id=" + id + ", aptitudeNo=" + aptitudeNo
				+ ", minQuality=" + minQuality + ", maxQuality=" + maxQuality
				+ ", rate=" + rate + ", fullRate=" + fullRate + "]";
	}

	
	public int compareTo(PetQualityConfig o) {
		if(o == null) {
			return -1;
		}
		int rate = o.rate;
		return (this.rate < rate ? -1 : (this.rate == rate ? 0 : 1));
	}
	
}
