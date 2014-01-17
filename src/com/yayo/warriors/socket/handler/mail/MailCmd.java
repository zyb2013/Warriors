package com.yayo.warriors.socket.handler.mail;

import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.mail.vo.MailVO;

public interface MailCmd {
	
	int LOAD_MAILS = 1;
	int DELETE_MAILS = 2;
	int RECEIVE_MAIL_REWARD = 3;
	int READ_MAIL = 4;
	int PUSH_MAIL = 100;
}
