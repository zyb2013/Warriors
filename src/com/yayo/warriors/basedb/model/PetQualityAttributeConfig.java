package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.module.pet.types.PetJob;

/**
 * 家将基础属性系数
 * 通过家将初始化获得的品质来乘积
 * @author liuyuhua
 */
@Resource
public class PetQualityAttributeConfig {

	/** 类型 {@link PetJob}**/
	@Id
	private int id;
	
	/** 生命系数*/
	private float hp;

	/** 内力系数*/
	private float mp;
	
	/** 外功系数*/
	private float physicalattack;
	
	/** 内力系数*/
	private float theurgyattack;
	
	/** 外防系数*/
	private float physicaldefense;
	
	/** 内防系数*/
	private float theurgydefense;
	
	/** 外暴系数*/
	private float physicalcritical;
	
	/** 内暴系数*/
	private float theurgycritical;
	
	/** 命中系数*/
	private float hit;
	
	/** 闪避系数*/
	private float dodge;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getHp() {
		return hp;
	}

	public void setHp(float hp) {
		this.hp = hp;
	}

	public float getMp() {
		return mp;
	}

	public void setMp(float mp) {
		this.mp = mp;
	}

	public float getPhysicalattack() {
		return physicalattack;
	}

	public void setPhysicalattack(float physicalattack) {
		this.physicalattack = physicalattack;
	}

	public float getTheurgyattack() {
		return theurgyattack;
	}

	public void setTheurgyattack(float theurgyattack) {
		this.theurgyattack = theurgyattack;
	}

	public float getPhysicaldefense() {
		return physicaldefense;
	}

	public void setPhysicaldefense(float physicaldefense) {
		this.physicaldefense = physicaldefense;
	}

	public float getTheurgydefense() {
		return theurgydefense;
	}

	public void setTheurgydefense(float theurgydefense) {
		this.theurgydefense = theurgydefense;
	}

	public float getPhysicalcritical() {
		return physicalcritical;
	}

	public void setPhysicalcritical(float physicalcritical) {
		this.physicalcritical = physicalcritical;
	}

	public float getTheurgycritical() {
		return theurgycritical;
	}

	public void setTheurgycritical(float theurgycritical) {
		this.theurgycritical = theurgycritical;
	}

	public float getHit() {
		return hit;
	}

	public void setHit(float hit) {
		this.hit = hit;
	}

	public float getDodge() {
		return dodge;
	}

	public void setDodge(float dodge) {
		this.dodge = dodge;
	}

	@Override
	public String toString() {
		return "PetQualityAttributeConfig [id=" + id + ", hp=" + hp + ", mp="
				+ mp + ", physicalattack=" + physicalattack
				+ ", theurgyattack=" + theurgyattack + ", physicaldefense="
				+ physicaldefense + ", theurgydefense=" + theurgydefense
				+ ", physicalcritical=" + physicalcritical
				+ ", theurgycritical=" + theurgycritical + ", hit=" + hit
				+ ", dodge=" + dodge + "]";
	}
}
