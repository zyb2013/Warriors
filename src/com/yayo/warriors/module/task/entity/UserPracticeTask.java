package com.yayo.warriors.module.task.entity;

import static com.yayo.warriors.module.task.type.TaskStatus.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.basedb.model.PracticeRewardConfig;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.model.TaskCondition;
import com.yayo.warriors.module.task.rule.TaskRule;
import com.yayo.warriors.module.task.type.TaskStatus;
@Entity
@Table(name="userPracticeTask")
public class UserPracticeTask extends BaseModel<Long> {
	private static final long serialVersionUID = -9116181965131667466L;
	@Id
	@Column(name="playerId")
	private Long id;
	
	private int type = -1;
	
	private int amount = 0;
	
	private int completes;
	
	private int taskLevel ;
	
	private int dayOfWeek = 0;
	
	private int totalAmount = 0 ;
	
	@Column(name="conditions")
	private long conditions = 0L;
	
	private String rewardInfo ;
	
	private String taskParams ;

	private int status = TaskStatus.UNACCEPT;
	
	private int quality = Quality.WHITE.ordinal();
	
	@Transient
	private transient volatile boolean refreshable = false;
	
	@Transient
	private transient volatile Set<Integer> rewardInfoSet = null;
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void alterAmount(int addAmount) {
		this.amount = Math.max(0, this.amount + addAmount);
	}

	public long getConditions() {
		return conditions;
	}

	public void setConditions(long conditions) {
		this.conditions = conditions;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/** 完成的任务次数 */
	public void addCompletes(int addComplete) {
		this.completes += addComplete;
	}
	
	public int getCompletes() {
		return completes;
	}

	public void setCompletes(int complete) {
		this.completes = complete;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public boolean isRefreshable() {
		return refreshable;
	}

	public void updateRefreshable(boolean refreshable) {
		this.refreshable = refreshable;
	}

	public boolean validStatus(int...status) {
		return ArrayUtils.contains(status, this.status);
	}
	
	public void checkAndUpdateStatus() {
		if(this.status == TaskStatus.ACCEPTED && this.amount <= 0) {
			this.status = TaskStatus.COMPLETED;
		}
	}
	/**
	 * 构建日环任务
	 * 
	 * @param  playerId					角色ID
	 * @return {@link UserLoopTask}		日环任务
	 */
	public static UserPracticeTask valueOf(long playerId) {
		UserPracticeTask userTask = new UserPracticeTask();
		userTask.id = playerId;
		userTask.refreshable = true;
		userTask.dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		return userTask;
	}

	public boolean needRefreshTask() {
		if(refreshable && this.status == TaskStatus.REWARDS) {
			return true;
		}
		return this.type == -1 || conditions <= 0;
	}

	public void refreshTask(TaskCondition loopTaskInfo, int quality, int playerLevel) {
		this.quality = quality;
		this.status = UNACCEPT;
		this.refreshable = false ;
		this.taskLevel = Math.max(TaskRule.MIN_PRACTICE_LEVEL, playerLevel);
		if(loopTaskInfo != null) {
			this.type = loopTaskInfo.getType();
			this.amount = loopTaskInfo.getAmount() ;
			this.totalAmount = loopTaskInfo.getAmount() ;
			this.conditions = loopTaskInfo.getCondition() ;
			this.taskParams = loopTaskInfo.getTaskParams();
		} else {
			this.amount = this.totalAmount;
		}
	}

	public boolean isNeedDailyRefresh() {
		return this.dayOfWeek != Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
	}

	public void resetFinish() {
		this.completes = 0 ;
		this.rewardInfo = "" ;
		this.refreshable = true ;
		this.rewardInfoSet = null;
		this.dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
	}

	public int getTaskLevel() {
		return Math.max(TaskRule.MIN_PRACTICE_LEVEL, taskLevel);
	}

	public void acceptTask() {
		this.status = TaskStatus.ACCEPTED;
	}

	public String getRewardInfo() {
		return rewardInfo;
	}

	public void setRewardInfo(String rewardInfo) {
		this.rewardInfoSet = null;
		this.rewardInfo = rewardInfo;
	}

	public void setTaskLevel(int taskLevel) {
		this.taskLevel = taskLevel;
	}
	
	public boolean isComplete() {
		return this.status == TaskStatus.COMPLETED || this.status == TaskStatus.REWARDS;
	}

	public boolean completeAllCondition() {
		return this.amount <= 0;
	}

	public void completeLoopTask() {
		this.status = TaskStatus.COMPLETED;
	}

	public String getTaskParams() {
		return taskParams;
	}

	public void setTaskParams(String taskParams) {
		this.taskParams = taskParams;
	}

	public boolean isKillRightMonster(int monsterId) {
		if(StringUtils.isNumeric(taskParams)){
			return Integer.parseInt(this.taskParams) == monsterId ;
		}
		return false;
	}
	
	public int getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	public void giveUp(int quality) {
		this.quality = quality;
		this.amount = totalAmount;
		this.status = TaskStatus.UNACCEPT;
	}

	/**
	 * 编号_完成数量_是否可以领取|编号_完成数量_是否可以领取
	 * 
	 * @param rewardInfosMap
	 * @return
	 */
	public String buildRewardInfoString(Collection<PracticeRewardConfig> rewardConfigs) {
		if(rewardConfigs == null || rewardConfigs.isEmpty()) {
			return "";
		}
		
		StringBuilder builder = new StringBuilder() ;
		Set<Integer> rewardInfos = this.getRewardInfoSet();
		for(PracticeRewardConfig rewardConfig : rewardConfigs) {
			if(!rewardConfig.canReward()) {
				continue;
			}
			
			int rewardId = rewardConfig.getId();
			int state = rewardInfos.contains(rewardId) ? 0 : 1;
			for (RewardVO rewardVO : rewardConfig.getRewardList()) {
				Integer count = rewardVO.getCount();
				Integer itemId = rewardVO.getBaseId();
				builder.append(itemId).append(Splitable.ATTRIBUTE_SPLIT);
				builder.append(count).append(Splitable.ATTRIBUTE_SPLIT);
				builder.append(rewardId).append(Splitable.ATTRIBUTE_SPLIT);
				builder.append(state).append(Splitable. ELEMENT_DELIMITER);
			}
		}
		return builder.toString();
	}
	
	/**
	 * 获得奖励信息列表
	 * 
	 * @return {@link Set}		已领取奖励的ID列表
	 */
	public Set<Integer> getRewardInfoSet() {
		if(this.rewardInfoSet != null) {
			return this.rewardInfoSet;
		}
		
		synchronized (this) {
			if(this.rewardInfoSet != null) {
				return this.rewardInfoSet;
			}
			
			this.rewardInfoSet = new HashSet<Integer>();
			if(StringUtils.isBlank(this.rewardInfo)) {
				return this.rewardInfoSet;
			}
			
			for (String element : rewardInfo.split(Splitable.ELEMENT_SPLIT)) {
				if(!StringUtils.isBlank(element)) {
					this.rewardInfoSet.add(Integer.valueOf(element));
				}
			}
		}
		return this.rewardInfoSet;
	}
	
	/** 
	 * 增加奖励信息
	 * 
	 * @param rewardId		奖励ID
	 * @param updateInfo	true-更新奖励列表, false-不更新
	 */
	public void addRewardInfo(int rewardId, boolean updateInfo) {
		this.getRewardInfoSet().add(rewardId);
		if(updateInfo) {
			this.updateRewardInfoSet();
		}
	}
	
	/** 更新奖励信息 */
	public void updateRewardInfoSet() {
		StringBuffer buffer = new StringBuffer();
		for (Integer rewardCount : getRewardInfoSet()) {
			buffer.append(rewardCount).append(Splitable.ELEMENT_DELIMITER);
		}
		this.rewardInfo = buffer.toString();
	}
	
	@Override
	public String toString() {
		return "UserLoopTask [id=" + id + ", type=" + type + ", amount=" + amount + ", completes="
				+ completes + ", taskLevel=" + taskLevel + ", dayOfWeek=" + dayOfWeek
				+ ", totalAmount=" + totalAmount + ", conditions=" + conditions + ", rewardInfo="
				+ rewardInfo + ", taskParams=" + taskParams + ", status=" + status + ", quality="
				+ quality + "]";
	}

	public boolean canGainThisReward(int rewardId) {
		return this.getRewardInfoSet().contains(rewardId);
	}
	
	public boolean isAccepted() {
		return this.status != TaskStatus.UNACCEPT && this.status != TaskStatus.REWARDS;
	}
}
