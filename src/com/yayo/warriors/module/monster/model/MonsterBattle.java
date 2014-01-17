 package com.yayo.warriors.module.monster.model;

import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.yayo.common.lock.IEntity;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.module.fight.type.FightCasting;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;

/**
 * 怪物战斗信息类
 * 
 * @author LIUYUHUA
 */
public class MonsterBattle implements IEntity<Long> {

	/** 怪物主键*/
	private long monsterId;
	
	/** 角色的HP */
	private int hp;
	
	/** 角色的MP */
	private int mp;
	
	/** 角色的HP最大值 */
	private int hpMax;
	
	/** 角色的MP最大值 */
	private int mpMax;
	
	/** 命中 */
	private int hit;

	/** 躲闪 */
	private int dodge;

	/** 怪物等级 */
	private int level;

	/** 怪物的格挡 */
	private int block;

	/** 怪物的穿透 */
	private int pierce;

	/** 怪物的经验 */
	private int monsterExp;

	/** 怪物的急速 */
	private int rapidly;

	/** 怪物的力量 */
	private int strength;

	/** 怪物的敏捷 */
	private int dexerity;

	/** 怪物的坚韧 */
	private int ductility;
	
	/** 怪物的智力 */
	private int intellect;
	
	/** 怪物的移动速度 */
	private int moveSpeed;
	
	/** 怪物的体力 */
	private int constitution;
	
	/** 怪物的精神 */
	private int spirituality;
	
	/** 怪物的法术攻击 */
	private int theurgyAttack;
	
	/** 怪物的法术防御 */
	private int theurgyDefense;
	
	/** 怪物的法术暴击 */
	private int theurgyCritical;
	
	/** 怪物的物理攻击 */
	private int physicalAttack;
	
	/** 怪物的物理防御 */
	private int physicalDefense;
	
	/** 怪物的物理暴击 */
	private int physicalCritical;

	/** 怪物的定身抗性 */
	private int immobilizeDefense;
	
	/** 怪物的战斗属性 */
	private MonsterFightConfig monsterFight = null;

	/** 技能战斗属性对象 */
	private Fightable skillFightables = new Fightable();

	/** 刷新状态 */
	private transient volatile int flushable = Flushable.FLUSHABLE_NORMAL;
	
	/** 伤害信息记录*/
	private ConcurrentLinkedHashMap<ISpire, HurtInfo> hurtInfoMap = new ConcurrentLinkedHashMap.Builder<ISpire, HurtInfo>().maximumWeightedCapacity(20).build();
	
	
	public Long getIdentity() {
		return this.monsterId;
	}

	/**
	 * 构造函数
	 * @param monsterId   怪物的ID
	 * @param config      怪物的基础配置
	 * @return
	 */
	public static MonsterBattle valueOf(long monsterId, MonsterFightConfig monsterFight){
		MonsterBattle battle = new MonsterBattle();
		battle.monsterId = monsterId;
		battle.monsterFight = monsterFight;
		battle.flushable = Flushable.FLUSHABLE_LEVEL_UP;
		battle.skillFightables.putAll(monsterFight.getSkillInfoMap());
		return battle;
	}
	
	public void increaseHp(int addHp) {
		this.setHp(this.getHp() + addHp);
	}

	public void increaseMp(int addMp) {
		this.setMp(this.getMp() + addMp);
	}

	public void decreaseHp(int addHp) {
		this.setHp(Math.max(0, this.getHp() - addHp));
	}
	
	public void decreaseMp(int addMp) {
		this.setMp(Math.max(0, this.getMp() - addMp));
	}
	
	public int getLevel() {
		return level;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = Math.min(this.hpMax, hp);
	}

	public int getMp() {
		return mp;
	}

	public void setMp(int mp) {
		this.mp = Math.min(this.mpMax, mp);
	}

	public int getHpMax() {
		return hpMax;
	}

	public void setHpMax(int hpMax) {
		this.hpMax = hpMax;
	}

	public int getMpMax() {
		return mpMax;
	}

	public void setMpMax(int mpMax) {
		this.mpMax = mpMax;
	}

	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
	}

	public int getDodge() {
		return dodge;
	}

	public void setDodge(int dodge) {
		this.dodge = dodge;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public int getPierce() {
		return pierce;
	}

	public void setPierce(int pierce) {
		this.pierce = pierce;
	}

	public int getMonsterExp() {
		return monsterExp;
	}

	public void setMonsterExp(int monsterExp) {
		this.monsterExp = monsterExp;
	}

	public int getRapidly() {
		return rapidly;
	}

	public void setRapidly(int rapidly) {
		this.rapidly = rapidly;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getDexerity() {
		return dexerity;
	}

	public void setDexerity(int dexerity) {
		this.dexerity = dexerity;
	}

	public int getDuctility() {
		return ductility;
	}

	public void setDuctility(int ductility) {
		this.ductility = ductility;
	}

	public int getIntellect() {
		return intellect;
	}

	public void setIntellect(int intellect) {
		this.intellect = intellect;
	}

	public int getMoveSpeed() {
		return moveSpeed;
	}

	public void setMoveSpeed(int moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public int getConstitution() {
		return constitution;
	}

	public void setConstitution(int constitution) {
		this.constitution = constitution;
	}

	public int getSpirituality() {
		return spirituality;
	}

	public void setSpirituality(int spirituality) {
		this.spirituality = spirituality;
	}

	public int getTheurgyAttack() {
		return theurgyAttack;
	}

	public void setTheurgyAttack(int theurgyAttack) {
		this.theurgyAttack = theurgyAttack;
	}

	public int getTheurgyDefense() {
		return theurgyDefense;
	}

	public void setTheurgyDefense(int theurgyDefense) {
		this.theurgyDefense = theurgyDefense;
	}

	public int getTheurgyCritical() {
		return theurgyCritical;
	}

	public void setTheurgyCritical(int theurgyCritical) {
		this.theurgyCritical = theurgyCritical;
	}

	public int getPhysicalAttack() {
		return physicalAttack;
	}

	public void setPhysicalAttack(int physicalAttack) {
		this.physicalAttack = physicalAttack;
	}

	public int getPhysicalDefense() {
		return physicalDefense;
	}

	public void setPhysicalDefense(int physicalDefense) {
		this.physicalDefense = physicalDefense;
	}

	public int getPhysicalCritical() {
		return physicalCritical;
	}

	public void setPhysicalCritical(int physicalCritical) {
		this.physicalCritical = physicalCritical;
	}

	public int getFlushable() {
		return flushable;
	}

	public void setFlushable(int flushable) {
		this.flushable = flushable;
	}

	public ConcurrentLinkedHashMap<ISpire, HurtInfo> getHurtInfoMap() {
		return hurtInfoMap;
	}

	public void setHurtInfoMap(ConcurrentLinkedHashMap<ISpire, HurtInfo> hurtInfoMap) {
		this.hurtInfoMap = hurtInfoMap;
	}

	public void setMonsterId(long monsterId) {
		this.monsterId = monsterId;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setMonsterFight(MonsterFightConfig monsterFight) {
		this.monsterFight = monsterFight;
	}

	public void setSkillFightables(Fightable skillFightables) {
		this.skillFightables = skillFightables;
	}

	/**
	 * 是否死亡
	 * @return true 死亡  false 没有死亡
	 */
	public boolean isDead(){
		return this.hp <= 0;
	}
	
	// Getter and Setter...
	public Long getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(Long monsterId) {
		this.monsterId = monsterId;
	}
 
	public int getImmobilizeDefense() {
		return immobilizeDefense;
	}

	public void setImmobilizeDefense(int immobilizeDefense) {
		this.immobilizeDefense = immobilizeDefense;
	}

	/**
	 * 取得怪物的移动速度
	 * @return
	 */
	public double getMoveSpeedServerData() {
		return Tools.divideAndRoundDown(moveSpeed, AttributeKeys.RATE_BASE, 3);
	}

	public Fightable getSkillFightables() {
		return skillFightables;
	}
	
	public MonsterFightConfig getMonsterFight() {
		return monsterFight;
	}

	public int getSkillLevel(int skillId) {
		return this.skillFightables.getAttribute(skillId);
	}
	
	public boolean hasSkill(int skillId) {
		return this.skillFightables.containsKey(skillId);
	}

	public FightCasting getFightCasting() {
		return FightCasting.values()[ monsterFight.getFightCaseing() ];
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (monsterId ^ (monsterId >>> 32));
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		MonsterBattle other = (MonsterBattle) obj;
		return monsterId == other.monsterId;
	}

	
	public String toString() {
		return "MonsterBattle [monsterId=" + monsterId + ", hp=" + hp + ", mp=" + mp + ", hpMax="
				+ hpMax + ", mpMax=" + mpMax + ", hit=" + hit + ", dodge=" + dodge + ", level="
				+ level + ", block=" + block + ", pierce=" + pierce + ", monsterExp=" + monsterExp
				+ ", rapidly=" + rapidly + ", strength=" + strength + ", dexerity=" + dexerity
				+ ", ductility=" + ductility + ", intellect=" + intellect + ", moveSpeed="
				+ moveSpeed + ", constitution=" + constitution + ", spirituality=" + spirituality
				+ ", theurgyAttack=" + theurgyAttack + ", theurgyDefense=" + theurgyDefense
				+ ", theurgyCritical=" + theurgyCritical + ", physicalAttack=" + physicalAttack
				+ ", physicalDefense=" + physicalDefense + ", physicalCritical=" + physicalCritical
				+ ", monsterFight=" + monsterFight + ", skillFightables=" + skillFightables
				+ ", hurtInfoMap=" + hurtInfoMap + "]";
	}

	/**
	 * 添加被砍信息(移到monsterdomain中)
	 * @param playerId 	玩家编号
	 * @param hurt		伤害值
	 * @param currentHP	当前HP
	 * @param skillId	使用技能
	 */
	public void addFightInfo(ISpire spire, int hurt, int skillId) {
		HurtInfo info = hurtInfoMap.get(spire);
		if(info == null){
			info = new HurtInfo(spire, 0);
			hurtInfoMap.putIfAbsent(spire, info);
			hurtInfoMap.get(spire);
		}
		info.addInfo( Math.abs(hurt), skillId);
	}

	/**
	 * 忘记所有仇人
	 */
	public void forgetAllEnemy(){
		this.hurtInfoMap.clear() ;
	}
	
	/**
	 * 取得怪物的仇恨信息
	 * @return
	 */
	public Map<ISpire, HurtInfo> getHurtInfo(){
		return this.hurtInfoMap ;
	}	
	
	/**
	 * 当前怪物剩余HP百分比
	 * @return
	 */
	public int getMonsterHpPercent(){
		return (int)((this.getHp() / (double)this.getHpMax()) * 100);
	}

	/**
	 * 复活
	 */
	public void resurrection() {
		this.forgetAllEnemy();
		flushable = Flushable.FLUSHABLE_LEVEL_UP;
	}
	
	/**
	 * 移除伤害信息
	 * @param unitId
	 */
	public void removeHurtInfo(ISpire spire){
		if(hurtInfoMap.size() > 0 && spire != null){
			hurtInfoMap.remove(spire);
		}
	}
	
	//---------------------------------debuffer相关接口----------------------------------------
	/**
	 * 修改生命值
	 * + num
	 * - num
	 * @param num
	 * @return 修改后的生命值
	 */
	public int alterHp(int num){
		this.setHp(Math.max(0, this.getHp() + num));
		return this.getHp() ;
	}
	
	/**
	 * 更新刷新状态
	 * 
	 * @param flushable
	 */
	public void updateFlushable(int flushable) {
		if(flushable == Flushable.FLUSHABLE_NOT) {
			this.flushable = flushable;
		} else if(flushable >= this.flushable) {
			this.flushable = flushable;
		}
	}
	
	/**
	 * 需要刷新的状态
	 * @return
	 */
	public boolean isFlushable() {
		return this.flushable != Flushable.FLUSHABLE_NOT;
	}
	
	public boolean isFullHpMpFlushable() {
		return this.flushable == Flushable.FLUSHABLE_LEVEL_UP;
	}
	
	/**
	 * 更新战斗属性列表 
	 * 
	 * @param fightable
	 */
	public void updateFightableAttributes(Fightable fightable) {
		for (Entry<Object, Integer> entry : fightable.entrySet()) {
			Integer attribute = (Integer) entry.getKey();
			Integer attrValue = (Integer) entry.getValue();
			if(attribute != null && attrValue != null) {
				setAttribute(attribute, attrValue);
			}
		}
	}

	/**
	 * 更新战斗属性列表 
	 * 
	 * @param attribute 	参数属性
	 */
	public int getAttribute(int attribute) {
		switch (attribute) {
			case AttributeKeys.HP:					return this.getHp();					
			case AttributeKeys.MP:					return this.getMp();					
			case AttributeKeys.HIT:					return this.getHit();					
			case AttributeKeys.LEVEL:				return this.getLevel();				
			case AttributeKeys.DODGE:				return this.getDodge();				
			case AttributeKeys.BLOCK:				return this.getBlock();				
			case AttributeKeys.HP_MAX:				return this.getHpMax();				
			case AttributeKeys.MP_MAX:				return this.getMpMax();				
			case AttributeKeys.PIERCE:				return this.getPierce();				
			case AttributeKeys.RAPIDLY:				return this.getRapidly();				
			case AttributeKeys.STRENGTH:			return this.getStrength();			
			case AttributeKeys.DEXERITY:			return this.getDexerity();			
			case AttributeKeys.DUCTILITY:			return this.getDuctility();			
			case AttributeKeys.INTELLECT:			return this.getIntellect();			
			case AttributeKeys.MOVE_SPEED:			return this.getMoveSpeed();			
			case AttributeKeys.CONSTITUTION:		return this.getConstitution();		
			case AttributeKeys.SPIRITUALITY:		return this.getSpirituality();		
			case AttributeKeys.THEURGY_ATTACK:		return this.getTheurgyAttack();		
			case AttributeKeys.THEURGY_DEFENSE:		return this.getTheurgyDefense();		
			case AttributeKeys.PHYSICAL_ATTACK:		return this.getPhysicalAttack();		
			case AttributeKeys.THEURGY_CRITICAL:	return this.getTheurgyCritical();		
			case AttributeKeys.PHYSICAL_DEFENSE:	return this.getPhysicalDefense();		
			case AttributeKeys.PHYSICAL_CRITICAL:	return this.getPhysicalCritical();	
			case AttributeKeys.IMMOBILIZE_DEFENSE:	return this.getImmobilizeDefense();	
		}
		return 0;
	}

	/**
	 * 更新战斗属性列表 
	 * 
	 * @param attribute 	参数属性
	 */
	public void setAttribute(int attribute, int attrValue) {
		switch (attribute) {
			case AttributeKeys.HP:					this.setHp(attrValue);					break;
			case AttributeKeys.MP:					this.setMp(attrValue);					break;
			case AttributeKeys.HIT:					this.setHit(attrValue);					break;
			case AttributeKeys.LEVEL:				this.setLevel(attrValue);				break;
			case AttributeKeys.DODGE:				this.setDodge(attrValue);				break;
			case AttributeKeys.BLOCK:				this.setBlock(attrValue);				break;
			case AttributeKeys.HP_MAX:				this.setHpMax(attrValue);				break;
			case AttributeKeys.MP_MAX:				this.setMpMax(attrValue);				break;
			case AttributeKeys.PIERCE:				this.setPierce(attrValue);				break;
			case AttributeKeys.RAPIDLY:				this.setRapidly(attrValue);				break;
			case AttributeKeys.STRENGTH:			this.setStrength(attrValue);			break;
			case AttributeKeys.DEXERITY:			this.setDexerity(attrValue);			break;
			case AttributeKeys.DUCTILITY:			this.setDuctility(attrValue);			break;
			case AttributeKeys.INTELLECT:			this.setIntellect(attrValue);			break;
			case AttributeKeys.MOVE_SPEED:			this.setMoveSpeed(attrValue);			break;
			case AttributeKeys.CONSTITUTION:		this.setConstitution(attrValue);		break;
			case AttributeKeys.SPIRITUALITY:		this.setSpirituality(attrValue);		break;
			case AttributeKeys.THEURGY_ATTACK:		this.setTheurgyAttack(attrValue);		break;
			case AttributeKeys.THEURGY_DEFENSE:		this.setTheurgyDefense(attrValue);		break;
			case AttributeKeys.PHYSICAL_ATTACK:		this.setPhysicalAttack(attrValue);		break;
			case AttributeKeys.THEURGY_CRITICAL:	this.setTheurgyCritical(attrValue);		break;
			case AttributeKeys.PHYSICAL_DEFENSE:	this.setPhysicalDefense(attrValue);		break;
			case AttributeKeys.PHYSICAL_CRITICAL:	this.setPhysicalCritical(attrValue);	break;
			case AttributeKeys.IMMOBILIZE_DEFENSE:	this.setImmobilizeDefense(attrValue);	break;
		}
	}

	/**
	 * 更新战斗属性列表 
	 * 
	 * @return {@link Fightable}	战斗属性集合
	 */
	public Fightable getAttributes() {
		Fightable fightable = new Fightable();
		fightable.set(AttributeKeys.HP, this.hp);					
		fightable.set(AttributeKeys.MP, this.mp);					
		fightable.set(AttributeKeys.HIT, this.hit);					
		fightable.set(AttributeKeys.LEVEL, this.level);				
		fightable.set(AttributeKeys.DODGE, this.dodge);				
		fightable.set(AttributeKeys.BLOCK, this.block);				
		fightable.set(AttributeKeys.HP_MAX, this.hpMax);				
		fightable.set(AttributeKeys.MP_MAX, this.mpMax);				
		fightable.set(AttributeKeys.PIERCE, this.pierce);				
		fightable.set(AttributeKeys.RAPIDLY, this.rapidly);				
		fightable.set(AttributeKeys.STRENGTH, this.strength);			
		fightable.set(AttributeKeys.DEXERITY, this.dexerity);			
		fightable.set(AttributeKeys.DUCTILITY, this.ductility);			
		fightable.set(AttributeKeys.INTELLECT, this.intellect);			
		fightable.set(AttributeKeys.MOVE_SPEED, this.moveSpeed);			
		fightable.set(AttributeKeys.CONSTITUTION, this.constitution);		
		fightable.set(AttributeKeys.SPIRITUALITY, this.spirituality);		
		fightable.set(AttributeKeys.THEURGY_ATTACK, this.theurgyAttack);		
		fightable.set(AttributeKeys.THEURGY_DEFENSE, this.theurgyDefense);		
		fightable.set(AttributeKeys.PHYSICAL_ATTACK, this.physicalAttack);		
		fightable.set(AttributeKeys.THEURGY_CRITICAL, this.theurgyCritical);		
		fightable.set(AttributeKeys.PHYSICAL_DEFENSE, this.physicalDefense);		
		fightable.set(AttributeKeys.PHYSICAL_CRITICAL, this.physicalCritical);	
		fightable.set(AttributeKeys.IMMOBILIZE_DEFENSE, this.immobilizeDefense);			
		return fightable;
	}
	
}
