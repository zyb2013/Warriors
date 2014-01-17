package com.yayo.warriors.module.mail.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;

/**
 * 用户邮件
 * 
 * @author huachaoping
 */
@Entity
@Table(name="userMail")
public class UserMail extends BaseModel<Long> {

	private static final long serialVersionUID = -6789998409922803253L;
	
	/** 主键 */
	@Id
	@Column(name="playerId")
	private Long id;
	
	/** 邮件ID, 格式:邮件ID_邮件ID */
	@Lob
	private String mailIds = "";
	
	/** 是否已领取邮件附件 : 0-未领取, 1-已领取 */
	@Lob
	private String received = "";
	
	/** 邮件状态, 格式:邮件ID_邮件ID */
	@Lob
	private String mailState = "";
	
	/** 已删除邮件: 格式:邮件ID_数量|... 主要记录有附件的邮件 */
	@Lob
	private String delMails = "";
	
	/** 邮件ID列表 */
	@Transient
	private transient Set<Long> mailIdSet = null;
	
	/** 已领取的邮件ID列表 */
	@Transient
	private transient Set<Long> receivedSet = null;
	
	/** 已读取的邮件ID列表 */
	@Transient
	private transient Set<Long> stateSet = null;
	
	/** 删除的邮件列表 */
	@Transient
	private transient Set<Long> delMailSet = null;
	
	
	/**
	 * 格式解析
	 * 
	 * @param format          格式字符串
	 * @param formatSet       缓存
	 * @return {@link Set<Long>}
	 */
	public Set<Long> getCacheSet(String format, Set<Long> formatSet) {
		if (formatSet != null) {
			return formatSet;
		}
		
		synchronized (this) {
			if (formatSet == null) {
				formatSet = Collections.synchronizedSet(new HashSet<Long>());
				if (StringUtils.isBlank(format)) {
					return formatSet;
				}
				
				String[] array = format.split(Splitable.ATTRIBUTE_SPLIT);
				for (String element : array) {
					formatSet.add(Long.valueOf(element));
				}
			}
		}
		return formatSet;
	}
	
	/**
	 * 更新字符串
	 * 
	 * @param formatSet
	 */
	public String updateformat(Set<Long> formatSet) {
		if (formatSet == null || formatSet.isEmpty()) {
			return StringUtils.EMPTY;
		}
		
		StringBuilder builder = new StringBuilder();
		synchronized (formatSet) {
			for (long mailId : formatSet) {
				builder.append(mailId).append(Splitable.ATTRIBUTE_SPLIT);
			}
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getReceived() {
		return received;
	}

	public void setReceived(String received) {
		this.received = received;
	}

	public String getMailState() {
		return mailState;
	}

	public void setMailState(String mailState) {
		this.mailState = mailState;
	}
	
	public String getMailIds() {
		return mailIds;
	}

	public void setMailIds(String mailIds) {
		this.mailIds = mailIds;
	}
	
	public Set<Long> getMailIdSet() {
		mailIdSet = getCacheSet(mailIds, mailIdSet);
		return mailIdSet;
	}

	public void setMailIdSet(Set<Long> mailIdSet) {
		this.mailIdSet = mailIdSet;
	}

	public Set<Long> getReceivedSet() {
		receivedSet = getCacheSet(received, receivedSet);
		return receivedSet;
	}

	public void setReceivedSet(Set<Long> receivedSet) {
		this.receivedSet = receivedSet;
	}

	public Set<Long> getStateSet() {
		stateSet = getCacheSet(mailState, stateSet);
		return stateSet;
	}

	public void setStateSet(Set<Long> stateSet) {
		this.stateSet = stateSet;
	}

	public String getDelMails() {
		return delMails;
	}

	public void setDelMails(String delMails) {
		this.delMails = delMails;
	}
	
	public Set<Long> getDelMailSet() {
		delMailSet = getCacheSet(delMails, delMailSet);
		return delMailSet;
	}

	public void setDelMailSet(Set<Long> delMailSet) {
		this.delMailSet = delMailSet;
	}
	
	
	/**
	 * 更新邮件
	 * @param mailId
	 */
	public void addMailId(long mailId) {
		this.getMailIdSet().add(mailId);
		this.mailIds = this.updateformat(this.mailIdSet);
	}
	
	/**
	 * 批量增加邮件
	 * @param mailIds
	 */
	public void addMailIds(Collection<Long> mailIds) {
		if (mailIds != null && !mailIds.isEmpty()) {
			this.getMailIdSet().addAll(mailIds);
			this.mailIds = this.updateformat(this.mailIdSet);
		}
	}
	
	/**
	 * 更新邮件附件领取状态
	 * @param mailId
	 */
	public void addReceivedMailId(long mailId) {
		this.getReceivedSet().add(mailId);
		this.received = this.updateformat(this.receivedSet);
	}
	
	/**
	 * 更新邮件已读状态
	 * @param mailId
	 */
	public void addReadedMailId(long mailId) {
		this.getStateSet().add(mailId);
		this.mailState = this.updateformat(this.stateSet);
	}
	
	/**
	 * 更新删除状态
	 * @param mailId
	 */
	public void add2DelMails(long mailId) {
		this.getDelMailSet().add(mailId);
		this.delMails = this.updateformat(this.delMailSet);
	}
	
	public void add2DelMails(Collection<Long> mailIds) {
		if (mailIds != null && !mailIds.isEmpty()) {
			this.getDelMailSet().addAll(mailIds);
			this.delMails = this.updateformat(this.delMailSet);
		}
	}
	
	/**
	 * 删除邮件
	 * @param mailId
	 */
	public void removeMail(long mailId) {
		this.getStateSet().remove(mailId);
		this.getMailIdSet().remove(mailId);
		this.getReceivedSet().remove(mailId);
		this.mailIds = this.updateformat(this.mailIdSet);
		this.received = this.updateformat(this.receivedSet);
		this.mailState = this.updateformat(this.stateSet);
	}
	
	/**
	 * 批量删除邮件
	 * @param mailIds
	 */
	public void removeMails(List<Long> mailIds) {
		this.getStateSet().removeAll(mailIds);
		this.getMailIdSet().removeAll(mailIds);
		this.getReceivedSet().removeAll(mailIds);
		this.mailIds = this.updateformat(this.mailIdSet);
		this.received = this.updateformat(this.receivedSet);
		this.mailState = this.updateformat(this.stateSet);
	}
	
	/**
	 * 批量删除邮件
	 * @param mailIds
	 */
	public void removeAllMail() {
		this.getStateSet().clear();
		this.getMailIdSet().clear();
		this.getReceivedSet().clear();
		this.mailIds = this.updateformat(this.mailIdSet);
		this.received = this.updateformat(this.receivedSet);
		this.mailState = this.updateformat(this.stateSet);
	}
	
	
	public static UserMail valueOf(long playerId) {
		UserMail userMail = new UserMail();
		userMail.id = playerId;
		return userMail;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof UserMail))
			return false;
		UserMail other = (UserMail) obj;
		return id != null && other.id != null && id.equals(other.id);
	}

}
