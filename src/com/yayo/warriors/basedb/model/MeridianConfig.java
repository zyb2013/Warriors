 package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 经脉基础对象
 * 
 * @author huachaoping
 */
@Resource
public class MeridianConfig {
	
	/** 自增id*/
	@Id
	private int id;
	
	/** 经脉类型*/
	private int meridianType;
	
	/** 经脉阶段 */
	private int meridianStage;
	
	/** 职业 */
	private int job;
	
	/** 条件Id*/
	private int chain;

	/** 加成属性key*/
	private int attrKey;
	
	/** 加成属性值*/
	private int value;
	
	/** 冲穴成功几率*/
	private int probability;
	
	/** 额外属性加成 */
	private int extraAttr;
	
	/** 冲穴所需道具类型*/
	private int requiredPropsChildType;
	
	/** 所需真气 */
	private int requiredGas;
	
	/** 所需数量*/
	private int requiredCount;
	
	
	public int getMeridianStage() {
		return meridianStage;
	}

	public void setMeridianStage(int meridianStage) {
		this.meridianStage = meridianStage;
	}

	public int getRequiredPropsChildType() {
		return requiredPropsChildType;
	}

	public void setRequiredPropsChildType(int requiredPropsChildType) {
		this.requiredPropsChildType = requiredPropsChildType;
	}

	public int getRequiredCount() {
		return requiredCount;
	}

	public void setRequiredCount(int requiredCount) {
		this.requiredCount = requiredCount;
	}

	public int getExtraAttr() {
		return extraAttr;
	}

	public void setExtraAttr(int extraAttr) {
		this.extraAttr = extraAttr;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public int getChain() {
		return chain;
	}
	
	public void setChain(int chain) {
		this.chain = chain;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMeridianType() {
		return meridianType;
	}

	public void setMeridianType(int meridianType) {
		this.meridianType = meridianType;
	}

	public int getAttrKey() {
		return attrKey;
	}

	public void setAttrKey(int attrKey) {
		this.attrKey = attrKey;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getRequiredGas() {
		return requiredGas;
	}

	public void setRequiredGas(int requiredGas) {
		this.requiredGas = requiredGas;
	}
	
}
