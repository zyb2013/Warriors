package com.yayo.warriors.module.horse.vo;

import java.io.Serializable;
import com.yayo.warriors.module.horse.entity.Horse;
/**
 * 
 * @author liuyuhua
 */
public class HorseVo implements Serializable{
	private static final long serialVersionUID = 1263812195107294940L;
	
	/** 当前坐骑等级*/
	private int level;
	
	/** 当前坐骑经验*/
	private int exp;
	
	/** 是否乘骑 true乘骑,false没有乘骑*/
	private boolean riding;
	
	/* 坐骑自定义元宝幻化次数,累计*/
	/** 总共增加的经验值*/
	private int totleupExp;
	/** 小暴次数*/
	private int minRateCount;
	/** 大暴次数*/
	private int maxRateCount;
	
	
	/**
	 * 构造方法
	 * @param horse 坐骑对象
	 * @return {@link HorseVo} 坐骑传输对象
	 */
	public static HorseVo valueOf(Horse horse) {
		if(horse == null) {
			return null;
		}
		
		HorseVo vo = new HorseVo();
		vo.level = horse.getLevel();
		vo.exp = horse.getExp();
		vo.riding = horse.isRiding();
		return vo;
	}
	
	/**
	 * 构造方法
	 * @param level     坐骑等级
	 * @param exp       坐骑经验
	 * @param riding    坐骑是否骑乘
	 * @return {@link HorseVo} 
	 */
	public static HorseVo valueOf(int level,int exp,boolean riding){
		HorseVo vo = new HorseVo();
		vo.level = level;
		vo.exp = exp;
		vo.riding = riding;
		return vo;
	}
	
	/**
	 * "自定义元宝幻化次数" 构造方法
	 * @param horse        坐骑对象
	 * @param totleExp     总共增加的经验值
	 * @param minRateCount 小暴次数
	 * @param maxRateCount 大爆次数
	 * @return {@link HorseVo}
	 */
	public static HorseVo valueOf(Horse horse,int totleExp,int minRateCount,int maxRateCount){
		HorseVo vo = HorseVo.valueOf(horse);
		vo.totleupExp = totleExp;
		vo.minRateCount = minRateCount;
		vo.maxRateCount = maxRateCount;
		return vo;
	}

	//Getter and Setter...
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public boolean isRiding() {
		return riding;
	}

	public void setRiding(boolean riding) {
		this.riding = riding;
	}

	public int getTotleupExp() {
		return totleupExp;
	}

	public void setTotleupExp(int totleupExp) {
		this.totleupExp = totleupExp;
	}

	public int getMinRateCount() {
		return minRateCount;
	}

	public void setMinRateCount(int minRateCount) {
		this.minRateCount = minRateCount;
	}

	public int getMaxRateCount() {
		return maxRateCount;
	}

	public void setMaxRateCount(int maxRateCount) {
		this.maxRateCount = maxRateCount;
	}



}
