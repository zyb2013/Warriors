package com.yayo.warriors.module.mail.vo;

import java.io.Serializable;
import java.util.List;

import com.yayo.warriors.module.gift.model.GiftRewardInfo;
import com.yayo.warriors.module.mail.entity.Mail;

/**
 * 邮件VO
 * 
 * @author huachaoping
 */
public class MailVO implements Serializable {

	private static final long serialVersionUID = 6190672696783702798L;
	
	/** 邮件ID */
	private long userMailId;
	
	/** 发送方ID(官方发送为-1) */
	private long senderId;
	
	/** 邮件标题 */
	private String title = "";
	
	/** 邮件内容 */
	private String content = "";
	
	/** 发送时间 */
	private long sendTime;
	
	/** 附件结束时间 */
	private long endTime;

	/** 邮件状态: 0-未读, 1-已读 */
	private int state;
	
	/** 附件领取信息: 0-未领取, 1-已领取 */
	private int received;
	
	/** 附件信息 */
	private Object[] mailRewad;
	

	public static MailVO valueOf(long useMailId, int state, int received, Mail mail) {
		MailVO mailVO = new MailVO();
		mailVO.userMailId = useMailId;
		mailVO.state = state;
		mailVO.received = received;
		mailVO.senderId = mail.getSenderId();
		mailVO.title = mail.getTitle();
		mailVO.content = mail.getContent();
		mailVO.sendTime = mail.getSendTime().getTime();
		mailVO.endTime = mail.getEndTime().getTime();
		List<GiftRewardInfo> rewards = mail.getGiftRewardInfos();
		if (mail.getGoldenRewards() > 0) {
			rewards.add(GiftRewardInfo.goldenReward((int)mail.getGoldenRewards()));
		}
		if (mail.getSilverRewards() > 0) {
			rewards.add(GiftRewardInfo.silverReward((int)mail.getSilverRewards()));
		}
		if (mail.getCouponRewards() > 0) {
			rewards.add(GiftRewardInfo.couponReward((int)mail.getCouponRewards()));
		}
		mailVO.mailRewad = rewards.toArray();
		return mailVO;
	}
	

	public long getUserMailId() {
		return userMailId;
	}

	public void setUserMailId(long mailId) {
		this.userMailId = mailId;
	}

	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Object[] getMailRewad() {
		return mailRewad;
	}

	public void setMailRewad(Object[] mailRewad) {
		this.mailRewad = mailRewad;
	}
	
	public int getReceived() {
		return received;
	}

	public void setReceived(int received) {
		this.received = received;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
}
