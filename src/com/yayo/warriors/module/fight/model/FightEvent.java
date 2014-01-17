package com.yayo.warriors.module.fight.model;

import java.util.Collection;

import com.yayo.warriors.module.map.domain.ISpire;

/**
 * 战斗事件对象
 * 
 * @author Hyint
 */
public class FightEvent {
	
	/** 使用的技能ID */
	private int skillId;

	/** 战斗上下文 */
	private Context context;
	
	/** 攻击者 */
	private ISpire attacker;
	
	/** 被攻击者 */
	private ISpire targeter;
	
	/** 通知的玩家 */
	private Collection<Long> viewPlayers;
	
	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Collection<Long> getViewPlayers() {
		return viewPlayers;
	}
	
	public void setViewPlayers(Collection<Long> viewPlayers) {
		this.viewPlayers = viewPlayers;
	}

	public ISpire getTargeter() {
		return targeter;
	}

	public void setTargeter(ISpire targeter) {
		this.targeter = targeter;
	}

	/**
	 * 构建战斗事件对象
	 * 
	 * @param  attacker				释放技能的战斗单位
	 * @param  skillId				技能ID
	 * @param  context				战斗上下文
	 * @param  viewPlayers			可视区域的角色ID列表
	 * @return {@link FightEvent}	
	 */
	public static FightEvent valueOf(ISpire attacker, int skillId, Context context, Collection<Long> viewPlayers) {
		FightEvent fightEvent = new FightEvent();
		fightEvent.skillId = skillId;
		fightEvent.context = context;
		fightEvent.attacker = attacker;
		fightEvent.viewPlayers = viewPlayers;
		return fightEvent;
	}

	/**
	 * 构建战斗事件对象
	 * 
	 * @param  attacker				释放技能的战斗单位
	 * @param  targeter				被释放技能的战斗单位
	 * @param  skillId				技能ID
	 * @param  context				战斗上下文
	 * @param  viewPlayers			可视区域的角色ID列表
	 * @return {@link FightEvent}	
	 */
	public static FightEvent valueOf(ISpire attacker, ISpire targeter, int skillId, Context context, Collection<Long> viewPlayers) {
		FightEvent fightEvent = new FightEvent();
		fightEvent.skillId = skillId;
		fightEvent.context = context;
		fightEvent.targeter = targeter;
		fightEvent.attacker = attacker;
		fightEvent.viewPlayers = viewPlayers;
		return fightEvent;
	}

	public ISpire getAttacker() {
		return attacker;
	}

	public void setAttacker(ISpire attacker) {
		this.attacker = attacker;
	}

	@Override
	public String toString() {
		return "FightEvent [skillId=" + skillId + ", context=" + context + ", attacker=" + attacker
				+ ", targeter=" + targeter + ", viewPlayers=" + viewPlayers + "]";
	}
}
