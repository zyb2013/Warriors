package com.yayo.warriors.module.duntask.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.yayo.warriors.basedb.model.DungeonTaskEventConfig;
import com.yayo.warriors.module.duntask.types.TaskState;


/***
 * 副本任务 
 * @author liuyuhua
 */
public class DunTask {

	/** 副本ID*/
	private long id;
	
	/** 副本原型ID*/
	private int baseId;
	
	/** 玩家的ID*/
	private long playerId;
	
	/** 状态*/
	private TaskState state;
	
	/** 事件类型*/
	private List<DunTaskEvent> events = new ArrayList<DunTaskEvent>();
	
	/** 自动ID生成*/
	private static final AtomicLong atomicLong = new AtomicLong(1); 
	
	/**
	 * 私有构造
	 */
	private DunTask(){
		
	}
	
	/**
	 * 构造方法
	 * @param id           
	 * @param playerId     玩家的ID
	 * @param id           副本任务ID
	 * @param eventConfigs 事件配置
	 * @return {@link DunTask}           
	 */
	public static DunTask valueOf(long playerId,int baseId,Collection<DungeonTaskEventConfig> eventConfigs){
		DunTask dunTask = new DunTask();
		dunTask.baseId = baseId;
		dunTask.playerId = playerId;
		dunTask.id = getDungeonTaskId();
		dunTask.state = TaskState.PROGRESS;
		if(eventConfigs != null && !eventConfigs.isEmpty()){
			for(DungeonTaskEventConfig config : eventConfigs){
				dunTask.events.add(DunTaskEvent.valueOf(config));
			}
		}
		return dunTask;
	}
	
	private static synchronized long getDungeonTaskId() {
		return atomicLong.getAndIncrement();
	}
	
	/**
	 * 任务是否完成
	 * @return true 完成;false没有完成
	 */
	public boolean isCompleted(){
		if(this.events.isEmpty()){
			return true;
		}
		for(DunTaskEvent event : this.events){
			if(!event.isCompleted()){
				return false;
			}
		}
		return true;
	}

	//Getter and Setter....
	public long getPlayerId() {
		return playerId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public TaskState getState() {
		return state;
	}

	public void setState(TaskState state) {
		this.state = state;
	}

	public List<DunTaskEvent> getEvents() {
		return events;
	}

	public void setEvents(List<DunTaskEvent> events) {
		this.events = events;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DunTask other = (DunTask) obj;
		if (id != other.id)
			return false;
		if (playerId != other.playerId)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "DunTask [id=" + id + ", baseId=" + baseId + ", playerId="
				+ playerId + ", state=" + state + ", events=" + events + "]";
	}
	
}
