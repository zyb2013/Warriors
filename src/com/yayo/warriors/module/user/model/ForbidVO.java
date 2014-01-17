package com.yayo.warriors.module.user.model;

import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;

/**
 * 封禁对象
 * 
 * @author Hyint
 */
public class ForbidVO {

	/** 正常情况 */
	public static final int NORMAL = 0;
	/** 封禁情况 */
	public static final int FORBID = 1;
	
	/** 封禁状态. */
	private int forbid;
	
	/** 封禁的结束时间(单位: 毫秒) */
	private long endTime;

	public int getForbid() {
		return forbid;
	}

	public void setForbid(int forbid) {
		this.forbid = forbid;
	}

	/** 是否被封禁 */
	public synchronized boolean isForbidden() {
		if(this.forbid == NORMAL) {
			return false;
		}
		
		//禁言中. 判断是否超过禁言时间, 超过则将时间设置为0
		long endTimeSecond = DateUtil.toSecond(this.endTime);
		long currSecond = DateUtil.toSecond(System.currentTimeMillis());
		if(endTimeSecond > currSecond) {
			return true;
		}

		this.endTime = 0L;
		this.forbid = NORMAL;
		return false;
	}
	
	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * 构建 {@link ForbidVO} 聊天对象
	 * 
	 * @param  forbid				封禁类型
	 * @param  endTime				封禁结束时间(单位: 毫秒)
	 * @return {@link ForbidVO}		封禁聊天状态
	 */
	public static ForbidVO valueOf(int forbid, long endTime) {
		ForbidVO forbidChat = new ForbidVO();
		forbidChat.forbid = forbid;
		forbidChat.endTime = endTime;
		return forbidChat;
	}

	@Override
	public String toString() {
		return forbid + Splitable.ATTRIBUTE_SPLIT + endTime;
	}
	
}
