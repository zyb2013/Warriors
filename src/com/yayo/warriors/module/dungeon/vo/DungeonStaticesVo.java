package com.yayo.warriors.module.dungeon.vo;

import java.io.Serializable;

/**
 * 副本统计 
 * @author liuyuhua
 */
public class DungeonStaticesVo implements Serializable{
	private static final long serialVersionUID = -8543569658305179280L;

	/** 回合数*/
	private int round;
	
	/** 剩余怪物总数*/
	private int number;
	
	/** 本回合怪物总数*/
	private int total;
	
	/** 本回合可以获得的怪物经验*/
	private int exp;
	
	/**
	 * 构造方法
	 * @param round     回合数
	 * @param number    剩余怪物总数
	 * @param total     本回合怪物总数
	 * @param exp       本回合可以获得的怪物经验
	 * @return {@link DungeonStaticesVo} 副本统计 对象
	 */
	public static DungeonStaticesVo valueOf(int round,int number,int total,int exp){
		DungeonStaticesVo vo = new DungeonStaticesVo();
		vo.exp = exp;
		vo.total = total;
		vo.round = round;
		vo.number = number;
		return vo;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	@Override
	public String toString() {
		return "DungeonStaticesVo [round=" + round + ", number=" + number
				+ ", total=" + total + ", exp=" + exp + "]";
	}
}
