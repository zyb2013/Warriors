package com.yayo.warriors.module.task.vo;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.yayo.warriors.basedb.model.PracticeRewardConfig;
import com.yayo.warriors.module.task.entity.UserPracticeTask;
import com.yayo.warriors.module.task.rule.TaskRule;

/**
 * 试练任务 VO 对象
 * 
 * @author Hyint
 */
public class PracticeTaskVO implements Serializable {
	private static final long serialVersionUID = 1073472611492216033L;

	/** 角色ID */
	private long playerId ;

	/** 任务的事件类型  */
	private int type ;

	/** 任务的完成条件  */
	private long conditions;
	
	/** 任务的剩余数量 */
	private int amount;

	/** 任务的总数量 */
	private int totalAmount;

	/** 任务的品质 */
	private int quality;

	/** 任务的状态 */
	private int status;
	
	/** 购买物品的NPCID */
	private int npcId;
	
	/** 已完成的次数 */
	private int completes;

	/** 任务的等级 */
	private int taskLevel;
	
	/** 当前任务是属于哪一天的 */
	private int dayOfWeek = 0;
	
	/** 银币奖励 */
	private int silverExpr;

	/** 经验奖励 */
	private int expExpr;

	/** 真气奖励 */
	private int gasExpr;
	
	/** 奖励道具ID1_奖励道具的数量1_需要完成任务数量1_是否可以领取1|奖励道具ID2_奖励道具的数量2_需要完成任务数量2_是否可以领取2|(0不可领取，1可领取)*/
	private String rewards ;
	/**
	 * 构建试炼任务VO对象
	 * 
	 * @param userTask			用户试炼任务对象
	 * @param practiceReward	试炼任务奖励表
	 * @param rewardInfos		试炼任务奖励信息
	 */
	public PracticeTaskVO(UserPracticeTask userTask, PracticeRewardConfig practiceReward, Collection<PracticeRewardConfig> rewardInfos) {
		this.type = userTask.getType();
		this.playerId = userTask.getId();
		this.amount = userTask.getAmount();
		this.status = userTask.getStatus();
		this.quality = userTask.getQuality();
		this.completes = userTask.getCompletes();
		this.taskLevel = userTask.getTaskLevel();
		this.dayOfWeek = userTask.getDayOfWeek();
		this.conditions = userTask.getConditions();
		this.totalAmount = userTask.getTotalAmount() ;
		this.npcId = StringUtils.isNumeric(userTask.getTaskParams()) ? Integer.parseInt(userTask.getTaskParams()) : 0;
		this.rewards = userTask.buildRewardInfoString(rewardInfos);
		
		if(practiceReward != null) {
			this.expExpr = practiceReward.getExpValue(this.taskLevel, this.quality) ;
			this.gasExpr = practiceReward.getGasValue(this.taskLevel, this.quality) ;
			this.silverExpr = practiceReward.getSilverValue(this.taskLevel, this.quality) ;
		}
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

	public int getTaskLevel() {
		return taskLevel;
	}

	public void setTaskLevel(int taskLevel) {
		this.taskLevel = taskLevel;
	}

	public long getConditions() {
		return conditions;
	}

	public void setConditions(long conditions) {
		this.conditions = conditions;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
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

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public int getCompletes() {
		return completes;
	}

	public void setCompletes(int complete) {
		this.completes = complete;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
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

	public boolean isCanAcceptd() {
		return this.completes < TaskRule.MAX_PRACTICE_COMPLETE_COUNT;
	}

	@Override
	public String toString() {
		return "PracticeTaskVO [playerId=" + playerId + ", type=" + type + ", conditions="
				+ conditions + ", amount=" + amount + ", totalAmount=" + totalAmount + ", quality="
				+ quality + ", status=" + status + ", npcId=" + npcId + ", completes=" + completes
				+ ", dayOfWeek=" + dayOfWeek + ", silverExpr=" + silverExpr + ", expExpr="
				+ expExpr + ", gasExpr=" + gasExpr + ", rewards=" + rewards + "]";
	}
	
}
