package com.yayo.warriors.module.rank.entity;

import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.module.user.type.Sex;

public class RankEntry extends BaseModel<Long> {
	private static final long serialVersionUID = -3812007324289206292L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private RankType rankType = RankType.PLAYER_LEVEL;
	
	private String name;
	
	private String name2;
	
	private Sex sex = Sex.MALE;
	
	private Job job = Job.COMMON;
	
	@Enumerated
	private Camp camp = Camp.NONE;
	
	private int allianceSize = 0;
	
	private long allianceSilver = 0;
	
	private int level = 0;
	
	private long value = 0;
	

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

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

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
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

	public RankType getRankType() {
		return rankType;
	}

	public void setRankType(RankType rankType) {
		this.rankType = rankType;
	}
	
}
