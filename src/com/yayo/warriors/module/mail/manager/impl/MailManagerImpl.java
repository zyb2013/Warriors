package com.yayo.warriors.module.mail.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.module.mail.dao.MailDao;
import com.yayo.warriors.module.mail.entity.Mail;
import com.yayo.warriors.module.mail.entity.UserMail;
import com.yayo.warriors.module.mail.manager.MailManager;
import com.yayo.warriors.module.mail.model.MailType;

/**
 * 邮件管理
 * 
 * @author huachaoping
 */
@Service
public class MailManagerImpl extends CachedServiceAdpter implements MailManager {

	/** 日志 */
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MailDao mailDao;
	@Autowired
	private DbService dbService;
	
	/** HashKey */
	private static final String HASH_KEY = "MAIL_";
	/** SubKey */
	private static final String MAIL_TYPE = "MAIL_TYPE";
	
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserMail.class);
	}
	
	
	public void sendEmail(Mail mail, Collection<Long> playerIds) {
		mailDao.save(mail);
		put2EntityCache(mail);
		if(playerIds != null && !playerIds.isEmpty()) {
			for (long playerId : playerIds) {
				UserMail userMail = getUserMail(playerId);
				if (userMail != null) {
					ChainLock lock = LockUtils.getLock(userMail);
					try {
						lock.lock();
						userMail.addMailId(mail.getId());
						dbService.submitUpdate2Queue(userMail);
					} finally {
						lock.unlock();
					}
				}
			}
		}
	}

	/**
	 * 创建基础邮件
	 * 
	 * @param mail
	 */
	
	public void createMail(Mail mail) {
		mailDao.save(mail);
		put2EntityCache(mail);
	}
	
	/**
	 * 查询基础邮件信息
	 * 
	 * @param mailId                 邮件ID
	 * @return {@link Mail}
	 */
	
	public Mail getMail(long mailId) {
		return this.get(mailId, Mail.class);
	}

	
	/**
	 * 查询用户邮件信息
	 * 
	 * @param userMailId             用户邮件ID
	 * @return {@link UserMail}
	 */
	
	public UserMail getUserMail(long playerId) {
		if (playerId <= 0L) {
			return null;
		}
		
		UserMail userMail = this.get(playerId, UserMail.class);
		if(userMail == null) {
			return userMail;
		}

		List<Long> systemMails = this.listMailIds(MailType.SYSTEM);
		if(systemMails == null || systemMails.isEmpty()) {
			return userMail;
		}
		
		ChainLock lock = LockUtils.getLock(userMail);
		try {
			lock.lock();
			List<Long> copyEmailIds = new ArrayList<Long>(systemMails);
			copyEmailIds.removeAll(userMail.getDelMailSet());
			if(copyEmailIds.isEmpty()) {
				return userMail;
			}
			userMail.addMailIds(copyEmailIds);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userMail);
		return userMail;			
	}
	
	
	/**
	 * 用户邮件KEY
	 * 
	 * @param mailType
	 * @return {@link String}
	 */
	private String getUserMailSubKey(int mailType) {
		return new StringBuffer().append(MAIL_TYPE).append(mailType).toString();
	}

	/**
	 * 取得指定邮件类型的邮件ID列表
	 * 
	 * @param  mailType			邮件类型
	 * @return {@link List}		邮件ID列表
	 */
	@SuppressWarnings("unchecked")
	public List<Long> listMailIds(int mailType) {
		String subkey = this.getUserMailSubKey(mailType);
		List<Long> result = (List<Long>) cachedService.getFromCommonCache(HASH_KEY, subkey);
		if (result == null) {
			result = mailDao.listMailIds(mailType);
			cachedService.put2CommonHashCache(HASH_KEY, subkey, result);
		}
		return result;
	}
	
	/**
	 * 从数据库中查询邮件对象
	 * 
	 * @param id
	 * @param clazz
	 */
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if (id != null && clazz == UserMail.class) {
			UserMail userMail = mailDao.get(id, UserMail.class);
			if(userMail != null) {
				return (T) userMail;
			}

			try {
				userMail = UserMail.valueOf((Long) id);
				mailDao.save(userMail);
			} catch (Exception e) {
				userMail = null;
				logger.error("角色:[{}] 创建邮件信息异常:{}", id, e);
			}
			return (T) userMail;
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	
	
	public void removeMailCache(int mailType) {
		cachedService.removeFromCommonHashCache(HASH_KEY, getUserMailSubKey(mailType));
	}

	
}
