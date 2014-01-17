package com.yayo.warriors.module.team.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * 组队的VO对象
 * 
 * @author hyint
 */
public class TeamVO implements Serializable {
	private static final long serialVersionUID = 4503822524042605581L;
	
	/** 队伍 ID */
	private int id;
	
	/** 队长 ID */
	private long leaderId;
	
	/** 阵法. 可能后面会改名 */
	private int teamMethod;
	
	/** 分配模式 */
	private int allocateType;
	
	/** 成员对象 */
	private MemberVO[] memberVO;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(long leaderId) {
		this.leaderId = leaderId;
	}

	public MemberVO[] getMemberVO() {
		return memberVO;
	}

	public void setMemberVO(MemberVO[] memberVO) {
		this.memberVO = memberVO;
	}
	
	public int getAllocateType() {
		return allocateType;
	}

	public void setAllocateType(int allocateType) {
		this.allocateType = allocateType;
	}

	public int getTeamMethod() {
		return teamMethod;
	}

	public void setTeamMethod(int teamMethod) {
		this.teamMethod = teamMethod;
	}

	public static TeamVO valueOf(Team team, Collection<MemberVO> memberVOList) {
		TeamVO teamVO = new TeamVO();
		teamVO.id = team.getId();
		teamVO.leaderId = team.getLeaderId();
		teamVO.teamMethod = team.getTeamMethod();
		teamVO.allocateType = team.getAllocateMode().ordinal();
		teamVO.memberVO = memberVOList == null ? new MemberVO[0] : memberVOList.toArray(new MemberVO[memberVOList.size()]);
		return teamVO;
	}
	
}
