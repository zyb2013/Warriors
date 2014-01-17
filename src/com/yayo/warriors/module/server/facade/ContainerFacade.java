package com.yayo.warriors.module.server.facade;

import com.yayo.warriors.module.user.model.UserDomain;

public interface ContainerFacade {
	void cancelDataRemoveScheduleTask(long playerId);
	void onLogoutUpdateListener(long playerId, String remoteIp);
	void onLoginListener(UserDomain userDomain, int branching);
}
