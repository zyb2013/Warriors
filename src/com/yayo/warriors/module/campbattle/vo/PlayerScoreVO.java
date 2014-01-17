package com.yayo.warriors.module.campbattle.vo;

import java.io.Serializable;
import java.util.Date;

import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.user.type.Camp;


/**
 * 积分VO
 * @author jonsai
 *
 */
public class PlayerScoreVO implements Serializable {
	private static final long serialVersionUID = -3400567462670708464L;

	/** 角色id */
	private long playerId;
	
	/** 角色名 */
	private String playerName;
	
	/** 阵营 */
	private Camp camp = Camp.NONE;
	
	/** 阵营称号 */
	private CampTitle campTitle = CampTitle.NONE;
	
	/** 参与次数 */
	private int joins;
	
	/** 得分排名 */
	private int rank;
	
	/** 总得分 */
	private int totalScore;
	
	/** 领官衔俸禄奖励时间, 为空或不是今天可以领奖 */
	private Date salaryReward;
	
	/** 领官衔时装奖励时间, 为空表示可以领奖 */
	private Date suitReward;

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getJoins() {
		return joins;
	}

	public void setJoins(int joins) {
		this.joins = joins;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public CampTitle getCampTitle() {
		return campTitle;
	}

	public void setCampTitle(CampTitle campTitle) {
		this.campTitle = campTitle;
	}

	public Date getSalaryReward() {
		return salaryReward;
	}

	public void setSalaryReward(Date salaryReward) {
		this.salaryReward = salaryReward;
	}

	public Date getSuitReward() {
		return suitReward;
	}

	public void setSuitReward(Date suitReward) {
		this.suitReward = suitReward;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
	
}
