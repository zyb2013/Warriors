package com.yayo.warriors.socket.handler.rank;

import static com.yayo.common.socket.type.ResponseCode.RESPONSE_CODE_ERROR;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.rank.facade.RankFacade;
import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.rank.vo.RankVO;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class RankHandler extends BaseHandler {
	@Autowired
	private RankFacade rankFacade;

	
	protected int getModule() {
		return Module.RANK;
	}

	
	protected void inititialize() {
		putInvoker(RankCmd.GET_RANKINFO_BY_TYPE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getRankInfo(session, request, response, false);
			}
		});
		
		putInvoker(RankCmd.SEARCH_PLAYER_RANK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getRankInfo(session, request, response, true);
			}
		});
	}

	private void getRankInfo(IoSession session, Request request, Response response, boolean isSearch) {
		Long playerId = sessionManager.getPlayerId(session);
		RankType rankType = null;
		Job job = Job.COMMON;
		int pageSize = 0;
		int pageNow = 0;
		String playerName = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TYPE)) {
				int type = ((Number) aso.get(ResponseKey.TYPE)).intValue();
				rankType = EnumUtils.getEnum(RankType.class, type);
			}
			if (aso.containsKey(ResponseKey.JOB)) {
				int jt = ((Number) aso.get(ResponseKey.JOB)).intValue();
				job = EnumUtils.getEnum(Job.class, jt);
			}
			if (aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number) aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			if (aso.containsKey(ResponseKey.PAGE_NOW)) {
				pageNow = ((Number) aso.get(ResponseKey.PAGE_NOW)).intValue();
			}
			if(isSearch){
				if( aso.containsKey(ResponseKey.PLAYER_NAME) ) {
					playerName = (String) aso.get(ResponseKey.PLAYER_NAME);
				}
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<RankVO> resultObject = null;
		if(isSearch){	
			resultObject = rankFacade.getRankVOByPlayerName(playerId, playerName, rankType, job, pageSize);
		} else {		
			resultObject = rankFacade.getRankVO(playerId, rankType, job, pageSize, pageNow);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put( ResponseKey.RESULT, resultObject.getResult() );
		resultMap.put( ResponseKey.CONTENT, resultObject.getValue() );
		response.setValue( resultMap );
		
		session.write( response );
	}

}
