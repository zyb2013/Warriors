package com.yayo.warriors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.scheduling.Scheduled;
import com.yayo.warriors.basedb.adapter.OnlineActiveService;
import com.yayo.warriors.module.buffer.facade.BufferFacade;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;

/**
 * 全服线程统一接口
 * 
 * @author Hyint
 */
@Component
public class Bootstrap {

	@Autowired
	private BufferFacade bufferFacade;
	@Autowired
	private OnlineActiveService onlineActiveService;
	@Autowired
	private DungeonManager dungeonManager;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/** 启动定时任务 */
	@Scheduled(name="SERVER_ONTIME_SERVER", value="*/2 * * * * *")
	protected void startScheduleTask() {
		try {
			bufferFacade.processBufferScheduling();
		} catch (Exception e) {
			logger.error("{}", e);
		}
		
		try {
			onlineActiveService.processActiveRuleScheduling();//在线玩法怪物
		} catch (Exception e) {
			logger.error("{}", e);
		}
		
		try {
			dungeonManager.processDungeonRule();//在线玩法怪物
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
	
}
