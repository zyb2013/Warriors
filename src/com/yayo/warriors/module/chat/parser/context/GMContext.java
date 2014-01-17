package com.yayo.warriors.module.chat.parser.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * GMParser解析上下文
 * 
 * @author Hyint
 */
@Component
public class GMContext {

	/**
	 * 根据战斗类型注册的解析器集合
	 */
	private Map<String, GMCommandParser> parsers = new HashMap<String, GMCommandParser>(1);

	/**
	 * 获取已注册的命令处理器
	 * 
	 * @param  command					命令
	 * @return {@link GMCommandParser}	战斗技能解析器
	 */
	public GMCommandParser getParser(String command) {
		return parsers.get(command.toLowerCase());
	}

	/**
	 * 注册命令解析器
	 * 
	 * @param  command					命令
	 * @param  parser 					效果处理器
	 */
	public void putParser(String command, GMCommandParser parser) {
		parsers.put(command.toLowerCase(), parser);
	}
	 

}
