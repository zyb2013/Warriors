package com.yayo.warriors.module.mail.manager;

import java.util.Collection;

import com.yayo.warriors.module.mail.entity.Mail;
import com.yayo.warriors.module.mail.entity.UserMail;
import com.yayo.warriors.module.server.listener.DataRemoveListener;

/**
 * 邮件管理
 * 
 * @author huachaoping
 */
public interface MailManager extends DataRemoveListener {
	
	/**
	 * 创建基础邮件
	 * 
	 * @param mail
	 */
	void createMail(Mail mail);
	
	/**
	 * 发送邮件
	 * 
	 * @param mail			邮件实体
	 * @param playerIds		接收邮件的角色ID列表, 没有则置空
	 */
	void sendEmail(Mail mail, Collection<Long> playerIds);
	
	/**
	 * 查询基础邮件信息
	 * 
	 * @param mailId                 邮件ID
	 * @return {@link Mail}
	 */
	Mail getMail(long mailId);
	
	/**
	 * 查询用户邮件信息
	 * 
	 * @param playerId               用户自增ID
	 * @return {@link UserMail}
	 */
	UserMail getUserMail(long playerId);
	
	/**
	 * 移除邮件缓存
	 * 
	 * @param mailType               邮件类型
	 */
	void removeMailCache(int mailType);
	
}
