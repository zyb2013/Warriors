package com.yayo.warriors.module.team.model;

import java.io.Serializable;

/**
 * 查询附近的玩家信息
 * 
 * @author Hyint
 */
public class QueryMemberVO implements Serializable {
	private static final long serialVersionUID = 2037178694649214867L;
	
	/** 角色ID */
	private long id;

	/** 玩家阵营 */
	private int camp;
	
	/** 角色等级 */
	private int level;

	/** 角色名字 */
	private String name;

	/** 服务器ID标识 */
	private int serverId;
	
	/** 角色的职业. 门派*/
	private int job;
	
	/** 是否有组队(true-有组队) */
	private boolean inTeam;
	
	/** 帮派的名字 */
	private String allianceName;
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public String getAllianceName() {
		return allianceName;
	}

	public void setAllianceName(String allianceName) {
		this.allianceName = allianceName;
	}
	
	public boolean isInTeam() {
		return inTeam;
	}

	public void setInTeam(boolean inTeam) {
		this.inTeam = inTeam;
	}

	@Override
	public String toString() {
		return "QueryMemberVO [id=" + id + ", level=" + level + ", name=" + name + ", serverId=" + serverId + ", job=" + job + ", allianceName=" + allianceName + "]";
	}


}
