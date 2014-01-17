package com.yayo.warriors.module.lottery.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 抽奖VO
 *	@author jonsai 
 */
public class LotteryVO implements Serializable {
	/**  */
	private static final long serialVersionUID = -3975818299093677436L;

	/** 角色id */
	private long playerId ;
	
	/** 角色名 */
	private String playerName ;
	
	/** 抽奖规则id */
	private int lotteryId;
	
	/** 抽奖奖励 */
	private LotteryRewardVo[] rewards = null;
	
	//--------------------------------
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	
	public static LotteryVO valueOf(long playerId, String playerName, int lotterId, List<LotteryRewardVo> values){
		LotteryVO vo = new LotteryVO();
		vo.playerId = playerId;
		vo.playerName = playerName;
		vo.lotteryId = lotterId;
		vo.rewards = values != null ? values.toArray(new LotteryRewardVo[values.size()]) : null;
		
		return vo;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getLotteryId() {
		return lotteryId;
	}

	public void setLotteryId(int lotteryId) {
		this.lotteryId = lotteryId;
	}

	public LotteryRewardVo[] getRewards() {
		return rewards;
	}

	public void setRewards(LotteryRewardVo[] rewards) {
		this.rewards = rewards;
	}
	
}
