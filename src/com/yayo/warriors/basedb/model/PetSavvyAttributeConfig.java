package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.module.pet.types.PetJob;

/**
 * 家将悟性属性(二级属性系数)
 * @author liuyuhua
 */
@Resource
public class PetSavvyAttributeConfig {
	
	/** 类型 {@link PetJob}**/
	@Id
	private int id;
	
	/** 生命成长*/
	private int hp;
	
	/** 内力成长*/
	private int mp;
	
	/** 物理攻击 */
	private int physicalattack;
	
	/** 内功成长*/
	private int theurgyattack;
	
	/** 外放成长 */
	private int physicaldefense;
	
	/** 内防成长 */
	private int theurgydefense;
	
	/** 外暴成长 */
	private int physicalcritical;
	
	/** 内暴成长 */
	private int theurgycritical;
	
	/** 命中率 */
	private int hit;
	
	/** 闪避值*/
	private int dodge;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getMp() {
		return mp;
	}

	public void setMp(int mp) {
		this.mp = mp;
	}

	public int getPhysicalattack() {
		return physicalattack;
	}

	public void setPhysicalattack(int physicalattack) {
		this.physicalattack = physicalattack;
	}

	public int getTheurgyattack() {
		return theurgyattack;
	}

	public void setTheurgyattack(int theurgyattack) {
		this.theurgyattack = theurgyattack;
	}

	public int getPhysicaldefense() {
		return physicaldefense;
	}

	public void setPhysicaldefense(int physicaldefense) {
		this.physicaldefense = physicaldefense;
	}

	public int getTheurgydefense() {
		return theurgydefense;
	}

	public void setTheurgydefense(int theurgydefense) {
		this.theurgydefense = theurgydefense;
	}

	public int getPhysicalcritical() {
		return physicalcritical;
	}

	public void setPhysicalcritical(int physicalcritical) {
		this.physicalcritical = physicalcritical;
	}

	public int getTheurgycritical() {
		return theurgycritical;
	}

	public void setTheurgycritical(int theurgycritical) {
		this.theurgycritical = theurgycritical;
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

	@Override
	public String toString() {
		return "PetSavvyAttributeConfig [id=" + id + ", hp=" + hp + ", mp="
				+ mp + ", physicalattack=" + physicalattack
				+ ", theurgyattack=" + theurgyattack + ", physicaldefense="
				+ physicaldefense + ", theurgydefense=" + theurgydefense
				+ ", physicalcritical=" + physicalcritical
				+ ", theurgycritical=" + theurgycritical + ", hit=" + hit
				+ ", dodge=" + dodge + "]";
	}
}
