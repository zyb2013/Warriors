package com.yayo.warriors.module.fight.model;

import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.FightMode;


/**
 * 区域详细信息VO
 * 
 * @author Hyint
 */
public class AreaVO {

	/** 发起攻击的单位 */
	private ISpire attacker;
	
	/** 被攻击者的单位 */
	private ISpire targeter;
	
	/** X坐标点 */
	private int positionX;
	
	/** Y坐标点 */
	private int positionY;

	/** 游戏地图对象 */
	private GameMap gameMap;
	
	/** 发起攻击者的战斗单位 */
	private ISpire attackOwner;
	
	/** 被攻击者的战斗单位 */
	private ISpire targetOwner;
	
	/** 战斗模式 */
	private FightMode fightMode = FightMode.PEACE;

	/** 攻击者的阵营 */
	private int attackCamp = Camp.NONE.ordinal();
	
	public ISpire getAttacker() {
		return attacker;
	}

	public ISpire getTargeter() {
		return targeter;
	}

	public int getPositionX() {
		return positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public FightMode getFightMode() {
		return fightMode;
	}

	public GameMap getGameMap() {
		return gameMap;
	}

	public int getAttackCamp() {
		return attackCamp;
	}

	/**
	 * 构建AOE区域信息
	 * 
	 * @param  attacker		攻击者
	 * @param  fightMode	战斗模式
	 * @param  positionX	X据点坐标
	 * @param  positionY	Y据点坐标
	 * @param  gameMap		地图对象
	 * @return
	 */
	public static AreaVO spireAoe(ISpire attacker, FightMode fightMode, int camp, int positionX, int positionY, GameMap gameMap) {
		AreaVO areaVO = new AreaVO();
		areaVO.gameMap = gameMap;
		areaVO.attackCamp = camp;
		areaVO.attacker = attacker;
		areaVO.fightMode = fightMode;
		areaVO.positionX = positionX;
		areaVO.positionY = positionY;
		return areaVO;
	}
	
	/**
	 * 构建AOE区域信息
	 * 
	 * @param  attacker		攻击者
	 * @param  fightMode	战斗模式
	 * @param  positionX	X据点坐标
	 * @param  positionY	Y据点坐标
	 * @param  gameMap		地图对象
	 * @return
	 */
	public static AreaVO spireAoe(ISpire attacker, ISpire attackOwner, FightMode fightMode, int camp, int positionX, int positionY, GameMap gameMap) {
		AreaVO areaVO = new AreaVO();
		areaVO.gameMap = gameMap;
		areaVO.attackCamp = camp;
		areaVO.attacker = attacker;
		areaVO.fightMode = fightMode;
		areaVO.positionX = positionX;
		areaVO.positionY = positionY;
		areaVO.attackOwner = attackOwner;
		return areaVO;
	}
	
	/**
	 * 家将打家将
	 * 
	 * @param  attacker			发起攻击的家将
	 * @param  attackOwner		发起攻击的家将的主人
	 * @param  targeter			被攻击的家将
	 * @param  targetOwner		被攻击的家将的主人
	 * @param  fightMode		战斗模式
	 * @param  attackCamp		攻击阵营
	 * @param  positionX		X坐标点
	 * @param  positionY		Y坐标点
	 * @param  gameMap			据点地图
	 * @return {@link AreaVO}	区域VO对象
	 */
	public static AreaVO petToPet(ISpire attacker, ISpire attackOwner, ISpire targeter, ISpire targetOwner, 
			FightMode fightMode, int attackCamp, int positionX, int positionY, GameMap gameMap) {
		AreaVO areaVO = new AreaVO();
		areaVO.gameMap = gameMap;
		areaVO.attacker = attacker;
		areaVO.targeter = targeter;
		areaVO.fightMode = fightMode;
		areaVO.positionX = positionX;
		areaVO.positionY = positionY;
		areaVO.attackCamp = attackCamp;
		areaVO.attackOwner = attackOwner;
		areaVO.targetOwner = targetOwner;
		return areaVO;
	}

	/**
	 * 家将打家将
	 * 
	 * @param  attacker			发起攻击的家将
	 * @param  targeter			被攻击的家将
	 * @param  targetOwner		被攻击的家将的主人
	 * @param  fightMode		战斗模式
	 * @param  attackCamp		攻击阵营
	 * @param  posX				X坐标点
	 * @param  posY				Y坐标点
	 * @param  gameMap			据点地图
	 * @return {@link AreaVO}	区域VO对象
	 */
	public static AreaVO spireToPet(ISpire attacker, ISpire targeter, ISpire targetOwner, 
			FightMode fightMode, int attackCamp, int posX, int posY, GameMap gameMap) {
		AreaVO areaVO = new AreaVO();
		areaVO.gameMap = gameMap;
		areaVO.attacker = attacker;
		areaVO.targeter = targeter;
		areaVO.fightMode = fightMode;
		areaVO.positionX = posX;
		areaVO.positionY = posY;
		areaVO.attackCamp = attackCamp;
		areaVO.targetOwner = targetOwner;
		return areaVO;
	}

	/**
	 * 家将打其他精灵
	 * 
	 * @param  attacker			发起攻击的家将
	 * @param  attackOwner		发起攻击的家将的主人
	 * @param  targeter			被攻击的家将
	 * @param  targetOwner		被攻击的家将的主人
	 * @param  fightMode		战斗模式
	 * @param  attackCamp		攻击阵营
	 * @param  posX				X坐标点
	 * @param  posY				Y坐标点
	 * @param  gameMap			据点地图
	 * @return {@link AreaVO}	区域VO对象
	 */
	public static AreaVO petToSpire(ISpire attacker, ISpire attackOwner, ISpire targeter, 
			FightMode fightMode, int attackCamp, int posX, int posY, GameMap gameMap) {
		AreaVO areaVO = new AreaVO();
		areaVO.positionX = posX;
		areaVO.positionY = posY;
		areaVO.gameMap = gameMap;
		areaVO.attacker = attacker;
		areaVO.targeter = targeter;
		areaVO.fightMode = fightMode;
		areaVO.attackCamp = attackCamp;
		areaVO.attackOwner = attackOwner;
		return areaVO;
	}
	
	/**
	 * 其他精灵攻击精灵
	 * 
	 * @param attacker
	 * @param targeter
	 * @param fightMode
	 * @param positionX
	 * @param positionY
	 * @param gameMap
	 * @return
	 */
	public static AreaVO spireToSpire(ISpire attacker, ISpire targeter, FightMode fightMode, int attackCamp, int x, int y, GameMap gameMap) {
		AreaVO areaVO = new AreaVO();
		areaVO.positionX = x;
		areaVO.positionY = y;
		areaVO.gameMap = gameMap;
		areaVO.attacker = attacker;
		areaVO.targeter = targeter;
		areaVO.fightMode = fightMode;
		areaVO.attackCamp = attackCamp;
		return areaVO;
	}

 
	public ISpire getAttackOwner() {
		return attackOwner;
	}

	public ISpire getTargetOwner() {
		return targetOwner;
	}
}
