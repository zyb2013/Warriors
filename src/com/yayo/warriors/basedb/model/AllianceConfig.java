package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

@Resource
public class AllianceConfig {
	
	/** 等级*/
	@Id
	private int id;
	
	/** 升级需要的资金*/
	private int needSilver;
	
	/** 等级对应的帮主职位个数*/
	private int masterNum;
	
	/** 等级对应的长老职位个数*/
	private int elderNum;
	
	/** 等级对应的护法职位个数*/
	private int prolawNum;
	
	/** 等级对应的副帮主职位个数*/
	private int deputymasterNum;
	
	/** 成员上限*/
	private int memberLimit;
	
	/** 资源(可存铜币)上限*/
	private int silverLimit;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNeedSilver() {
		return needSilver;
	}

	public void setNeedSilver(int needSilver) {
		this.needSilver = needSilver;
	}

	public int getMasterNum() {
		return masterNum;
	}

	public void setMasterNum(int masterNum) {
		this.masterNum = masterNum;
	}

	public int getElderNum() {
		return elderNum;
	}

	public void setElderNum(int elderNum) {
		this.elderNum = elderNum;
	}

	public int getProlawNum() {
		return prolawNum;
	}

	public void setProlawNum(int prolawNum) {
		this.prolawNum = prolawNum;
	}

	public int getDeputymasterNum() {
		return deputymasterNum;
	}

	public void setDeputymasterNum(int deputymasterNum) {
		this.deputymasterNum = deputymasterNum;
	}

	public int getMemberLimit() {
		return memberLimit;
	}

	public void setMemberLimit(int memberLimit) {
		this.memberLimit = memberLimit;
	}

	public int getSilverLimit() {
		return silverLimit;
	}

	public void setSilverLimit(int silverLimit) {
		this.silverLimit = silverLimit;
	}
	
}
