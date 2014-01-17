package com.yayo.warriors.module.chat.parser.context;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yayo.common.db.executor.DbService;
import com.yayo.warriors.common.helper.VOFactory;

/**
 * 抽象的命令解析器
 * 
 * @author Hyint
 */
public abstract class AbstractGMCommandParser implements GMCommandParser {
	@Autowired
	protected GMContext context;
	@Autowired
	protected DbService dbService;
	@Autowired
	protected VOFactory voFactory;
	
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@PostConstruct
	void init() {
		context.putParser(getCommand(), this);
	}

	/**
	 * 获得命令类型
	 * 
	 * @return {@link String}		返回值
	 */
	protected abstract String getCommand();
}
