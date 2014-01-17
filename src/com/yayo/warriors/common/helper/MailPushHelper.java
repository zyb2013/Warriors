package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.mail.entity.Mail;
import com.yayo.warriors.module.mail.vo.MailVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.mail.MailCmd;

/**
 * 邮件推送
 * 
 * @author huachaoping
 */
@Component
public class MailPushHelper {
	
	@Autowired
	private SessionManager sessionManager;
	
	private static ObjectReference<MailPushHelper> ref = new ObjectReference<MailPushHelper>();
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static MailPushHelper getInstance() {
		return ref.get();
	}
	
	
	/**
	 * 发邮件
	 * 
	 * @param baseMailVO
	 * @param playerId
	 */
	public static void sendMail(Mail mail, Collection<Long> playerIds) {
		MailVO mailVO = MailVO.valueOf(mail.getId(), 0, 0, mail);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.VALUES,  mailVO);
		Response response = Response.defaultResponse(Module.MAIL, MailCmd.PUSH_MAIL, resultMap);
		getInstance().sessionManager.write(playerIds, response);
	}
	
}
