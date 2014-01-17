package com.yayo.warriors.module.task.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.type.TaskStatus;

@Entity
@Table(name = "userEscortTask")
public class UserEscortTask extends BaseModel<Long>{

	private static final long serialVersionUID = 5996133594453407796L;
	
	@Id
	@Column(name="playerId")
	private Long id;
	
	private long lastDate;
	
	private int status = TaskStatus.UNACCEPT;
	
	private int actionTimes;
	
	private long timeCalcer;
	
	private int taskId;
	
	private int quality = Quality.WHITE.ordinal();
	
	private int plunderTimes;
	
	private int beplunderTimes;
	
	private boolean protection;
	
	private int acceptLevel;
	
	@Transient
	private transient volatile Date today = null;

	public void rewards(){
		this.status = TaskStatus.REWARDS;
	}
	
	public void complete(){
		this.status = TaskStatus.COMPLETED;
	}
	
	public void failed(){
		this.status = TaskStatus.FAILED;
	}
	
	public boolean isplunder(){
		return this.plunderTimes > 0;
	}
	
	public boolean isTimeOut(){
		if(this.status == TaskStatus.ACCEPTED){
			if(System.currentTimeMillis()  >= timeCalcer){
				return true;
			}
		}else if(this.status == TaskStatus.FAILED){
			return true;
		}
		return false;
	}
	
	
	public void acceptTask(int level,int taskId,int limitTime){
		this.acceptLevel = level;
		this.taskId = taskId;
		this.plunderTimes = 0;
		this.actionTimes += 1;
		this.status = TaskStatus.COMPLETED;
		this.timeCalcer = System.currentTimeMillis() + (limitTime * 1000);
	}
	
	public void giveupTask(int quality){
		this.taskId = 0;
		this.timeCalcer = 0;
		this.acceptLevel = 0;
		this.plunderTimes = 0;
		this.quality = quality;
		this.protection = false;
		this.status = TaskStatus.UNACCEPT;
	}
	public void reastTask(int quality) {
		this.taskId = 0;
		this.timeCalcer = 0;
		this.plunderTimes = 0;
		this.acceptLevel = 0;
		this.protection = false;
		this.status = TaskStatus.UNACCEPT;
		this.quality = quality;
	}

	public boolean isNoTask() {
		return this.status == TaskStatus.UNACCEPT;
	}
	
	public boolean canAccept() {
		return this.status == TaskStatus.UNACCEPT;
	}
	
	public boolean canGiveup() {
		return this.status != TaskStatus.UNACCEPT;
	}

	public void updateEscortTimes(int quality) {
		if (!this.isOverDate()) {
			if(this.isNoTask()){ 
				this.today = null;
				this.actionTimes = 0;
				this.beplunderTimes = 0;
				this.quality = quality;
				this.protection = false;
				this.lastDate = System.currentTimeMillis();
			}else{
				this.today = null;
				this.actionTimes = 0;
				this.beplunderTimes = 0;
				this.lastDate = System.currentTimeMillis();
			}
		}
	}
	
	private boolean isOverDate() {
		if(today != null){
			return DateUtil.isToday(today);
		}
		synchronized (this) {
			if(today != null){
				return DateUtil.isToday(today);
			}
			today = new Date(this.lastDate);
			return DateUtil.isToday(today);
		}
	}
	
	public static UserEscortTask valueOf(long playerId){
		UserEscortTask userEscortTask = new UserEscortTask();
		userEscortTask.id = playerId;
		return userEscortTask;
	}
	
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id ;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getActionTimes() {
		return actionTimes;
	}

	public void setActionTimes(int actionTimes) {
		this.actionTimes = actionTimes;
	}

	public long getTimeCalcer() {
		return timeCalcer;
	}

	public void setTimeCalcer(long timeCalcer) {
		this.timeCalcer = timeCalcer;
	}

	public int getPlunderTimes() {
		return plunderTimes;
	}

	public void setPlunderTimes(int plunderTimes) {
		this.plunderTimes = plunderTimes;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public long getLastDate() {
		return lastDate;
	}

	public void setLastDate(long lastDate) {
		this.lastDate = lastDate;
	}

	public int getBeplunderTimes() {
		return beplunderTimes;
	}

	public void setBeplunderTimes(int beplunderTimes) {
		this.beplunderTimes = beplunderTimes;
	}

	public boolean isProtection() {
		return protection;
	}

	public void setProtection(boolean protection) {
		this.protection = protection;
	}

	public int getAcceptLevel() {
		return acceptLevel;
	}
	
	public void setAcceptLevel(int acceptLevel) {
		this.acceptLevel = acceptLevel;
	}

	@Override
	public String toString() {
		return "UserEscortTask [id=" + id + ", lastDate=" + lastDate
				+ ", status=" + status + ", actionTimes=" + actionTimes
				+ ", timeCalcer=" + timeCalcer + ", taskId=" + taskId
				+ ", quality=" + quality + ", plunderTimes=" + plunderTimes
				+ ", beplunderTimes=" + beplunderTimes + ", protection="
				+ protection + ", acceptLevel=" + acceptLevel + "]";
	}

	public boolean isAccepted() {
		return this.status != TaskStatus.UNACCEPT && this.status != TaskStatus.REWARDS;
	}

}
