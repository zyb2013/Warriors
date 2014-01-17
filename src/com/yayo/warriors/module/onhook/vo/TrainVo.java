package com.yayo.warriors.module.onhook.vo;

import java.io.Serializable;


/**
 * 闭关视图对象
 * @author huachaoping 
 */
public class TrainVo implements Serializable{
	
	private static final long serialVersionUID = -5491760223249113300L;

	/** 当前经验储量*/
	private int curExp;
	
	/** 当前真气储量*/
	private int curGas;
	
	/** 已闭关时间*/
	private long lagTime;
	
	/**
	 * 构造函数
	 * @param curExp
	 * @param curGas
	 * @param lagTime
	 * @return
	 */
	public static TrainVo valueOf(int curExp, int curGas, long lagTime) {
		TrainVo closedVo = new TrainVo();
		closedVo.curExp  = curExp;
		closedVo.curGas  = curGas;
		closedVo.lagTime = lagTime;
		return closedVo;	
	}
	
	public int getCurExp() {
		return curExp;
	}

	public void setCurExp(int curExp) {
		this.curExp = curExp;
	}

	public int getCurGas() {
		return curGas;
	}

	public void setCurGas(int curGas) {
		this.curGas = curGas;
	}

	public long getLagTime() {
		return lagTime;
	}

	public void setLagTime(long lagTime) {
		this.lagTime = lagTime;
	}
	
	
}
