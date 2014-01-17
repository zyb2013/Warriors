package com.yayo.warriors.basedb.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import static com.yayo.warriors.type.IndexName.*;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.user.type.Job;

/**
 * 肉身基础对象
 * @author huachaoping
 *
 */
@Resource
public class MortalBodyConfig {
	
	/** 自增id */ 
	@Id
	private int id;
	
	/** 肉身类型 */
	@Index(name=MORTAL_JOB_TYPE_LEVEL, order=1)
	private int type;
	
	/** 职业{@link Job} */
	@Index(name=MORTAL_JOB_TYPE_LEVEL, order=0)
	private int job;
	
	/** 肉身等级 */
	@Index(name=MORTAL_JOB_TYPE_LEVEL, order=2)
	private int level;
	
	/** 加成属性KEY, 格式: 属性类型|属性类型 */
	private String attributeKey;
	
	/** 属性值, 格式: 属性值|属性值 */
	private String value;
	
	/** 升级概率 */
	private int probability;
	
	/** 升级所需道具Id */
	private int requiredPropsId;
	
	/** 升级所需道具数量 */
	private int requiredCount;
	
	/** 升级所需铜币 */
	private int requiredMoney;
	
	/** 概率最大值 */
	private int maxProbability;
	
	/** 加成属性KEY */
	@JsonIgnore
	private int[] attributes = null;
	
	/** 属性值 */
	@JsonIgnore
	private int[] values = null;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getProbability() {
		return probability;
	}
	
	public String getAttributeKey() {
		return attributeKey;
	}
	
	public void setAttributeKey(String attributeKey) {
		this.attributeKey = attributeKey;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public int getRequiredPropsId() {
		return requiredPropsId;
	}

	public void setRequiredPropsId(int requiredPropsId) {
		this.requiredPropsId = requiredPropsId;
	}

	public int getRequiredCount() {
		return requiredCount;
	}

	public void setRequiredCount(int requiredCount) {
		this.requiredCount = requiredCount;
	}

	public int getMaxProbability() {
		return maxProbability;
	}

	public void setMaxProbability(int maxProbability) {
		this.maxProbability = maxProbability;
	}

	public int getRequiredMoney() {
		return requiredMoney;
	}

	public void setRequiredMoney(int requiredMoney) {
		this.requiredMoney = requiredMoney;
	}
	
	
	public int[] getAttributes() {
		if (this.attributes != null) {
			return attributes;
		}
		
		String[] arrays = this.attributeKey.split(Splitable.ELEMENT_SPLIT);
		this.attributes = new int[arrays.length];
		for (int i = 0; i <= arrays.length - 1; i++) {
			this.attributes[i] = Integer.valueOf(arrays[i]); 
		}
		return this.attributes;
	}
	
	
	public int[] getValues() {
		if (this.values != null) {
			return this.values;
		}
		
		String[] arrays = this.value.split(Splitable.ELEMENT_SPLIT);
		this.values = new int[arrays.length];
		for (int i = 0; i <= arrays.length - 1; i++) {
			this.values[i] = Integer.valueOf(arrays[i]);
		}
		return this.values;
	}
	
}
