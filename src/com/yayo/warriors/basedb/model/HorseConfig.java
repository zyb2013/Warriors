package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.type.IndexName;

/**
 * 坐骑配置
 * @author liuyuhua
 */
@Resource
public class HorseConfig {

	/** 坐骑等级 */
	@Id
	private int id;
	
	/** 名字*/
	private String name;
	
	/** 坐骑模型*/
	@Index(name=IndexName.HORSE_MODEL , order = 0)
	private int model;
	
	/** 生命*/
	private int hp;
	
	/** 内力*/
	private int mp;
	
	/** 外攻(物攻)*/
	private int physicalAttack;
	
	/** 内攻(法攻)*/
	private int theurgyAttack;
	
	/** 外防(物防)*/
	private int physicalDefense;
	
	/** 内防(法防)*/
	private int theurgyDefense;
	
	/** 外暴(物暴)*/
	private int physicalCritical;
	
	/** 内暴(法暴)*/
	private int theurgyCritical;
	
	/** 命中*/
	private int hit;
	
	/** 闪避*/
	private int dodge;
	
	/** 坐骑速度*/
	private int speed;

	/** 道具经验*/
	private int propsExp;
	
	/** 元宝经验*/
	private int goldExp;

	/** 批量元宝经验(高级元宝)*/
	private int batchGoldExp;
	
	/** 升级需要的经验值*/
	private int levelupExp;
	
	/** 小爆概率*/
	private int minRate;
	
	/** 大爆概率*/
	private int maxRate;
	
	/** 满值区间*/
	private int fullRegion;
	
	/**
	 * 计算大小暴率
	 * 
	 * @return {@link Integer}	爆率信息. 0-不暴, 1-小暴, 2-大暴
	 */
	public int calcProbability() {
		int ranRate = Tools.getRandomInteger(fullRegion);
		if(ranRate <= this.minRate){
			return minRate;
		} else if(ranRate <= this.maxRate){
			return maxRate;
		}	//没有暴就返回. 0. TODO 
		return 0;
	}

	//Getter and Setter
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
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

	public int getPhysicalAttack() {
		return physicalAttack;
	}

	public void setPhysicalAttack(int physicalAttack) {
		this.physicalAttack = physicalAttack;
	}

	public int getTheurgyAttack() {
		return theurgyAttack;
	}

	public void setTheurgyAttack(int theurgyAttack) {
		this.theurgyAttack = theurgyAttack;
	}

	public int getPhysicalDefense() {
		return physicalDefense;
	}

	public void setPhysicalDefense(int physicalDefense) {
		this.physicalDefense = physicalDefense;
	}

	public int getTheurgyDefense() {
		return theurgyDefense;
	}

	public void setTheurgyDefense(int theurgyDefense) {
		this.theurgyDefense = theurgyDefense;
	}

	public int getPhysicalCritical() {
		return physicalCritical;
	}

	public void setPhysicalCritical(int physicalCritical) {
		this.physicalCritical = physicalCritical;
	}

	public int getTheurgyCritical() {
		return theurgyCritical;
	}

	public void setTheurgyCritical(int theurgyCritical) {
		this.theurgyCritical = theurgyCritical;
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

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getPropsExp() {
		return propsExp;
	}

	public void setPropsExp(int propsExp) {
		this.propsExp = propsExp;
	}

	public int getGoldExp() {
		return goldExp;
	}

	public void setGoldExp(int goldExp) {
		this.goldExp = goldExp;
	}

	public int getBatchGoldExp() {
		return batchGoldExp;
	}

	public void setBatchGoldExp(int batchGoldExp) {
		this.batchGoldExp = batchGoldExp;
	}

	public int getLevelupExp() {
		return levelupExp;
	}

	public void setLevelupExp(int levelupExp) {
		this.levelupExp = levelupExp;
	}

	public int getMinRate() {
		return minRate;
	}

	public void setMinRate(int minRate) {
		this.minRate = minRate;
	}

	public int getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(int maxRate) {
		this.maxRate = maxRate;
	}

	public int getFullRegion() {
		return fullRegion;
	}

	public void setFullRegion(int fullRegion) {
		this.fullRegion = fullRegion;
	}

	@Override
	public String toString() {
		return "HorseConfig [id=" + id + ", name=" + name + ", model=" + model
				+ ", hp=" + hp + ", mp=" + mp + ", physicalAttack="
				+ physicalAttack + ", theurgyAttack=" + theurgyAttack
				+ ", physicalDefense=" + physicalDefense + ", theurgyDefense="
				+ theurgyDefense + ", physicalCritical=" + physicalCritical
				+ ", theurgyCritical=" + theurgyCritical + ", hit=" + hit
				+ ", dodge=" + dodge + ", speed=" + speed + ", propsExp="
				+ propsExp + ", goldExp=" + goldExp + ", batchGoldExp="
				+ batchGoldExp + ", levelupExp=" + levelupExp + ", minRate="
				+ minRate + ", maxRate=" + maxRate + ", fullRegion="
				+ fullRegion + "]";
	}
	
}
