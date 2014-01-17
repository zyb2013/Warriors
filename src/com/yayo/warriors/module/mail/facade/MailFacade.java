package com.yayo.warriors.module.mail.facade;

import java.util.List;

import com.yayo.warriors.module.mail.vo.MailVO;


public interface MailFacade {
	List<MailVO> listUserMails(long playerId);
	int deleteMail(long playerId, List<Long> mailIds);
	int receiveMailReward(long playerId, long mailId);
	boolean sendMail(List<Long> playerIds);
	int readMail(long playerId, long mailId);
}
