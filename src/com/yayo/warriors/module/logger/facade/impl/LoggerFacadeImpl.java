package com.yayo.warriors.module.logger.facade.impl;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yayo.common.scheduling.Scheduled;
import com.yayo.warriors.module.logger.facade.LoggerFacade;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;

/**
 * 日志Facade接口
 * 
 * @author Hyint
 */
@Component
public class LoggerFacadeImpl implements LoggerFacade {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 定时写空行到日志文件中
	 */
	@Scheduled(name = "定时写空行到日志文件中", value = "0 0 0 * * ?")
	protected void ontimeWrite2Logger() {
		writeBlank2Log();
	}
	
	
	public void reflushLogger() {
		for(LogType logType : LogType.values()) {
			try {
				String logName = logType.getLogName();
				org.apache.log4j.Logger log = LogManager.getLogger(logName);
				if(log == null) {
					continue;
				}
				
				@SuppressWarnings("unchecked")
				Enumeration<Appender> allAppenders = log.getAllAppenders();
				if(allAppenders == null) {
					continue;
				}
				
				while(allAppenders.hasMoreElements()){
					Appender appender = allAppenders.nextElement();
					if(appender instanceof FileAppender){
						FileAppender fileAppender = (FileAppender) appender;
						fileAppender.setBufferedIO(false);
						fileAppender.setImmediateFlush(true);
						log.info("");
					}
				}
			} catch (Exception e) {
				logger.error("{}", e);
			}
		}
	}

	/**
	 * 写空行到日志文件中
	 * 
	 * @param logType	日志类型
	 */
	private void writeBlank2Log() {
		for (LogType logType : LogType.values()) {
			TraceLog.doLogger(logType, "");
		}
	}
}
