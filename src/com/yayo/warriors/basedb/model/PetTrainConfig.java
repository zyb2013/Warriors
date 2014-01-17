package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 家将培养表
 * @author liuyuhua
 */
@Resource
public class PetTrainConfig {
	
	/** 等级*/
	@Id
	private int id;
	
	/** 成长培养成功率*/
	private int savvyRate;
	
	/** 成长所需要使用的道具,通过该道具获取价格*/
	private int savvyProps;
	
	/** 培养满值空间*/
	private int trainfullRate;
	
	/** 需要消耗的铜币*/
	private int needSilver;
	
	/** 成长培养失败后,需要掉的等级*/
	private int savvyFail;
	
	/** 成长培养后获得,祝福值*/
	private int savvyBless;
	
	/** 成长祝福满值*/
	private int savvyBlessFull;
	
	/** 需要的道具数量*/
	private int number;
	
	/** 等级限制*/
	private int levelLimit;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSavvyRate() {
		return savvyRate;
	}

	public void setSavvyRate(int savvyRate) {
		this.savvyRate = savvyRate;
	}

	public int getSavvyProps() {
		return savvyProps;
	}

	public void setSavvyProps(int savvyProps) {
		this.savvyProps = savvyProps;
	}

	public int getTrainfullRate() {
		return trainfullRate;
	}

	public void setTrainfullRate(int trainfullRate) {
		this.trainfullRate = trainfullRate;
	}

	public int getNeedSilver() {
		return needSilver;
	}

	public void setNeedSilver(int needSilver) {
		this.needSilver = needSilver;
	}

	public int getSavvyFail() {
		return savvyFail;
	}

	public void setSavvyFail(int savvyFail) {
		this.savvyFail = savvyFail;
	}

	public int getSavvyBless() {
		return savvyBless;
	}

	public void setSavvyBless(int savvyBless) {
		this.savvyBless = savvyBless;
	}

	public int getSavvyBlessFull() {
		return savvyBlessFull;
	}

	public void setSavvyBlessFull(int savvyBlessFull) {
		this.savvyBlessFull = savvyBlessFull;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getLevelLimit() {
		return levelLimit;
	}

	public void setLevelLimit(int levelLimit) {
		this.levelLimit = levelLimit;
	}

	@Override
	public String toString() {
		return "PetTrainConfig [id=" + id + ", savvyRate=" + savvyRate
				+ ", savvyProps=" + savvyProps + ", trainfullRate="
				+ trainfullRate + ", needSilver=" + needSilver + ", savvyFail="
				+ savvyFail + ", savvyBless=" + savvyBless
				+ ", savvyBlessFull=" + savvyBlessFull + ", number=" + number
				+ ", levelLimit=" + levelLimit + "]";
	}

}
