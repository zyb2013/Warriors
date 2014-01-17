package com.yayo.warriors.module.server.listener;

import com.yayo.common.socket.delay.MessageInfo;


public interface DataRemoveListener extends Listener {
	void onDataRemoveEvent(MessageInfo messageInfo);
}
