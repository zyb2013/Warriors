package com.yayo.warriors.module.active.rule;

/**
 * 公告发送类型
 * @author liuyuhua
 */
public interface ActiveNoticeType {
	
	/** 1 - 提前 */
	int BEFORE_ACTIVE = 1;
	
	/** 2 - 活动开始时*/
	int BEGIN_ACTIVE = 2;
	
	/** 3 - 进行中(间隔循环)*/
	int ACTION_LOOP_ACTIVE = 3;
	
	/** 4 - 即将结束(距离结束的时间)*/
	int WILL_CLOSE_ACTIVE = 4;
	
	/** 5 - 已结束*/
	int CLOSED_ACTIVE = 5;
	
}
