package com.yayo.warriors.module.fight.model;

/**
 * 吟唱CD对象
 * 
 * @author Hyint
 */
public class SingCoolTime {

	/** 
	 * 技能ID 
	 */
	private int skillId;
	
	/** 
	 * 吟唱CD的结束时间 
	 */
	private long coolTime;

	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public long getCoolTime() {
		return coolTime;
	}

	public void setCoolTime(long coolTime) {
		this.coolTime = coolTime;
	}
	
	public static SingCoolTime valueOf() {
		return new SingCoolTime();
	}

	@Override
	public String toString() {
		return "SingCoolTime [skillId=" + skillId + ", coolTime=" + coolTime + "]";
	}
}
