package com.yayo.warriors.module.team.model;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.util.ConcurrentHashSet;

import com.yayo.warriors.module.team.type.AllocateMode;

/**
 * 队伍对象.
 * 
 * @author Hyint
 */
public class Team implements Serializable {
	private static final long serialVersionUID = 4897350321406310285L;

	/** 队伍ID */
	private int id;

	/** 队长ID */
	private long leaderId;
	
	/** 阵法 */
	private int teamMethod;
	
	/** 组队创建时间 */
	private Date createTime = new Date();

	/** 分配模式 */
	private AllocateMode allocateMode = AllocateMode.FREE_TYPE;

	/** 所有队伍成员 ConcurrentHashSet<玩家ID> */
	private ConcurrentHashSet<Long> members = new ConcurrentHashSet<Long>();

	// 自增序列
	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	private Team() {
	}

	/**
	 * 构建组队对象
	 * 
	 * @param  playerId			角色ID
	 * @return {@link Team}		组队对象
	 */
	public static Team valueOf(long playerId) {
		Team team = new Team();
		team.id = getSequence();
		team.leaderId = playerId;
		team.members.add(playerId);
		return team;
	}

	private static int getSequence() {
		int sn = SEQUENCE.incrementAndGet();
		if (sn > 0x2fffffff) { // 超过 1073741823 则开始尝试重置
			SEQUENCE.compareAndSet(sn, 0);
		}
		return sn;
	}

	public int getId() {
		return id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public synchronized long getLeaderId() {
		return leaderId;
	}

	public ConcurrentHashSet<Long> getMembers() {
		return members;
	}

	public void setMembers(ConcurrentHashSet<Long> members) {
		this.members = members;
	}

	public void setLeaderId(long leaderId) {
		this.leaderId = leaderId;
	}

	public AllocateMode getAllocateMode() {
		return allocateMode;
	}

	public void setAllocateMode(AllocateMode allocateMode) {
		this.allocateMode = allocateMode;
	}

	public int getTeamMethod() {
		return teamMethod;
	}

	public void setTeamMethod(int teamMethod) {
		this.teamMethod = teamMethod;
	}

	/**
	 * 队伍是否为空
	 */
	public boolean isEmpty() {
		return members.isEmpty();
	}

	/**
	 * 队伍成员数量
	 */
	public synchronized int size() {
		return members.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Team other = (Team) obj;
		return id == other.id;
	}

	@Override
	public String toString() {
		return "Team [id=" + id + ", leaderId=" + leaderId + ", createTime=" + createTime + ", members=" + members + "]";
	}

	public boolean contains(long playerId) {
		return this.members.contains(playerId);
	}

	public void add(long playerId) {
		this.members.add(playerId);
	}

	public void remove(long playerId) {
		this.members.remove(playerId);
	}
}
