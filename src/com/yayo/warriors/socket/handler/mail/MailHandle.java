package com.yayo.warriors.socket.handler.mail;

import static com.yayo.common.socket.type.ResponseCode.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.module.mail.facade.MailFacade;
import com.yayo.warriors.module.mail.vo.MailVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.util.ParamUtils;

import flex.messaging.io.amf.ASObject;

@Component
public class MailHandle extends BaseHandler {
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MailFacade mailFacade;
	
	
	protected int getModule() {
		return Module.MAIL;
	}

	
	protected void inititialize() {
		
		putInvoker(MailCmd.LOAD_MAILS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadMails(session, request, response);
			}
		});
		
		putInvoker(MailCmd.DELETE_MAILS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				delMails(session, request, response);
			}
		});
		
		putInvoker(MailCmd.READ_MAIL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				readMail(session, request, response);
			}
		});
		
		putInvoker(MailCmd.RECEIVE_MAIL_REWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				receiveMailReward(session, request, response);
			}
		});
	}
	
	
	protected void loadMails(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Collection<MailVO> voList = mailFacade.listUserMails(playerId);
		response.setValue(voList.toArray());
		session.write(response);
	}
	
	protected void delMails(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<Long> mailIds = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.MAIL_IDS)) {
				Object[] objects = ParamUtils.getParameter(aso, ResponseKey.MAIL_IDS, Object[].class);
				mailIds = new ArrayList<Long>();
				if (objects != null) {
					for (Object obj : objects) {
						mailIds.add(Long.valueOf(obj.toString()));
					}
				}
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = mailFacade.deleteMail(playerId, mailIds);
		response.setValue(result);
		session.write(response);
	}
	
	protected void readMail(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long mailId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.MAIL_ID)) {
				mailId = ((Number) aso.get(ResponseKey.MAIL_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = mailFacade.readMail(playerId, mailId);
		response.setValue(result);
		session.write(response);
	}
	
	protected void receiveMailReward(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long mailId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.MAIL_ID)) {
				mailId = ((Number) aso.get(ResponseKey.MAIL_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = mailFacade.receiveMailReward(playerId, mailId);
		response.setValue(result);
		session.write(response);
	}

	
}
