package com.yayo.warriors.module.user.vo;

import java.io.Serializable;

/**
 * 当前分线详细信息
 * 
 * @author Hyint
 */
public class BranchingVO implements Serializable {
	private static final long serialVersionUID = -7869714409746456719L;

	/** 分线编号/阵营编号 */
	private int id;
	
	/** 当前分线总人数 */
	private int count;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
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
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		BranchingVO other = (BranchingVO) obj;
		return id == other.id;
	}

	@Override
	public String toString() {
		return "BranchingVO [id=" + id + ", count=" + count + "]";
	}
	
	/**
	 * 构建分线VO对象
	 * 
	 * @param  branchingId			分线ID
	 * @param  count				在线人数
	 * @return {@link BranchingVO}	分先VO对象
	 */
	public static BranchingVO valueOf(int branchingId, int count) {
		BranchingVO branchingVO = new BranchingVO();
		branchingVO.id = branchingId;
		branchingVO.count = count;
		return branchingVO;
	}
}
