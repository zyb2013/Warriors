package com.yayo.warriors.module.rank.vo;

import java.io.Serializable;

import javax.persistence.Enumerated;

import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.module.user.type.Sex;

/**
 * 排行榜信息vo
 * @author jonsai
 *
 */
public class RankInfoVO implements Serializable{
	
	private static final long serialVersionUID = -4802183668716530520L;
	
	/** 角色id */
	private long playerId = 0L;
	
	/** 名字（角色名称， 帮派名称， 家将名称， 坐骑名称）  */
	private String name;
	
	/** 帮主角色名,家将主人名称,帮派名称 */
	private String name2;
	
	/** 性别  {@link Sex} */
	private int sex = Sex.MALE.ordinal();
	
	/** 职业 {@link Job}  */
	private int job = Job.COMMON.ordinal();
	
	/** 角色的阵营. 默认无阵营. {@link Camp}  */
	@Enumerated
	private int camp = Camp.NONE.ordinal();
	
	/** 帮派人数 */
	private int allianceSize = 0;
	
	/** 帮派资金 */
	private long allianceSilver = 0;
	
	/** 玩家等级, 帮派等级 , 家将等级, 坐骑等级, */
	private int level = 0;
	
	/** 数据 (资质/悟性/成长/铜币/武功层数/等) */
	private long value = 0;
	
	/** (家将自增id) */
	private long autoId;
	
	/** 家将的基础id */
	private int baseId;
	
	/** 排行名次,从1-10名才会使用到此字段  */
	private int rankIndex;
	
	//--------get and set--------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getAllianceSize() {
		return allianceSize;
	}

	public void setAllianceSize(int allianceSize) {
		this.allianceSize = allianceSize;
	}

	public long getAllianceSilver() {
		return allianceSilver;
	}

	public void setAllianceSilver(long allianceSilver) {
		this.allianceSilver = allianceSilver;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getRankIndex() {
		return rankIndex;
	}

	public void setRankIndex(int rankIndex) {
		this.rankIndex = rankIndex;
	}

	public long getAutoId() {
		return autoId;
	}

	public void setAutoId(long autoId) {
		this.autoId = autoId;
	}
	
	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		RankInfoVO other = (RankInfoVO) obj;
		if (playerId != other.playerId){
			return false;
		}
		return true;
	}
	
}
