package com.yayo.warriors.module.props.parser;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * 道具效果解析上下文
 * 
 * @author Hyint
 */
@Component
public class PropsParserContext {

	/**
	 * 根据道具类型注册的解析器集合
	 */
	private Map<Integer, PropsParser> parsers = new HashMap<Integer, PropsParser>(5);

	/**
	 * 获取已注册的道具效果处理器
	 * 
	 * @param  type 				道具类型
	 * @return {@link PropsParser}	道具效果处理器
	 */
	public PropsParser getParser(int type) {
		return parsers.get(type);
	}

	/**
	 * 注册道具效果处理器
	 * 
	 * @param type 					道具类型
	 * @param parser 				道具效果处理器
	 */
	public void putParser(int type, PropsParser parser) {
		parsers.put(type, parser);
	}

}
