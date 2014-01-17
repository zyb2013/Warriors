package com.yayo.warriors.module.fight.model;

import java.io.Serializable;

import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.fight.type.EffectType;
import com.yayo.warriors.module.fight.type.HitState;
import com.yayo.warriors.module.fight.type.State;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 战斗报告信息
 * 
 * @author Hyint
 */
public class FightReport implements Serializable {
	private static final long serialVersionUID = 8033380606993724389L;
	
	/** 该技能属性的承受者 */
	private UnitId unitId;
	
	/** 技能类型. {@link EffectType} */
	private EffectType skillType;
	
	/** 施放的技能 */
	private int skillId;
	
	/** 技能触发后影响承受者的效果类型.  详细见: {@link AttributeKeys} */
	private int attribute;
	
	/** 触发效果对应的属性值 */
	private int value;

	/** 是否暴击 */
	private boolean critical;

	/** 是否可以被击飞, true-可以被击飞, false-不可以被击飞 */
	private boolean knockFly;
	
	/** 角色的状态结束时间. 如果为 -1, 且技能类型为BUFF, 则为删除某个BUFF */
	private long statusEndTime;
	
	/** 命中状态 */
	private HitState hitState = HitState.HIT;
	
	/** 角色的状态.  0 - 正常、1 - 死亡、2 - 复活*/
	private State state = State.NORMAL;
	
	/** 擒拿手, 击退效果的最终X坐标点 */
	private int x = -1;

	/** 擒拿手, 击退效果的最终Y坐标点 */
	private int y = -1;
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public HitState getHitState() {
		return hitState;
	}

	public void setHitState(HitState hitState) {
		this.hitState = hitState;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}
	
	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public EffectType getReportSkillType() {
		return skillType;
	}

	public void setReportSkillType(EffectType reportSkillType) {
		this.skillType = reportSkillType;
	}

	public EffectType getSkillType() {
		return skillType;
	}

	public void setSkillType(EffectType skillType) {
		this.skillType = skillType;
	}

	public UnitId getUnitId() {
		return unitId;
	}

	public void setUnitId(UnitId unitId) {
		this.unitId = unitId;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public long getStatusEndTime() {
		return statusEndTime;
	}

	public void setStatusEndTime(long statusEndTime) {
		this.statusEndTime = statusEndTime;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isKnockFly() {
		return knockFly;
	}

	public void setKnockFly(boolean knockFly) {
		this.knockFly = knockFly;
	}

	@Override
	public String toString() {
		return "FightReport [unitId=" + unitId + ", skillType=" + skillType + ", skillId="
				+ skillId + ", attribute=" + attribute + ", value=" + value + ", critical="
				+ critical + ", knockFly=" + knockFly + ", statusEndTime=" + statusEndTime
				+ ", hitState=" + hitState + ", state=" + state + ", x=" + x + ", y=" + y + "]";
	}
	
	/**
	 * 攻击MISS
	 * 
	 * @param  unitId				被攻击目标
	 * @param  skillType			技能类型
	 * @param  skillId				技能ID
	 * @return {@link FightReport}	战斗报告
	 */
	public static FightReport miss(UnitId unitId, int skillId) {
		FightReport fightReport = new FightReport();
		fightReport.unitId = unitId;
		fightReport.skillId = skillId;
		fightReport.hitState = HitState.MISS;
		fightReport.skillType = EffectType.SKILL;
		return fightReport;
	}

	/**
	 * 攻击免疫
	 * 
	 * @param  unitId				被攻击目标
	 * @param  skillType			技能类型
	 * @param  skillId				技能ID
	 * @return {@link FightReport}	战斗报告
	 */
	public static FightReport immune(UnitId unitId, int skillId) {
		FightReport fightReport = new FightReport();
		fightReport.unitId = unitId;
		fightReport.skillId = skillId;
		fightReport.hitState = HitState.IMMUNE;
		fightReport.skillType = EffectType.SKILL;
		return fightReport;
	}
	
	/**
	 * 技能攻击, 普通伤害
	 * 
	 * @param  unitId				攻击对象
	 * @param  skillId				技能ID
	 * @param  attribute			角色的属性
	 * @param  value				造成属性对应的值影响
	 * @return {@link FightReport}	战报对象
	 */
	public static FightReport attack(UnitId unitId, int skillId, int attribute, int value) {
		FightReport fightReport = new FightReport();
		fightReport.value = value;
		fightReport.unitId = unitId;
		fightReport.skillId = skillId;
		fightReport.attribute = attribute;
		fightReport.skillType = EffectType.EFFECT;
		return fightReport;
	}
	
	/**
	 * 技能攻击, 普通伤害
	 * 
	 * @param  unitId				攻击对象
	 * @param  skillId				技能ID
	 * @param  attribute			角色的属性
	 * @param  value				造成属性对应的值影响
	 * @param  critical				是否暴击
	 * @param  knockFly				击飞效果
	 * @return {@link FightReport}	战报对象
	 */
	public static FightReport attack(UnitId unitId, int skillId, int attribute, int value, boolean critical, boolean knockFly) {
		FightReport fightReport = new FightReport();
		fightReport.value = value;
		fightReport.unitId = unitId;
		fightReport.skillId = skillId;
		fightReport.critical = critical;
		fightReport.knockFly = knockFly;
		fightReport.attribute = attribute;
		fightReport.skillType = EffectType.EFFECT;
		return fightReport;
	}
	
	/**
	 * 技能攻击, 普通伤害
	 * 
	 * @param  unitId				攻击对象
	 * @param  skillId				技能ID
	 * @param  attribute			角色的属性
	 * @param  value				造成属性对应的值影响
	 * @param  critical				是否暴击
	 * @param  knockFly				是否可以被击飞
	 * @param  state				角色的状态
	 * @return {@link FightReport}	战报对象
	 */
	public static FightReport attack(UnitId unitId, int skillId, int attribute, int value, boolean critical, boolean knockFly, State state) {
		FightReport fightReport = new FightReport();
		fightReport.state = state;
		fightReport.value = value;
		fightReport.unitId = unitId;
		fightReport.skillId = skillId;
		fightReport.critical = critical;
		fightReport.knockFly = knockFly;
		fightReport.attribute = attribute;
		fightReport.skillType = EffectType.EFFECT;
		return fightReport;
	}
	/**
	 * 技能攻击, 普通伤害
	 * 
	 * @param  unitId				攻击对象
	 * @param  skillId				技能ID
	 * @param  attribute			角色的属性
	 * @param  value				造成属性对应的值影响
	 * @param  critical				是否暴击
	 * @param  knockFly				击飞效果
	 * @param  state				角色的状态
	 * @param  point				坐标点
	 * @return {@link FightReport}	战报对象
	 */
	public static FightReport attack(UnitId unitId, int skillId, int attribute, int value, boolean critical, boolean knockFly, State state, Point point) {
		FightReport fightReport = new FightReport();
		fightReport.x = point.x;
		fightReport.y = point.y;
		fightReport.state = state;
		fightReport.value = value;
		fightReport.unitId = unitId;
		fightReport.skillId = skillId;
		fightReport.critical = critical;
		fightReport.knockFly = knockFly;
		fightReport.attribute = attribute;
		fightReport.skillType = EffectType.EFFECT;
		return fightReport;
	}

	/**
	 * 技能攻击, 普通伤害
	 * 
	 * @param  unitId				攻击对象
	 * @param  skillId				技能ID
	 * @param  endTime				效果结束时间
	 * @return {@link FightReport}	战报对象
	 */
	public static FightReport buffer(UnitId unitId, int bufferId, long endTime) {
		FightReport fightReport = new FightReport();
		fightReport.unitId = unitId;
		fightReport.skillId = bufferId;
		fightReport.statusEndTime = endTime;
		fightReport.skillType = EffectType.EFFECT;
		return fightReport;
	}
}
