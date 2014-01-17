package com.yayo.warriors.module.active.vo;

import java.io.Serializable;

/**
 * 
 * 获得任务实体VO对象. 该对象主要是返回给客户端做显示
 * 
 * @author huachaoping
 */
public class ActiveTaskVO implements Serializable {

	private static final long serialVersionUID = 6318546578583702210L;

	/** 活动Id */
	private int id;

	/** 当日次数 */
	private int count;

	/** 是否已接了该任务 */
	private boolean accepted;

	public static ActiveTaskVO valueOf(int id, int count, boolean accepted) {
		ActiveTaskVO vo = new ActiveTaskVO();
		vo.id = id;
		vo.count = count;
		vo.accepted = accepted;
		return vo;
	}

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

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	@Override
	public String toString() {
		return "ActiveTaskVO [id=" + id + ", count=" + count + ", accepted=" + accepted + "]";
	}

}
