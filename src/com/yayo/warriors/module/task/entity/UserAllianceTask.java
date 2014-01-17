package com.yayo.warriors.module.task.entity;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.task.model.AllianceTask;
import com.yayo.warriors.module.task.model.AllianceTaskEvent;
import com.yayo.warriors.module.task.type.EventStatus;

@Entity
@Table(name = "userAllianceTask")
public class UserAllianceTask extends BaseModel<Long> { 
	private static final long serialVersionUID = 5409774637356591854L;
	
	@Id
	@Column(name="playerId")
	private Long id;
	
	@Lob
	private String progresstask = "";
	
	@Lob
	private String rewardstask = "";
	
	private long startTime = 0;
	
	@Transient
	private transient volatile Date date = null;
	
	@Transient
	private transient volatile Map<Integer,Integer> rewardstasks = null;
	
	@Transient
	private transient volatile Set<AllianceTask> allianceTasks = null;
	
	public static UserAllianceTask valueOf(long playerId){
		UserAllianceTask task = new UserAllianceTask();
		task.id = playerId;
		return task;
	}
	
	public Map<Integer,Integer> getRewardsTasks(){
		if(rewardstasks != null){
			return rewardstasks;
		}
		
		synchronized (this) {
			if(rewardstasks != null){
				return rewardstasks;
			}
			
			rewardstasks = new HashMap<Integer,Integer>();
			if(rewardstask == null || rewardstask.isEmpty()){
				return rewardstasks;
			}
			
			List<String[]> resultSplit = Tools.delimiterString2Array(rewardstask);
			for(String[] results : resultSplit){
				if(results.length < 2){
					continue;
				}
				
				int taskId = Integer.parseInt(results[0]);
				int count = Integer.parseInt(results[1]);
				rewardstasks.put(taskId, count);
			}
			
			return rewardstasks;
		}
	}
	
	
	public void addTask(AllianceTask allianceTask){
		this.getTasks().add(allianceTask);
		this.progresstask = this.build2ProssString();
	}
	
	public void addAllTasks(Collection<AllianceTask> allianceTasks){
		this.getTasks().addAll(allianceTasks);
		this.progresstask = this.build2ProssString();
	}
	
	public void updateTask(AllianceTask allianceTask){
		allianceTask.updateEvents();
		this.getTasks().add(allianceTask);
		this.progresstask = this.build2ProssString();
	}
	
	public void removeTask(AllianceTask allianceTask){
		this.getTasks().remove(allianceTask);
		this.progresstask = this.build2ProssString();
	}
	public void addRewardTask(int taskId){
		Map<Integer,Integer> rewardTasks = this.getRewardsTasks(); 
		if(rewardTasks != null){
			if(rewardTasks.get(taskId) != null){
				int count = rewardTasks.get(taskId);
				count+=1;
				rewardTasks.put(taskId, count);
			}else{
				rewardTasks.put(taskId, 1);
			}
			this.rewardstask = this.build2RewardString();
		}
	}
	
	public AllianceTask getTask(int taskId){
		for(AllianceTask task : this.getTasks()){
			if(task.getTaskId() == taskId){
				return task;
			}
		}
		return null;
	}
	
	public Set<AllianceTask> getTasks() {
		if(date != null && DateUtil.isToday(date)){
			if(allianceTasks != null){
				return allianceTasks;
			}
			
			synchronized (this) {
				if(allianceTasks != null){
					return allianceTasks;
				}
				allianceTasks = buildTasks();
				return allianceTasks;
			}
		}else{
			synchronized (this) {
				if(date == null){
					date = new Date(startTime);
				}
				
				if(DateUtil.isToday(date)){
					if(allianceTasks != null){
						return allianceTasks;
					}
					
					allianceTasks = buildTasks();
					return allianceTasks;
				}
				
				startTime = System.currentTimeMillis();
				date = new Date(startTime);
				progresstask = "";
				rewardstask = "";
				rewardstasks = null;
				allianceTasks = null;
				
				allianceTasks = buildTasks();
				return allianceTasks;
			}
		}
		
	}
	
	private Set<AllianceTask> buildTasks(){
		Set<AllianceTask> alliancetasks = new HashSet<AllianceTask>();
		if(this.getProgresstask() == null){
			this.setProgresstask("");
		}
		
		if(this.getProgresstask().isEmpty()){
			return alliancetasks;
		}
		
		List<String[]> arrays = Tools.delimiterString2Array(this.getProgresstask());
		for(String[] array : arrays){
			if(array.length >= 7){
				int taskId = Integer.parseInt(array[0]);
				AllianceTask allianceTask = null;
				for(AllianceTask tmpCamp : alliancetasks){
					if(tmpCamp.getTaskId() == taskId){
						allianceTask = tmpCamp;
					}
				}
				
				if(allianceTask == null){
					int completeTime = this.getRewardsTasks().get(taskId) == null ? 0 : this.getRewardsTasks().get(taskId);
					allianceTask = AllianceTask.valueOf(id, taskId, completeTime);
					alliancetasks.add(allianceTask);
				}
				
				int type = Integer.valueOf(array[1]);
				int condition = Integer.valueOf(array[2]);
				int amount = Integer.valueOf(array[3]);
				int totalAmonnt = Integer.valueOf(array[4]);
				EventStatus status =  EnumUtils.getEnum(EventStatus.class, Integer.valueOf(array[5]));
				int baseId = Integer.valueOf(array[6]);
				allianceTask.addTaskEvent(AllianceTaskEvent.valueOf(type, condition, amount, totalAmonnt, status, baseId));
			}
		}
		
		
		if(!alliancetasks.isEmpty()){ 
			for(AllianceTask allianceTask : alliancetasks){
				allianceTask.checkTaskStatus();
			}
		}
		
		return alliancetasks;
	}
	
	private String build2ProssString(){
		StringBuilder builder = new StringBuilder();
		if(this.allianceTasks != null && !this.allianceTasks.isEmpty()){
			for(AllianceTask allianceTask : allianceTasks){
				builder.append(allianceTask.build2Task()).append(Splitable.ELEMENT_DELIMITER);
			}
		}
		
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
	private String build2RewardString(){
		StringBuilder builder = new StringBuilder();
		if(this.rewardstasks != null && !this.rewardstasks.isEmpty()){
			for(Entry<Integer, Integer> entry : rewardstasks.entrySet()){
				int taskId = entry.getKey();
				int count = entry.getValue();
				builder.append(taskId).append(Splitable.ATTRIBUTE_SPLIT).append(count).append(Splitable.ELEMENT_DELIMITER);
			}
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
		this.date = null;	
	}

	public String getProgresstask() {
		return progresstask;
	}

	public void setProgresstask(String progresstask) {
		this.progresstask = progresstask;
	}

	public String getRewardstask() {
		return rewardstask;
	}

	public void setRewardstask(String rewardstask) {
		this.rewardstask = rewardstask;
	}

	@Override
	public String toString() {
		return "UserAllianceTask [id=" + id + ", progresstask=" + progresstask
				+ ", rewardstask=" + rewardstask + ", startTime=" + startTime
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserAllianceTask other = (UserAllianceTask) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
