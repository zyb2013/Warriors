package com.yayo.warriors.basedb.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 家将"开蛋"几率
 * @author liuyuhua
 */
@Resource
public class PetEggConfig {
	
	@Id
	private int id;

	/** 掉落编号*/
	@Index(name="dropNo", order = 0)
	private int dropNo;
	
	/** 家将配置 {@link PetConfig#getId()}*/
	private int petConfigId;
	
	/** 概率*/
	private int rate;
	
	/** 满值概率*/
	private int fullRate;

	@JsonIgnore
	private PetConfig petConfig = null;
	//Getter and Setter...
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDropNo() {
		return dropNo;
	}

	public void setDropNo(int dropNo) {
		this.dropNo = dropNo;
	}

	public int getPetConfigId() {
		return petConfigId;
	}

	public void setPetConfigId(int petConfigId) {
		this.petConfigId = petConfigId;
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

	public PetConfig getPetConfig() {
		return petConfig;
	}

	public void setPetConfig(PetConfig petConfig) {
		this.petConfig = petConfig;
	}

	@Override
	public String toString() {
		return "PetEggProbability [id=" + id + ", dropNo=" + dropNo
				+ ", petConfigId=" + petConfigId + ", rate=" + rate
				+ ", fullRate=" + fullRate + "]";
	}
	
}
