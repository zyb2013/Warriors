package com.yayo.warriors.module.alliance.vo;

import java.io.Serializable;

/**
 * 帮派显示对象
 * <per>当玩家没有见到</per> 
 * @author liuyuhua
 */
public class AllianceVo implements Serializable{
	private static final long serialVersionUID = -5906756684495696680L;
	//频繁创建 transient属性的后果是怎么样的, 你真的了解了吗? 静态定义为什么要用 transient? 优源. 2012年5月16日21:43:43
	/** 可以申请*/
	public final static int OPEN = 0;
	/** 已经申请*/
	public final static int APPLY = 1;
	/** 关闭申请*/
	public final static int CLOSE = 2;
	/** 成员已满*/
	public final static int FULL  = 3; 
	/** 申请者已满*/
	public final static int APPLYFULL = 4;
	/** 已存在帮派*/
	public final static int JOINED = 5;
	
	/** 帮派ID*/
	private long id;
	
	/** 帮派名*/
	private String allianceName;
	
	/** 帮派等级*/
	private int level;

	/** 帮主名字*/
	private String masterName;
	
	/** 人数*/
	private int members;
	
	/** 个人当前申请状态*/
	private int state;
	
	/** 阵营*/
	private int camp;
	
	/** 帮主的ID*/
	private long masterId;
	
	/**
	 * 构造方法
	 * @param id                  帮派ID
	 * @param masterId            帮主的ID
	 * @param allianceName        帮派的名字
	 * @param masterName          帮助的名字
	 * @param level               帮派等级
	 * @param members             帮派成员数量
	 * @param state               状态
	 * @param camp                阵营
	 * @return {@link AllianceVo} 帮派显示对象
	 */
	public static AllianceVo valueOf(long id,long masterId,String allianceName,String masterName,int level,int members,int state,int camp){
		AllianceVo vo = new AllianceVo();
		vo.id           = id;
		vo.masterId     = masterId;
		vo.allianceName = allianceName;
		vo.masterName   = masterName;
		vo.level        = level;
		vo.members      = members;
		vo.state        = state;
		vo.camp         = camp;
		return vo;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAllianceName() {
		return allianceName;
	}

	public void setAllianceName(String name) {
		this.allianceName = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public int getMembers() {
		return members;
	}

	public void setMembers(int members) {
		this.members = members;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public long getMasterId() {
		return masterId;
	}

	public void setMasterId(long masterId) {
		this.masterId = masterId;
	}

	@Override
	public String toString() {
		return "AllianceVo [id=" + id + ", allianceName=" + allianceName
				+ ", level=" + level + ", masterName=" + masterName
				+ ", members=" + members + ", state=" + state + ", camp="
				+ camp + ", masterId=" + masterId + "]";
	}

}
