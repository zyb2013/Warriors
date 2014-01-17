package com.yayo.warriors.module.mail.model;

/**
 * 邮件类型
 * 
 * @author huachaoping
 */
public interface MailType {
	
	/** 全服邮件 */
	int SYSTEM = 1;
	
	/** 阵营邮件 */
	int CAMP = 2;
	
	/** 工会邮件 */
	int ALLIANCE = 3;
	
	/** 个人邮件 */
	int PLAYER = 4;
	
	/** 发给男的或女的 */ 
	int SEX = 5;
	
}
