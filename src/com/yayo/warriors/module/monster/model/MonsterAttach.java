package com.yayo.warriors.module.monster.model;

import com.yayo.warriors.module.user.model.Fightable;

/**
 * 怪物的附加属性
 * 
 * @author Hyint
 */
public class MonsterAttach {
	
	/** 最后处理的BUFF信息 */
	private Fightable afterBufferable = new Fightable();
	
	/** 最优先处理的BUFF信息 */
	private Fightable beforeBufferable = new Fightable();

	public Fightable getAfterBufferable() {
		return afterBufferable;
	}

	public Fightable getBeforeBufferable() {
		return beforeBufferable;
	}
	
	
}
