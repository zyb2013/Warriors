package com.yayo.warriors.module.chat.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.facade.UserFacade;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.LoginResult;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.vo.LoginVO;


@Component
public class AdminParser extends AbstractGMCommandParser {
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserFacade userFacade;
	
	
	protected String getCommand() {
		return GmType.ADMIN;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long blockUpTime = 100;
		int type = Integer.valueOf( elements[2].trim() );
		String userName = elements[3].trim();
		LoginResult loginResult = userFacade.getLoginResult(userName);
		List<LoginVO> loginVoList = loginResult.getLoginVoList();
		Collection<Long> playerIds = null;
		if(loginVoList != null && loginVoList.size() > 0){
		}
		LoginVO loginVO = loginVoList.get(0);
		Long playerId = loginVO.getId();
		playerIds = Arrays.asList( playerId );
		
		switch (type) {
		case 0:
			break;
		default:
			break;
		}
		
		return true;
	}

}
