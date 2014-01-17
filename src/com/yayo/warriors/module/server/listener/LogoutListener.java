package com.yayo.warriors.module.server.listener;

import com.yayo.warriors.module.user.model.UserDomain;


public interface LogoutListener extends Listener {
	
	void onLogoutEvent(UserDomain userDomain);
}
