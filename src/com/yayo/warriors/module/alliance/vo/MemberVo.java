package com.yayo.warriors.module.alliance.vo;

import java.io.Serializable;

/**
 * 帮派成员显示对象
 * @author liuyuhua
 */
public class MemberVo implements Serializable {
	private static final long serialVersionUID = 2439215883674064512L;
	
	/** 玩家的ID*/
	private long id;
	
	/** 玩家名字*/
	private String name;
	
	/** 职业*/
	private int job;
	
	/** 等级 */
	private int level;
	
	/** 当前贡献值*/
	private int donate;
	
	/** 历史贡献值*/
	private int hisdonate;
	
	/** 职位*/
	private int title;
	
	/** 是否在线*/
	private boolean online;
	

	/**
	 * 构造方法
	 * @param name         玩家的名字
	 * @param job          玩家的职业
	 * @param level        玩家的等级
	 * @param donate       玩家当前对帮派的贡献值
	 * @param hisdonate    玩家对帮派的利是贡献值
	 * @param title        职位
	 * @param online       是否在线
	 * @return {@link MemberVo} 帮派成员显示对象 
	 */
	public static MemberVo valueOf(long id,String name,int job,int level,int donate,int hisdonate,int title,boolean online){
		MemberVo vo = new MemberVo();
		vo.id     = id;
		vo.name   = name;
		vo.job    = job;
		vo.level  = level;
		vo.donate = donate;
		vo.title  = title;
		vo.online = online;
		vo.hisdonate = hisdonate;
		return vo;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}

	public int getHisdonate() {
		return hisdonate;
	}

	public void setHisdonate(int hisdonate) {
		this.hisdonate = hisdonate;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "MemberVo [id=" + id + ", name=" + name + ", job=" + job
				+ ", level=" + level + ", donate=" + donate + ", hisdonate="
				+ hisdonate + ", title=" + title + ", online=" + online + "]";
	}

}
