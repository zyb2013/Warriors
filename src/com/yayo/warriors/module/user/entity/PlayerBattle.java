package com.yayo.warriors.module.user.entity;

import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.user.model.ConcurrentFightable;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.PlayerStatus;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.FightMode;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.Job;

/**
 * 玩家战斗属性
 * 
 * @author liuyuhua
 */
@Entity
@Table(name = "playerBattle")
public class PlayerBattle extends BaseModel<Long> {
	private static final long serialVersionUID = -3612775679150525872L;

	@Id
	@Column(name = "playerId")
	private Long id;

	// ---------------------- 固有属性 --------------------------------
	/** 角色的HP */
	private int hp;

	/** 角色的MP */
	private int mp;

	/** 角色的真气 . */
	private int gas;

	/** 角色的经验. */
	private long exp;

	/** 当前的等级. */
	private int level;

	/** HP 便携包. */
	private int hpBag;

	/** MP 便携包. */
	private int mpBag;
	
	/** 家将 HP 便携包. */
	private int petHpBag;
	
	/** 角色的职业 */
	@Enumerated
	private Job job;

	/** 角色的模式 */
	private FightMode mode = FightMode.PEACE;
	
	//--------------------------一级属性 - 用于奖励固定增加-----------------------------------
	/** 力量 */
	private int strength;

	/** 敏捷 */
	private int dexerity;

	/** 智力 */
	private int intellect;

	/** 体力 */
	private int constitution;

	/** 精神 */
	private int spirituality;
	
	//--------------------------二级属性 - 用于奖励固定增加-----------------------------------------------------
	/** 命中率 */
	private int hit;
	
	/** 闪避值.*/
	private int dodge;
	
	/** 移动速度 */
	private int moveSpeed;
	
	/** 法术攻击 */
	private int theurgyAttack;
	
	/** 法术防御 */
	private int theurgyDefense;
	
	/** 法术暴击 */
	private int theurgyCritical;
	
	/** 物理攻击 */
	private int physicalAttack;
	
	/** 物理防御 */
	private int physicalDefense;
	
	/** 物理暴击 */
	private int physicalCritical;
	
	/** HP(永久加成)*/
	private int addHpMax;
	
	/** MP(永久加成)*/
	private int addMpMax;

	// ----------------------  不需要入库的信息 -----------------------------
	
	/** HP最大值 */
	@Transient
	private transient volatile int hpMax;
	
	/** 最大经验值 */
	@Transient
	private transient volatile long expMax;
	
	/** MP最大值 */
	@Transient
	private transient volatile int mpMax;
	
	/** Gas最大值 */
	@Transient
	private transient volatile int gasMax;
	
	/** 刷新状态 */
	@Transient
	private transient volatile int flushable = Flushable.FLUSHABLE_NORMAL;
	/** 角色的状态对象 */
	@Transient
	private transient PlayerStatus playerStatus = new PlayerStatus();
	/** 角色的属性. */
	@Transient
	private transient ConcurrentFightable attributes = new ConcurrentFightable();

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
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

	public int getIntellect() {
		return intellect;
	}

	public void setIntellect(int intellect) {
		this.intellect = intellect;
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

	public int getMoveSpeed() {
		return moveSpeed;
	}

	public void setMoveSpeed(int moveSpeed) {
		this.moveSpeed = moveSpeed;
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

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = this.level >= PlayerRule.getMaxPlayerLevel() ? 0 : exp;
	}

	public void increaseExp(long exp) {
		long canAddExp = Math.max(0, Long.MAX_VALUE - this.exp);
		setExp(this.exp + Math.min(exp, canAddExp));
	}

	public long getExpMax() {
		return expMax;
	}

	public void setExpMax(long expMax) {
		this.expMax = expMax;
	}

	public int getGas() {
		return gas;
	}

	public void setGas(int gas) {
		this.setAttribute(AttributeKeys.GAS, gas);
	}

	public void increaseGas(int gas) {
		setGas(this.gas + gas);
	}

	public void decreaseGas(int gas) {
		setGas(this.gas - gas);
	}

	public int getHp() {
		return hp;
	}

	public int getHpBag() {
		return hpBag;
	}

	public void setHpBag(int hpBag) {
		this.setAttribute(HP_BAG, hpBag);
	}

	public void setHpMax(int hpMax) {
		this.setAttribute(HP_MAX, hpMax);
	}

	public void setMpMax(int mpMax) {
		this.setAttribute(MP_MAX, mpMax);
	}

	public void setGasMax(int gasMax) {
		this.setAttribute(GAS_MAX, gasMax);
	}

	public int getMpBag() {
		return mpBag;
	}

	public void setMpBag(int mpBag) {
		this.setAttribute(MP_BAG, mpBag);
	}

	public void increaseHpBag(int hpBag) {
		setHpBag(this.hp + gas);
	}
	
	public void increasePetHpBag(int petHpBag) {
		setHpBag(this.petHpBag + petHpBag);
	}

	public void decreaseHpBag(int hpBag) {
		setHpBag(this.hpBag - hpBag);
	}
	
	public void decreasePetHpBag(int petHpBag) {
		setPetHpBag(this.petHpBag - petHpBag);
	}

	public void increaseMpBag(int mpBag) {
		setMpBag(this.mpBag + mpBag);
	}

	public void decreaseMpBag(int mpBag) {
		setMpBag(this.mpBag - mpBag);
	}

	public void setHp(int hp) {
		this.setAttribute(AttributeKeys.HP, hp);
	}

	public void increaseHp(int hp) {
		this.setHp(this.hp + hp);
	}

	public void decreaseHp(int hp) {
		this.setHp(this.hp - hp);
	}

	public int getMp() {
		return mp;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public void setMp(int mp) {
		this.setAttribute(MP, mp);
	}

	public void increaseMp(int mp) {
		setMp(this.mp + mp);
	}

	public void decreaseMp(int mp) {
		setMp(this.mp - mp);
	}

	public int getLevel() {
		return level;
	}

	public FightMode getMode() {
		return mode;
	}

	public void setMode(FightMode mode) {
		this.mode = mode;
	}

	public boolean isDead() {
		return this.hp <= 0;
	}

	public int getHpMax() {
		return this.getAttribute(HP_MAX);
	}

	public int getMpMax() {
		return this.getAttribute(MP_MAX);
	}

	public int getGasMax() {
		return this.getAttribute(GAS_MAX);
	}

	/**
	 * 设置角色战斗属性
	 * 
	 * @param attrKey 		属性键值
	 * @param attrValue 	属性值
	 */
	public void setAttribute(int attrKey, int attrValue) {
		if (attrKey == LEVEL) {
			this.level = attrValue;
		} else if(attrKey == HP_BAG) {
			this.hpBag = attrValue;
		} else if(attrKey == MP_BAG) {
			this.mpBag = attrValue;
		} else if(attrKey == PET_HP_BAG) {
			this.petHpBag = attrValue;
		} else if (attrKey == HP_MAX) {
			this.hpMax = attrValue;
		} else if (attrKey == MP_MAX) {
			this.mpMax = attrValue;
		} else if (attrKey == GAS_MAX) {
			this.gasMax = attrValue;
		} else if (attrKey == HP) {
			this.hp = Math.min(attrValue, this.hpMax);
		} else if (attrKey == MP) {
			this.mp = Math.min(attrValue, this.mpMax);
		} else if (attrKey == GAS) {
			this.gas = Math.min(attrValue, this.gasMax);
		} else {
			this.attributes.set(attrKey, attrValue);
		}
	}

	public int getAttribute(int attrKey) {
		if (attrKey == LEVEL) {
			return this.level;
		} else if (attrKey == HP) {
			return this.hp;
		} else if (attrKey == MP) {
			return this.mp;
		} else if (attrKey == GAS) {
			return this.gas;
		} else if (attrKey == HP_MAX) {
			return this.hpMax;
		} else if (attrKey == MP_MAX) {
			return this.mpMax;
		} else if (attrKey == GAS_MAX) {
			return this.gasMax;
		} else if(attrKey == HP_BAG) {
			return this.hpBag;
		} else if(attrKey == MP_BAG) {
			return this.mpBag;
		} else if(attrKey == PET_HP_BAG) {
			return this.petHpBag;
		} else {
			return this.attributes.getAttribute(attrKey);
		}
	}
	
	public double getAttributeRate(int attributeKey) {
		return Tools.divideAndRoundDown(getAttribute(attributeKey), AttributeKeys.RATE_BASE, 3);
	}

	public Fightable getAndCopyAttributes() {
		Fightable fightable = new Fightable();
		fightable.addAll(attributes);
		fightable.set(HP, this.hp);
		fightable.set(MP, this.mp);
		fightable.set(GAS, this.gas);
		fightable.set(LEVEL, this.level);
		fightable.set(HP_MAX, this.hpMax);
		fightable.set(MP_MAX, this.mpMax);
		fightable.set(HP_BAG, this.hpBag);
		fightable.set(MP_BAG, this.mpBag);
		fightable.set(GAS_MAX, this.gasMax);
		fightable.set(PET_HP_BAG, this.petHpBag);
		return fightable;
	}

	public void increaseAttribute(int attributeKey, int attributeValue) {
		int value = getAttribute(attributeKey);
		int canAddValue = Math.max(0, Integer.MAX_VALUE - value);
		this.setAttribute(attributeKey, value + Math.min(attributeValue, canAddValue));
	}

	public void decreaseAttribute(int attributeKey, int attributeValue) {
		int value = getAttribute(attributeKey);
		int total = value - attributeValue;
		this.setAttribute(attributeKey, total);
	}

	public void increaseLevel(int addLevel) {
		this.setLevel(this.level + addLevel);
	}

	public void decreaseLevel(int addLevel) {
		this.setLevel(this.level - addLevel);
	}

	public PlayerStatus getPlayerStatus() {
		return playerStatus;
	}

	public void setPlayerStatus(PlayerStatus playerStatus) {
		this.playerStatus = playerStatus;
	}

	public int getFlushable() {
		return flushable;
	}

	public void setFlushable(int flushable) {
		if(flushable == Flushable.FLUSHABLE_NOT || flushable >= this.flushable) {
			this.flushable = flushable;
		}
	}

	public boolean isFullHPMPFlushable() {
		return this.flushable == Flushable.FLUSHABLE_LEVEL_UP;
	}

	public boolean isFlushable() {
		return this.flushable != Flushable.FLUSHABLE_NOT;
	}

	public void setLevel(int level) {
		this.setAttribute(AttributeKeys.LEVEL, level);
	}

	public void putAttributes(Map<Object, Integer> fightable, boolean clear) {
		if(clear) {
			this.attributes.clear();
		}
		
		if(fightable != null && !fightable.isEmpty()) {
			for (Entry<Object, Integer> entry : fightable.entrySet()) {
				Integer attrKey = (Integer) entry.getKey();
				Integer attrValue = entry.getValue();
				if(attrKey != null && attrValue != null) {
					this.setAttribute(attrKey, attrValue);
				}
			}
		}
	}
	
	public boolean isPeaceMode() {
		return this.mode == FightMode.PEACE;
	}
	
	/**
	 * 构建角色战斗对象
	 * 
	 * @param  playerId 			角色ID
	 * @return {@link PlayerBattle} 角色战斗对象
	 */
	public static PlayerBattle valueOf() {
		return new PlayerBattle();
	}

	public int getAddHpMax() {
		return addHpMax;
	}

	public void setAddHpMax(int addHpMax) {
		this.addHpMax = addHpMax;
	}

	public int getAddMpMax() {
		return addMpMax;
	}

	public void setAddMpMax(int addMpMax) {
		this.addMpMax = addMpMax;
	}

	public int getPetHpBag() {
		return this.getAttribute(AttributeKeys.PET_HP_BAG);
	}

	public void setPetHpBag(int petHpBag) {
		this.setAttribute(AttributeKeys.PET_HP_BAG, petHpBag);
	}
}
