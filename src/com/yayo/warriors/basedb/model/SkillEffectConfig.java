package com.yayo.warriors.basedb.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.skill.type.TargetSite;
import com.yayo.warriors.type.IndexName;

/**
 * 技能效果配置信息
 * 
 * @author Hyint
 */
@Resource
public class SkillEffectConfig implements Comparable<SkillEffectConfig>{

	/** 自增序号ID */
	@Id
	private int id;
	
	/** 效果作用范围 */
	private int area;
	
	/** 跳动周期 */
	private int cycle;
	
	/** 技能效果编号*/
	@Index(name= IndexName.SKILLEFFECT_EFFECT_TYPE, order=0)
	private int effectType;
	
	/** 技能触发的BUFF的类型. 当该值大于0, 根据该值做BUFF效果唯一性 */
	private int buffType;
	
	/** 技能ID */
	@Index(name=IndexName.SKILL_EFFECT_SKILLID, order = 0)
	private int skillId;
	
	/** 技能触发几率. n1: 技能等级 */
	private String rate;
	
	/** 效果持续时间. 单位: 毫秒*/
	private int effectTime;
	
	/** 效果作用人数 */
	private int targetCount;

	/** 效果公式. n1: 技能等级 */
	private String effectExpr;
	
	/** 角色出招延迟, 当是DEBUFF定身效果时, 才使用该值 */
	private int delayMoves;
	
	/** 是否忽略命中(true-不命中也可以攻击, false-命中才能攻击) */
	private boolean ignoreHitAttack;
	
	/** 技能效果目标 */
	private int targetSite = TargetSite.MYSELF.ordinal();

	@JsonIgnore
	private SkillConfig skillConfig;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public int getTargetCount() {
		return targetCount;
	}

	public void setTargetCount(int targetCount) {
		this.targetCount = targetCount;
	}

	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public int getEffectType() {
		return effectType;
	}

	public void setEffectType(int effectType) {
		this.effectType = effectType;
	}

	public String getRate() {
		return rate;
	}

	public int getRateValue(int skillLevel) {
		return FormulaHelper.invoke(this.rate, skillLevel).intValue();
	}
	
	public void setRate(String rate) {
		this.rate = rate;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(int effectTime) {
		this.effectTime = effectTime;
	}

	public String getEffectExpr() {
		return effectExpr;
	}

	public void setEffectExpr(String effectExpr) {
		this.effectExpr = effectExpr;
	}
	
	public Number calcSkillEffect(int damageValue, Number...params) {
		return FormulaHelper.invoke(this.effectExpr, params).doubleValue() * damageValue;
	}

	public int getTargetSite() {
		return targetSite;
	}

	public void setTargetSite(int targetSite) {
		this.targetSite = targetSite;
	}

	public int getBuffType() {
		return buffType;
	}

	public void setBuffType(int bufferType) {
		this.buffType = bufferType;
	}

	public int getDelayMoves() {
		return delayMoves;
	}

	public void setDelayMoves(int delayMoves) {
		this.delayMoves = delayMoves;
	}

	
	public int compareTo(SkillEffectConfig o) {
		return o == null || this.id < o.getId() ? -1 : (this.id == o.getId() ? 0 : 1);
	}

	public static SkillEffectConfig valueOf(int id) {
		SkillEffectConfig effectConfig = new SkillEffectConfig();
		effectConfig.id = id;
		return effectConfig;
	}
	
	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public SkillConfig getSkillConfig() {
		return skillConfig;
	}

	public void updateSkillConfig(SkillConfig skillConfig) {
		this.skillConfig = skillConfig;
	}

	public boolean isIgnoreHitAttack() {
		return ignoreHitAttack;
	}

	public void setIgnoreHitAttack(boolean ignoreHitAttack) {
		this.ignoreHitAttack = ignoreHitAttack;
	}

	
	public String toString() {
		return "SkillEffectConfig [id=" + id + ", area=" + area + ", effectType=" + effectType
				+ ", bufferType=" + buffType + ", skillId=" + skillId + ", rate=" + rate
				+ ", effectTime=" + effectTime + ", targetCount=" + targetCount + ", effectExpr="
				+ effectExpr + ", targetSite=" + targetSite + "]";
	}
}
