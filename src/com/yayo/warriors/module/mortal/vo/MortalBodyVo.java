package com.yayo.warriors.module.mortal.vo;

import java.io.Serializable;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;

/**
 * 肉身VO
 * 
 * @author huachaoping
 */
public class MortalBodyVo implements Serializable{

	private static final long serialVersionUID = 6779847700337478549L;
	
	/** 以下为肉身等级(序号确定类型) */
	
	private int mortalBody0;
	private int mortalBody1;
	private int mortalBody2;
	private int mortalBody3;
	private int mortalBody4;
	private int mortalBody5;
	private int mortalBody6;
	private int mortalBody7;
	
	
	/** 加成的属性值 */
	private Object[] values = new Object[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	/** 肉身加成属性KEY */
	private Object[] attributes = new Object[] {PHYSICAL_ATTACK, PHYSICAL_DEFENSE, PHYSICAL_CRITICAL,
												THEURGY_ATTACK, THEURGY_DEFENSE, THEURGY_CRITICAL,
												HIT, DODGE, HP_MAX, MP_MAX };

	public int getMortalBody0() {
		return mortalBody0;
	}

	public void setMortalBody0(int mortalBody0) {
		this.mortalBody0 = mortalBody0;
	}

	public int getMortalBody1() {
		return mortalBody1;
	}

	public void setMortalBody1(int mortalBody1) {
		this.mortalBody1 = mortalBody1;
	}

	public int getMortalBody2() {
		return mortalBody2;
	}

	public void setMortalBody2(int mortalBody2) {
		this.mortalBody2 = mortalBody2;
	}

	public int getMortalBody3() {
		return mortalBody3;
	}

	public void setMortalBody3(int mortalBody3) {
		this.mortalBody3 = mortalBody3;
	}

	public int getMortalBody4() {
		return mortalBody4;
	}

	public void setMortalBody4(int mortalBody4) {
		this.mortalBody4 = mortalBody4;
	}

	public int getMortalBody5() {
		return mortalBody5;
	}

	public void setMortalBody5(int mortalBody5) {
		this.mortalBody5 = mortalBody5;
	}

	public int getMortalBody6() {
		return mortalBody6;
	}

	public void setMortalBody6(int mortalBody6) {
		this.mortalBody6 = mortalBody6;
	}

	public int getMortalBody7() {
		return mortalBody7;
	}

	public void setMortalBody7(int mortalBody7) {
		this.mortalBody7 = mortalBody7;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}

	public Object[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Object[] attributes) {
		this.attributes = attributes;
	}
	

}
