package com.yayo.warriors.socket.handler.duntask;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.module.duntask.constant.DungeonTaskConstant;
import com.yayo.warriors.module.duntask.facade.DungeonTaskFacade;
import com.yayo.warriors.module.duntask.model.DunTask;
import com.yayo.warriors.module.duntask.util.DunTaskFactory;
import com.yayo.warriors.module.duntask.vo.DunTaskVo;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import flex.messaging.io.amf.ASObject;

@Component
public class DunTaskHandler extends BaseHandler{

	@Autowired
	private DungeonTaskFacade dungeonTaskFacade;
	
	@Autowired
	private SessionManager sessionManager;
	
	
	protected int getModule() {
		return Module.DUNGEONTASK;
	}

	
	protected void inititialize() {
		
		putInvoker(DunTaskCmd.LOAD_DUNTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadDuntask(session, request, response);
			}
		});
		
		putInvoker(DunTaskCmd.SUBMIT_DUNTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				submitTask(session, request, response);
			}
		});
	}
	
	
	protected void submitTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long taskId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = dungeonTaskFacade.submit(playerId, taskId);
		if(result == DungeonTaskConstant.SUCCESS){
			response.setValue(taskId);
		}else{
			response.setValue(result);
		}
		
		session.write(response);
		
	}
	

	protected void loadDuntask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Collection<DunTask> duntasks = dungeonTaskFacade.getAllDunTask(playerId);
		
		if(duntasks == null){
			response.setValue(new DunTaskVo[0]);
			session.write(response);
			return;
		}
		
		List<DunTaskVo> vos = DunTaskFactory.buildDunTask4Array(duntasks);
		if(vos != null){
			response.setValue(vos.toArray());
		}else{
			response.setValue(new DunTaskVo[0]);
		}
		session.write(response);
	}

}
