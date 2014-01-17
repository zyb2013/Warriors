package com.yayo.warriors.module.server.listener;

import com.yayo.warriors.module.user.model.UserDomain;


public interface LoginListener extends Listener {

	void onLoginEvent(UserDomain userDomain, int branching);
}
