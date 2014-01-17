package com.yayo.warriors.basedb.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.skill.rule.SkillRule;
import com.yayo.warriors.module.skill.type.CastTarget;
import com.yayo.warriors.module.skill.type.Classify;
import com.yayo.warriors.module.skill.type.SkillType;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.IndexName;

/**
 * 技能基础表
 * 
 * @author Hyint
 */
@Resource
public class SkillConfig {

	/** 技能ID */
	@Id
	private int id;

	/** 冷却时间表的ID */
	private int cdId = 0;
	
	/** 技能名字 */
	private String name;

	/** 攻击距离 */
	private int distance = 0;
	
	/** 最大等级 */
	private int maxLevel = 0;
	
	/** 使用消耗MP量 */
	private String mpExpr = "";
	
	/** 出招延时 */
	private int delayMoves = 0;

	/** 释放技能给玩家的模式(当该技能作用给玩家/家将的时候, 是否需要杀戮, 和平模式?). 格式: 模式1_模式2 */
	private String fightMode = "";

	/** 技能类型. */
	private int type = SkillType.NONE.ordinal();
	
	/** 角色学习该技能需要的职业 */
	private int job = Job.COMMON.ordinal();

	/** 释放的目标 */
	private int castTarget = CastTarget.AREA.ordinal();

	/** 技能分类 */
	@Index(name=IndexName.SKILL_CLASSIFY, order = 0)
	private int classify = Classify.NORMAL_SKILL.ordinal();
	
	/** 战斗模式列表 */
	@JsonIgnore
	private Set<Integer> fightModeSet = null;
	
	/** 技能效果列表 */
	@JsonIgnore
	private List<SkillEffectConfig> skillEffects = null;
	
	/** 可以学习的技能信息 */
	@JsonIgnore
	private Map<Integer, SkillLearnConfig> skillLearns = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCdId() {
		return cdId;
	}

	public void setCdId(int cdId) {
		this.cdId = cdId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public String getMpExpr() {
		return mpExpr;
	}
	
	public int getSkillCostMp(int skillLevel) {
		return FormulaHelper.invoke(this.mpExpr, skillLevel).intValue();
	}

	public void setMpExpr(String mpExpr) {
		this.mpExpr = mpExpr;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getClassify() {
		return classify;
	}

	public void setClassify(int classify) {
		this.classify = classify;
	}

	public List<SkillEffectConfig> getSkillEffects() {
		return skillEffects;
	}

	public void setSkillEffects(List<SkillEffectConfig> skillEffects) {
		this.skillEffects = skillEffects;
	}
	
	public boolean canLearnSkill(Job playerJob) {
		return this.job == Job.COMMON.ordinal() || this.job == playerJob.ordinal();
	}
	
	public SkillLearnConfig getLearnSkill(int level) {
		return this.skillLearns.get(level);
	}
	
	public void setSkillLearns(Map<Integer, SkillLearnConfig> skillLearns) {
		this.skillLearns = skillLearns;
	}

	public Map<Integer, SkillLearnConfig> getSkillLearns() {
		return skillLearns;
	}

	public String getFightMode() {
		return fightMode;
	}

	public void setFightMode(String fightMode) {
		this.fightMode = fightMode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCastTarget() {
		return castTarget;
	}

	public void setCastTarget(int castTarget) {
		this.castTarget = castTarget;
	}

	public int getDelayMoves() {
		return delayMoves;
	}

	public void setDelayMoves(int delayMoves) {
		this.delayMoves = delayMoves;
	}
	
	/**
	 * 获得战斗模式集合
	 * 
	 * @return {@link Set}		战斗模式集合
	 */
	public Set<Integer> getFight2PlayerModeSet() {
		if(this.fightModeSet != null) {
			return this.fightModeSet;
		}
		
		synchronized (this) {
			if(this.fightModeSet != null) {
				return this.fightModeSet;
			}
			
			this.fightModeSet = new HashSet<Integer>();
			if(StringUtils.isBlank(this.fightMode)) {
				return this.fightModeSet;
			}
			
			String[] array = this.fightMode.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : array) {
				this.fightModeSet.add(Integer.valueOf(element));
			}
		}
		return this.fightModeSet;
	}
	
	/**
	 * 是否可以学习
	 * 
	 * @return {@link Boolean}	是否可以学习
	 */
	public boolean isCanLearn() {
		return ArrayUtils.contains(SkillRule.CANLEARN_SKILL_TYPES, this.type);
	}
	
	/**
	 * 是否主动技能
	 * 
	 * @return {@link Boolean}	true-主动技能, false-被动技能
	 */
	public boolean isActivity() {
		return ArrayUtils.contains(SkillRule.ACTIVE_SKILL_TYPES, this.type);
	}
	
	/**
	 * 
	 * 验证角色攻击角色, 是否可以攻击
	 * 
	 * @param  fightMode		角色的模式
	 * @return {@link Boolean}	是否可以攻击玩家
	 */
	public boolean validFightModeWithP2P(int fightMode) {
		return this.getFight2PlayerModeSet().contains(fightMode);
	}
	
	/**
	 * 验证技能类型
	 * 
	 * @param  skillTypes		技能类型数组
	 * @return {@link Boolean}	是否了类型符合
	 */
	public boolean validateSkillType(int...skillTypes) {
		return ArrayUtils.contains(skillTypes, this.type);
	}

}
