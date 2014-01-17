package com.yayo.warriors.module.meridian.vo;

import java.io.Serializable;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;


/**
 * 经脉数值显示VO   
 * @author huachaoping
 * 
 */
public class AttributeVo implements Serializable{

	private static final long serialVersionUID = -4473974111440355954L;

	/** 阳维脉通点数量*/
	private int smallMeridian1;		
	/** 阴维脉通点数量*/
	private int smallMeridian2;		
	/** 阳跷脉通点数量*/
	private int smallMeridian3;		
	/** 阴跷脉通点数量*/
	private int smallMeridian4;		
	/** 冲脉通点数量*/
	private int smallMeridian5;		
	/** 带脉通点数量*/
	private int smallMeridian6;		
	/** 任脉通点数量*/
	private int smallMeridian7;		
	/** 督脉通点数量*/
	private int smallMeridian8;
	
	// -------- 以下是大周天 -------
	
	private int bigMeridian1;
	private int bigMeridian2;
	private int bigMeridian3;
	private int bigMeridian4;
	private int bigMeridian5;
	private int bigMeridian6;
	private int bigMeridian7;	
	private int bigMeridian8;
	
	/** 是否通过小周天 */
	private boolean passStage;
	
	/** 剩余次数 */
	private int laveTimes;
	
	/** 获得经验 */
	private int requiredExp;
	
	/** 加成的属性值 */
	private Object[] values = new Object[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	/** 经脉加成的属性Key */
	private Object[] attributes = new Object[] {HIT, DODGE, THEURGY_ATTACK, THEURGY_DEFENSE, THEURGY_CRITICAL, PHYSICAL_ATTACK, PHYSICAL_DEFENSE, PHYSICAL_CRITICAL, HP_MAX, MP_MAX};
	
	
	
	public int getSmallMeridian1() {
		return smallMeridian1;
	}
	public void setSmallMeridian1(int smallMeridian1) {
		this.smallMeridian1 = smallMeridian1;
	}
	public int getSmallMeridian2() {
		return smallMeridian2;
	}
	public void setSmallMeridian2(int smallMeridian2) {
		this.smallMeridian2 = smallMeridian2;
	}
	public int getSmallMeridian3() {
		return smallMeridian3;
	}
	public void setSmallMeridian3(int smallMeridian3) {
		this.smallMeridian3 = smallMeridian3;
	}
	public int getSmallMeridian4() {
		return smallMeridian4;
	}
	public void setSmallMeridian4(int smallMeridian4) {
		this.smallMeridian4 = smallMeridian4;
	}
	public int getSmallMeridian5() {
		return smallMeridian5;
	}
	public void setSmallMeridian5(int smallMeridian5) {
		this.smallMeridian5 = smallMeridian5;
	}
	public int getSmallMeridian6() {
		return smallMeridian6;
	}
	public void setSmallMeridian6(int smallMeridian6) {
		this.smallMeridian6 = smallMeridian6;
	}
	public int getSmallMeridian7() {
		return smallMeridian7;
	}
	public void setSmallMeridian7(int smallMeridian7) {
		this.smallMeridian7 = smallMeridian7;
	}
	public int getSmallMeridian8() {
		return smallMeridian8;
	}
	public void setSmallMeridian8(int smallMeridian8) {
		this.smallMeridian8 = smallMeridian8;
	}
	public int getBigMeridian1() {
		return bigMeridian1;
	}
	public void setBigMeridian1(int bigMeridian1) {
		this.bigMeridian1 = bigMeridian1;
	}
	public int getBigMeridian2() {
		return bigMeridian2;
	}
	public void setBigMeridian2(int bigMeridian2) {
		this.bigMeridian2 = bigMeridian2;
	}
	public int getBigMeridian3() {
		return bigMeridian3;
	}
	public void setBigMeridian3(int bigMeridian3) {
		this.bigMeridian3 = bigMeridian3;
	}
	public int getBigMeridian4() {
		return bigMeridian4;
	}
	public void setBigMeridian4(int bigMeridian4) {
		this.bigMeridian4 = bigMeridian4;
	}
	public int getBigMeridian5() {
		return bigMeridian5;
	}
	public void setBigMeridian5(int bigMeridian5) {
		this.bigMeridian5 = bigMeridian5;
	}
	public int getBigMeridian6() {
		return bigMeridian6;
	}
	public void setBigMeridian6(int bigMeridian6) {
		this.bigMeridian6 = bigMeridian6;
	}
	public int getBigMeridian7() {
		return bigMeridian7;
	}
	public void setBigMeridian7(int bigMeridian7) {
		this.bigMeridian7 = bigMeridian7;
	}
	public int getBigMeridian8() {
		return bigMeridian8;
	}
	public void setBigMeridian8(int bigMeridian8) {
		this.bigMeridian8 = bigMeridian8;
	}
	
	
	public Object[] getAttributes() {
		return attributes;
	}
	public void setAttributes(Object[] attributes) {
		this.attributes = attributes;
	}
	public Object[] getValues() {
		return values;
	}
	public void setValues(Object[] values) {
		this.values = values;
	}
	
	
	public int getLaveTimes() {
		return laveTimes;
	}
	public void setLaveTimes(int laveTimes) {
		this.laveTimes = laveTimes;
	}
	public int getRequiredExp() {
		return requiredExp;
	}
	public void setRequiredExp(int requiredExp) {
		this.requiredExp = requiredExp;
	}
	public boolean isPassStage() {
		return passStage;
	}
	public void setPassStage(boolean passStage) {
		this.passStage = passStage;
	}
	
}