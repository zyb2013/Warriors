package com.yayo.warriors.socket.handler.duntask;

import com.yayo.warriors.module.duntask.vo.DunTaskVo;

public interface DunTaskCmd {
	
	int LOAD_DUNTASK = 1;
	int SUBMIT_DUNTASK = 2;
	int PUT_ACCEPT_DUNTASK = 100;
	int PUT_UPDATE_PROGRESS = 101;
}

