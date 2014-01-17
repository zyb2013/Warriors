package com.yayo.warriors.module.user.model;


/**
 * 用户属性刷新封装类
 * 
 * @author Hyint
 */
public class UserAttach {

	/** 坐骑是否乘骑 */
	private boolean isRiding = false;
	
	/** 时装是否超时 */
	private boolean factionOutOfExpiration = false;
	
	/** 用户技能附加信息 */
	private Fightable skillable = new Fightable(0);

	/** 经脉点数属性值 */
	private Fightable meridianable = new Fightable(0);
	
	/** 肉身附加属性值 */
	private Fightable mortalable = new Fightable(0);

	/** 用户装备基础属性信息 */
	private Fightable equipable = new Fightable(0);

	/** 装备的神武属性 */
	private Fightable shenwuable = new Fightable(0);
	
	/** 装备的附加属性 */
	private Fightable equipAdditions = new Fightable(0);
	
	/** 装备强化属性 */
	private Fightable equipEnhancable = new Fightable(0);

	/** 用户装备镶嵌属性 */
	private Fightable userEquipHoles = new Fightable(0);
	
	/** 用户装备的套装属性 */
	private Fightable equipSuits = new Fightable(0);
	
	/** 用户装备的星级套装属性 */
	private Fightable equipStarSuits = new Fightable(0);
	
	/** 用户坐骑附加属性信息  */
	private Fightable userHorsesable = new Fightable(0);

	/** 用户骑乘坐骑附加属性信息  */
	private Fightable ridHorsesable = new Fightable(0);
	
	/** 最后处理的BUFF信息 */
	private Fightable afterBufferable = new Fightable(0);
	
	/** 最优先处理的BUFF信息 */
	private Fightable beforeBufferable = new Fightable(0);
	
	/** 家将真传(附身)附加属性信息*/
	private Fightable userPetMerged = new Fightable(0);
	
	/** 好友度BUFF加成 */
	private Fightable friendlyAdded = new Fightable(0);

	public boolean isRiding() {
		return isRiding;
	}

	public void setRiding(boolean isRiding) {
		this.isRiding = isRiding;
	}

	public Fightable getMortalable() {
		return mortalable;
	}

	public Fightable getSkillable() {
		return skillable;
	}

	public Fightable getBeforeBufferable() {
		return beforeBufferable;
	}

	public Fightable getAfterBufferable() {
		return afterBufferable;
	}

	public Fightable getEquipable() {
		return equipable;
	}

	public void setEquipable(Fightable equipable) {
		this.equipable = equipable;
	}

	public Fightable getHorsesable() {
		return userHorsesable;
	}

	public boolean isFactionOutOfExpiration() {
		return factionOutOfExpiration;
	}

	public void setFactionOutOfExpiration(boolean factionOutOfExpiration) {
		this.factionOutOfExpiration = factionOutOfExpiration;
	}

	public Fightable getEquipHoles() {
		return userEquipHoles;
	}

	public Fightable getMeridianable() {
		return meridianable;
	}

	public void setMeridianable(Fightable meridianable) {
		this.meridianable = meridianable;
	}

	public Fightable getEquipSuits() {
		return equipSuits;
	}

	public Fightable getEquipStarSuits() {
		return equipStarSuits;
	}

	public Fightable getEquipEnhancable() {
		return equipEnhancable;
	}

	public Fightable getEquipAdditions() {
		return equipAdditions;
	}

	public Fightable getUserPetMerged() {
		return userPetMerged;
	}

	public Fightable getShenwuable() {
		return shenwuable;
	}

	public Fightable getFriendlyAdded() {
		return friendlyAdded;
	}

	public Fightable getRidHorsesable() {
		return ridHorsesable;
	}
	
}
