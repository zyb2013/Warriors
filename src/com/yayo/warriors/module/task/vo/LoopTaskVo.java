package com.yayo.warriors.module.task.vo;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.yayo.warriors.basedb.model.LoopRewardConfig;
import com.yayo.warriors.module.task.entity.UserLoopTask;
import com.yayo.warriors.module.task.rule.TaskRule;

public class LoopTaskVo implements Serializable{
	private static final long serialVersionUID = 1073472611492216033L;
	
	private long playerId ;
	private int type ;
	private long conditions;
	private int amount;
	private int totalAmount;
	private int quality;
	private int status;
	private int npcId;
	private int completes;
	private int dayOfWeek = 0;
	private int silverExpr;
	private int expExpr;
	private int gasExpr;
	private String rewards ;
	
	public LoopTaskVo(UserLoopTask userTask, LoopRewardConfig loopRewardConfig) {
		this.type = userTask.getType();
		this.playerId = userTask.getId();
		this.amount =userTask.getAmount();
		this.status = userTask.getStatus();
		this.quality = userTask.getQuality();
		this.completes = userTask.getCompletes();
		this.dayOfWeek = userTask.getDayOfWeek();
		this.conditions = userTask.getConditions();
		this.totalAmount = userTask.getTotalAmount();
		this.npcId = StringUtils.isNumeric(userTask.getTaskParams()) ? Integer.parseInt(userTask.getTaskParams()) : 0;
		this.silverExpr = loopRewardConfig.getSilverValue(userTask.getTaskLevel(), userTask.getQuality());
		this.expExpr = loopRewardConfig.getExpValue(userTask.getTaskLevel(), userTask.getQuality());
		this.gasExpr = loopRewardConfig.getGasValue(userTask.getTaskLevel(), userTask.getQuality());
	}
	
	public LoopTaskVo(UserLoopTask userTask, LoopRewardConfig loopRewardConfig , Collection<LoopRewardConfig> rewardInfos) {
		this.type = userTask.getType();
		this.playerId = userTask.getId();
		this.amount = userTask.getAmount();
		this.status = userTask.getStatus();
		this.quality = userTask.getQuality();
		this.completes = userTask.getCompletes();
		this.dayOfWeek = userTask.getDayOfWeek();
		this.conditions = userTask.getConditions();
		this.totalAmount = userTask.getTotalAmount() ;
		this.npcId = StringUtils.isNumeric(userTask.getTaskParams()) ? Integer.parseInt(userTask.getTaskParams()) : 0;
		this.rewards = userTask.buildRewardInfoString(rewardInfos);
		
		this.silverExpr = loopRewardConfig.getSilverValue(userTask.getTaskLevel(),userTask.getQuality()) ;
		this.expExpr = loopRewardConfig.getExpValue(userTask.getTaskLevel(),userTask.getQuality()) ;
		this.gasExpr = loopRewardConfig.getGasValue(userTask.getTaskLevel(),userTask.getQuality()) ;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getConditions() {
		return conditions;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public void setConditions(long condition) {
		this.conditions = condition;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getSilverExpr() {
		return silverExpr;
	}

	public void setSilverExpr(int silverExpr) {
		this.silverExpr = silverExpr;
	}

	public int getExpExpr() {
		return expExpr;
	}

	public void setExpExpr(int expExpr) {
		this.expExpr = expExpr;
	}

	public int getGasExpr() {
		return gasExpr;
	}

	public void setGasExpr(int gasExpr) {
		this.gasExpr = gasExpr;
	}

	public String getRewards() {
		return rewards;
	}

	public void setRewards(String rewards) {
		this.rewards = rewards;
	}

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}



	public int getTotalAmount() {
		return totalAmount;
	}

	public int getCompletes() {
		return completes;
	}

	public boolean isCanAcceptd() {
		return this.completes < TaskRule.MAX_LOOP_COMPLETE_COUNT;
	}
	
	public void setCompletes(int complete) {
		this.completes = complete;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public String toString() {
		return "LoopTaskVo [playerId=" + playerId + ", type=" + type + ", conditions=" + conditions
				+ ", amount=" + amount + ", totalAmount=" + totalAmount + ", quality=" + quality
				+ ", status=" + status + ", npcId=" + npcId + ", completes=" + completes
				+ ", silverExpr=" + silverExpr + ", expExpr=" + expExpr + ", gasExpr=" + gasExpr
				+ ", rewards=" + rewards + "]";
	}
	
}
