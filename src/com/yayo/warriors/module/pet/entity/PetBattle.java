package com.yayo.warriors.module.pet.entity;

import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.pet.rule.PetRule;
import com.yayo.warriors.module.pet.types.PetJob;
import com.yayo.warriors.module.user.model.ConcurrentFightable;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;


@Entity
@Table(name = "userPetBattle")
public class PetBattle extends BaseModel<Long> {
	private static final long serialVersionUID = -6461184138587551178L;
	
	@Id
	@Column(name = "petId")
	private Long id;
	
	private int hp;

	private long exp;

	private int level;
	
	private int quality;
	
	private int savvy;
	
	@Enumerated
	private PetJob job;
	
	private int mergedLevel =  0;
	
	private long mergedTime = 0;
	
	private int mergedBlessPercent = 0;
	
	private int mergedBless = 0;
	
	private int fighting = 0;
	
	@Transient
	private transient volatile boolean checkingExp = false;
	@Transient
	private transient volatile ConcurrentFightable attributes = new ConcurrentFightable();
	@Transient
	private transient volatile int flushable = Flushable.FLUSHABLE_NORMAL;
	
	@Transient
	private transient volatile long backTime = 0;

	public static PetBattle valueOf(Long petId,PetJob job,int quality,int hp){
		PetBattle petBattle = new PetBattle();
		petBattle.level   = PetRule.INIT_DEFAULT_LEVEL;
		petBattle.id      = petId;
		petBattle.job     = job;
		petBattle.quality = quality;
		petBattle.exp     = 0;
		petBattle.hp      = hp;
		return petBattle;
	}
	
	public boolean canFight(){
		long currentTime = System.currentTimeMillis();
		if((currentTime - this.backTime) >= 5000){
			return true;
		}
		return false;
	}
	
	
	public void goBack(){
		if(this.hp <= 0){
			this.hp = 1;
		}
		
		this.backTime = System.currentTimeMillis();
	}
	
	public void increaseHp(int hp) {
		this.hp += hp;
		this.hp = this.hp > this.getHpMax() ? this.getHpMax() : this.hp;
		this.setAttribute(AttributeKeys.HP, this.hp);
	}

	public void decreaseHp(int hp) {
		this.hp -= hp;
		this.hp = this.hp < 0 ? 0 : this.hp;
		this.setAttribute(AttributeKeys.HP, this.hp);
	}
	
	public void increaseExp(long exp) {
		this.exp += exp;
		this.checkingExp = true;
	}

	public void decreaseExp(long exp) {
		this.checkingExp = true;
		this.exp = Math.max(0, this.exp - exp);
	}
	
	public boolean isDeath() {
		return this.hp <= 0;
	}
	
	public int getHpMax() {
		return this.getAttribute(AttributeKeys.HP_MAX);
	}
	
	public void increaseLevel(int addLevel) {
		this.setLevel(this.level + addLevel);
	}

	public void decreaseLevel(int addLevel) {
		this.setLevel(this.level - addLevel);
	}
	
	public int getFlushable() {
		return flushable;
	}

	public void setFlushable(int flushable) {
		if(flushable == Flushable.FLUSHABLE_NOT) {
			this.flushable = flushable;
		} else if(flushable >= this.flushable) {
			this.flushable = flushable;
		}
	}

	public boolean isFullHPMPFlushable() {
		return this.flushable == Flushable.FLUSHABLE_LEVEL_UP;
	}

	public boolean isFlushable() {
		return this.flushable != Flushable.FLUSHABLE_NOT;
	}
	
	public boolean checkOverMerged(){
		long theTime = System.currentTimeMillis();
		if(theTime >= mergedTime){
			Date date = DateUtil.changeDateTime(new Date(theTime), 1, 6, 0, 0);
			mergedTime = date.getTime();
			mergedBlessPercent = 0;
			mergedBless = 0;
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public PetJob getJob() {
		return job;
	}


	public void setJob(PetJob job) {
		this.job = job;
	}
	
	public int getSavvy() {
		return savvy;
	}

	public void setSavvy(int savvy) {
		this.savvy = savvy;
	}

	public void setHp(int hp) {
		hp = hp > this.getHpMax() ? this.getHpMax() : hp;
		this.hp = hp;
		this.attributes.put(AttributeKeys.HP, this.hp);
	}
	
	public int getHp() {
		return hp;
	}

	public boolean isCheckingExp() {
		return this.checkingExp;
	}

	public void markCheckingExp() {
		this.checkingExp = true;
	}

	public void uncheckingExp() {
		this.checkingExp = false;
	}


	public long getExp() {
		return exp;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getMergedLevel() {
		return mergedLevel;
	}

	public void setMergedLevel(int mergedLevel) {
		this.mergedLevel = mergedLevel;
	}

	public long getMergedTime() {
		return mergedTime;
	}

	public void setMergedTime(long mergedTime) {
		this.mergedTime = mergedTime;
	}

	public int getMergedBlessPercent() {
		return mergedBlessPercent;
	}

	public void setMergedBlessPercent(int mergedBlessPercent) {
		this.mergedBlessPercent = mergedBlessPercent;
	}

	public int getMergedBless() {
		return mergedBless;
	}

	public void setMergedBless(int mergedBless) {
		this.mergedBless = mergedBless;
	}
	
	public int getFighting() {
		return fighting;
	}

	public void setFighting(int fighting) {
		this.fighting = fighting;
	}

	public int setAttribute(int attrKey, int attrValue) {
		int max = attrValue;
		if (attrKey == LEVEL) {
			this.level = attrValue;
		}else if (attrKey == HP) {
			max = getAttribute(HP_MAX);
			attrValue = hp = (attrValue > max ? max : attrValue);
		} 
		return this.attributes.set(attrKey, attrValue);
	}
	
	public int getAttribute(int attributeKey) {
		return this.attributes.getAttribute(attributeKey);
	}

	public ConcurrentFightable getAttributes() {
		return attributes;
	}

	public void setAttributes(ConcurrentFightable attributes) {
		this.attributes = attributes;
	}


	public boolean constains(int attrKey) {
		return this.attributes.containsKey(attrKey);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PetBattle other = (PetBattle) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
