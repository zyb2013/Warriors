package com.yayo.warriors.event;

import static com.yayo.warriors.module.achieve.model.AchieveType.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.AbstractReceiver;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.logger.log.LoginLogger;
import com.yayo.warriors.module.server.facade.ContainerFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 用户登陆事件
 * 
 * @author hyint
 */
@Component
public class LoginEventReceiver extends AbstractReceiver<LoginEvent> {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ContainerFacade containerFacade;
	
	@Override
	public String[] getEventNames() {
		return new String[] { LoginEvent.NAME };
	}

	@Override
	public void doEvent(LoginEvent event) {
		long playerId = event.getOwnerId();
		int branching = event.getBranching();
		String clientIp = event.getClientIp();
		containerFacade.cancelDataRemoveScheduleTask(playerId);
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain != null) {
			Player player = userDomain.getPlayer();		// 角色对象
			containerFacade.onLoginListener(userDomain, branching);
			LoginLogger.login(player, userDomain.getBattle(), clientIp, branching);
		}
	}
}
