package com.yayo.warriors.module.mail.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;

/**
 * 邮件DAO
 * 
 * @author huachaoping
 */
public interface MailDao extends CommonDao {
	
	/**
	 * 获得全服邮件
	 * 
	 * @param mailType         邮件类型
	 * @return {@link List}
	 */
	List<Long> listMailIds(int mailType);
}
