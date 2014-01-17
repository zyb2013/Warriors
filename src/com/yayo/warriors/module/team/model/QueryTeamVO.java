package com.yayo.warriors.module.team.model;

import java.io.Serializable;


/**
 * 查询组队信息
 * 
 * @author Hyint
 */
public class QueryTeamVO implements Serializable {
	private static final long serialVersionUID = 465160894945131221L;

	/** 队伍 ID */
	private int id;
	
	/** 队长 ID */
	private long leaderId;
	
	/** 队长的名字 */
	private String leaderName;
	
	/** 阵法. 可能后面会改名 */
	private int teamMethod;
	
	/** 平均等级*/
	private int averageLevel;
	
	/** 队伍当前人数 */
	private int currentMembers;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(long leaderId) {
		this.leaderId = leaderId;
	}

	public String getLeaderName() {
		return leaderName;
	}

	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}

	public int getTeamMethod() {
		return teamMethod;
	}

	public void setTeamMethod(int teamMethod) {
		this.teamMethod = teamMethod;
	}

	public int getAverageLevel() {
		return averageLevel;
	}

	public void setAverageLevel(int averageLevel) {
		this.averageLevel = averageLevel;
	}

	public int getCurrentMembers() {
		return currentMembers;
	}

	public void setCurrentMembers(int currentMembers) {
		this.currentMembers = currentMembers;
	}

	@Override
	public String toString() {
		return "QueryTeamVO [id=" + id + ", leaderId=" + leaderId + ", leaderName=" + leaderName
				+ ", teamMethod=" + teamMethod + ", averageLevel=" + averageLevel
				+ ", currentMembers=" + currentMembers + "]";
	}

}
