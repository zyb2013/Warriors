package com.yayo.warriors.module.map.types;

import com.yayo.warriors.module.user.type.FightMode;

/**
 * 场景类型
 * @author liuyuhua
 */
public enum ScreenType {

	/** 0 - 新手村 */
	BIRTHPLACE(FightMode.PEACE.ordinal(), FightMode.PEACE),
	
	/** 1 - 主城*/
	CASTLE(FightMode.PEACE.ordinal(), FightMode.PEACE),
	
	/** 2 - 中立城 */
	NEUTRAL(-1, FightMode.PEACE, FightMode.KILLING, FightMode.CAMPING),
	
	/** 3 - 副本*/
	DUNGEON(FightMode.PEACE.ordinal(), FightMode.PEACE),
	
	/** 4 - 野外地图 */
	FIELD(FightMode.PEACE.ordinal(), FightMode.PEACE),
	
	/** 5 - 战场类型 */
	BATTLE_FIELD(FightMode.KILLING.ordinal(), FightMode.KILLING),
	
	/** 6 - 阵营类型*/
	CAMP(FightMode.CAMPING.ordinal(),FightMode.CAMPING);
	
	/** 需要强制切换的模式 */
	private int forceMode = -1;
	
	/** 该图可以使用的模式 */
	private FightMode[] fightModes;
	
	/**
	 * 场景类型
	 * 
	 * @param forceMode		强制切换的类型. -1 - 保留原来的模式
	 * @param fightModes	可以切换的模式类型
	 */
	ScreenType(int forceMode, FightMode...fightModes) {
		this.forceMode = forceMode;
		this.fightModes = fightModes;
	}

	public FightMode[] getFightModes() {
		return fightModes;
	}

	public int getForceMode() {
		return forceMode;
	}
}
