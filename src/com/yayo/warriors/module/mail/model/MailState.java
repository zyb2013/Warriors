package com.yayo.warriors.module.mail.model;

import java.util.Date;

import com.yayo.common.utility.DateUtil;

/**
 * 邮件状态
 * 
 * @author huachaoping
 */
public enum MailState {
	
	/** 未读 */
	UNREAD(15, new DeadlineCtrl() {
		
		public boolean isOutOfDate(Date sendTime) {
			int deadline = UNREAD.getDeadline();
			Date outOfDate = DateUtil.changeDateTime(sendTime, deadline, 0, 0, 0);
			return new Date().after(outOfDate);
		}
	}),
	
	/** 已读 */
	READED(3, new DeadlineCtrl() {
		
		public boolean isOutOfDate(Date sendTime) {
			int deadline = READED.getDeadline();
			Date outOfDate = DateUtil.changeDateTime(sendTime, deadline, 0, 0, 0);
			return new Date().after(outOfDate);
		}
	}),
	
	/** 已删除 */
	DELETED(0, new DeadlineCtrl() {
		
		public boolean isOutOfDate(Date sendTime) {
			return true;
		}
	}),
	
	;
	
	/** 保存期限:天 */
	private int deadline;
	/** 邮件期限控制器 */
	private DeadlineCtrl deadlineCtrl;
	
	
	private MailState(int deadline, DeadlineCtrl ctrl) {
		this.deadline = deadline;
		this.deadlineCtrl = ctrl;
	}

	public int getDeadline() {
		return deadline;
	}
	
	public boolean isOutOfDate(Date sendTime) {
		return deadlineCtrl.isOutOfDate(sendTime);
	}
	
	
	interface DeadlineCtrl {
		
		/**
		 * 邮件是否过期
		 * 
		 * @param sendTime
		 * @return {@link Boolean}
		 */
		public boolean isOutOfDate(Date sendTime);
		
	}
	
}
