package com.yayo.warriors.basedb.model;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.type.IndexName;

/**
 * 帮派技能
 * @author liuyuhua
 */
@Resource
public class AllianceSkillConfig {
	
	/** 增量编号*/
	@Id
	private int id;
	
	/** 技能等级*/
	@Index(name = IndexName.ALLIANCE_SKILL_LEVEL, order = 1)
	private int skillLevel;
	
	/** 技能ID*/
	@Index(name = IndexName.ALLIANCE_SKILL_LEVEL, order = 0)
	private int skillId;
	
	/** 最低帮派技能建筑等级*/
	private int buildLevel;
	
	/** 研究消耗资金*/
	private int researchSilver;
	
	/** 前置技能 {技能ID_等级}*/
	private String exSkill;
	
	/** 学习技能 消耗贡献值*/
	private int donate;
	
	/** 学习消耗资金*/
	private int learnSilver;
	
	/** 研究技能,的技能前置约束条件*/
	@JsonIgnore
	private int[] researchSkillCondition;
	
	/** 技能所增加的属性*/
	private int attribute;
	
	/** 技能属性增加的值*/
	private int attrValue;
	
	/**
	 * 研究技能,的技能前置约束条件
	 * int[0] = 前置技能ID
	 * int[1] = 前置技能等级
	 * @return {@link int[]}
	 */
	public int[] getResearchSkillCondition() {
		if(this.researchSkillCondition != null) {
			return this.researchSkillCondition;
		}
		
		synchronized (this) {
			if(this.researchSkillCondition != null) {
				return this.researchSkillCondition;
			}
			
			this.researchSkillCondition = new int[2];
			if(StringUtils.isBlank(exSkill)) {
				return this.researchSkillCondition;
			}
			
			String[] array = exSkill.split(Splitable.ATTRIBUTE_SPLIT);
			if(array.length >= 2) {
				this.researchSkillCondition[0] = Integer.valueOf(array[0]);
				this.researchSkillCondition[1] = Integer.valueOf(array[1]);
			}
		}
		return this.researchSkillCondition;
	}
	
	//Getter and Setter...

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
	}

	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public int getBuildLevel() {
		return buildLevel;
	}

	public void setBuildLevel(int buildLevel) {
		this.buildLevel = buildLevel;
	}

	public int getResearchSilver() {
		return researchSilver;
	}

	public void setResearchSilver(int researchSilver) {
		this.researchSilver = researchSilver;
	}

	public String getExSkill() {
		return exSkill;
	}

	public void setExSkill(String exSkill) {
		this.exSkill = exSkill;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}

	public int getLearnSilver() {
		return learnSilver;
	}

	public void setLearnSilver(int learnSilver) {
		this.learnSilver = learnSilver;
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

	@Override
	public String toString() {
		return "AllianceSkillConfig [id=" + id + ", skillLevel=" + skillLevel
				+ ", skillId=" + skillId + ", buildLevel=" + buildLevel
				+ ", researchSilver=" + researchSilver + ", exSkill=" + exSkill
				+ ", donate=" + donate + ", learnSilver=" + learnSilver
				+ ", attribute=" + attribute + ", attrValue=" + attrValue + "]";
	}
	
}
