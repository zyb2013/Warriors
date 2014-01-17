package com.yayo.warriors.module.task.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import com.yayo.warriors.module.task.model.CampTask;
import com.yayo.warriors.module.task.model.TaskEvent;
import com.yayo.warriors.module.task.type.EventStatus;

@Entity
@Table(name = "userCampTask")
public class UserCampTask extends BaseModel<Long> {
	private static final long serialVersionUID = 5409774637356591854L;
	
	@Id
	@Column(name="playerId")
	private Long id;
	
	@Lob
	private String progresstask = "";
	
	@Lob
	private String rewardstask = "";
	
	private int level = 0;
	
	private long startTime = 0;
	
	@Transient
	private transient volatile Date date = null;
	
	@Transient
	private transient volatile List<Integer> rewardstasks = null;
	
	@Transient
	private transient volatile Set<CampTask> campTasks = null;
	
	public static UserCampTask valueOf(long playerId){
		UserCampTask task = new UserCampTask();
		task.id = playerId;
		return task;
	}
	
	public List<Integer> getRewardsTasks(){
		if(rewardstasks != null){
			return rewardstasks;
		}
		
		synchronized (this) {
			if(rewardstasks != null){
				return rewardstasks;
			}
			
			rewardstasks = new ArrayList<Integer>();
			if(rewardstask == null || rewardstask.isEmpty()){
				return rewardstasks;
			}
			
			String[] tmp = rewardstask.split(Splitable.ATTRIBUTE_SPLIT);
			for(String taskId : tmp){
				rewardstasks.add(Integer.parseInt(taskId));
			}
			return rewardstasks;
		}
	}
	
	public void addCampTask(CampTask campTask){
		this.getCampTask().add(campTask);
		this.progresstask = this.build2ProssString();
	}
	
	public void updateCampTask(CampTask campTask){
		this.getCampTask().add(campTask);
		this.progresstask = this.build2ProssString();
	}
	public void removeCampTask(CampTask campTask){
		this.getCampTask().remove(campTask);
		this.progresstask = this.build2ProssString();
	}
	
	public void addRewardCampTask(int taskId){
		this.getRewardsTasks().add(taskId);
		this.rewardstask = this.build2RewardString();
	}
	
	public CampTask getCampTask(int taskId){
		for(CampTask task : this.getCampTask()){
			if(task.getTaskId() == taskId){
				return task;
			}
		}
		return null;
	}
	
	public Set<CampTask> getCampTask() {
		synchronized (this) {
			if(!isToday()){
				this.setLevel(0);         
				this.setRewardstask("");  
				this.setProgresstask(""); 
				this.setStartTime(System.currentTimeMillis());
				this.campTasks = null;
				this.rewardstasks = null;
				this.date = new Date(startTime);
		     }
			
			if(campTasks != null){
				return campTasks;
			}
			campTasks = this.getCampTasks();
			return campTasks;
		}
	}
	
	private Set<CampTask> getCampTasks(){
		Set<CampTask> camptasks = new HashSet<CampTask>();
		if(this.getProgresstask() == null){
			this.setProgresstask("");
		}
		
		if(this.getProgresstask().isEmpty()){
			return camptasks;
		}
		
		List<String[]> arrays = Tools.delimiterString2Array(this.getProgresstask());
		for(String[] array : arrays){
			if(array.length >= 6){
				int taskId = Integer.parseInt(array[0]);
				CampTask campTask = null;
				for(CampTask tmpCamp : camptasks){
					if(tmpCamp.getTaskId() == taskId){
						campTask = tmpCamp;
					}
				}
				
				if(campTask == null){
					campTask = CampTask.valueOf(id, taskId);
					camptasks.add(campTask);
				}
				
				int type = Integer.valueOf(array[1]);
				int condition = Integer.valueOf(array[2]);
				int amount = Integer.valueOf(array[3]);
				int totalAmonnt = Integer.valueOf(array[4]);
				EventStatus status =  EnumUtils.getEnum(EventStatus.class, Integer.valueOf(array[5]));
				campTask.addTaskEvent(TaskEvent.valueOf(type, condition, amount, totalAmonnt, status));
			}
		}
		
		
		if(!camptasks.isEmpty()){
			for(CampTask campTask : camptasks){
				campTask.checkTaskStatus();
			}
		}
		
		return camptasks;
	}
	
	private boolean isToday(){
		synchronized (this) {
			if(date == null){
				date = new Date(startTime);
			}
			return DateUtil.isToday(date);
		}
	}
	
	private String build2ProssString(){
		StringBuilder builder = new StringBuilder();
		if(this.campTasks != null && !this.campTasks.isEmpty()){
			for(CampTask campTask : campTasks){
				builder.append(campTask.build2Task()).append(Splitable.ELEMENT_DELIMITER);
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
			for(int taskId : rewardstasks){
				builder.append(taskId).append(Splitable.ATTRIBUTE_SPLIT);
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return "UserCampTask [id=" + id + ", progresstask=" + progresstask
				+ ", rewardstask=" + rewardstask + ", level=" + level
				+ ", startTime=" + startTime + "]";
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
		UserCampTask other = (UserCampTask) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public boolean isAccepted() {
		return !this.getCampTasks().isEmpty();
	}
}
