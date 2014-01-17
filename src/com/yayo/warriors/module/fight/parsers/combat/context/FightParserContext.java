package com.yayo.warriors.module.fight.parsers.combat.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yayo.common.utility.Splitable;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗类型解析上下文
 * 
 * @author Hyint
 */
@Component
public class FightParserContext {

	/**
	 * 根据战斗类型注册的解析器集合
	 */
	private Map<String, FightParser> parsers = new HashMap<String, FightParser>(1);

	/**
	 * 获取已注册的战斗处理器
	 * 
	 * @param  type 				战斗状态类型
	 * @return {@link FightParser}	战斗技能解析器
	 */
	public FightParser getParser(ElementType attack, ElementType target) {
		return parsers.get(toType(attack, target));
	}

	/**
	 * 注册技能解析器
	 * 
	 * @param type 					战斗类型
	 * @param parser 				效果处理器
	 */
	public void putParser(String type, FightParser parser) {
		parsers.put(type, parser);
	}
	
	/**
	 * 构建解析器类型
	 * 
	 * @param  attack				攻击者类型
	 * @param  target				被攻击者类型
	 * @return {@link String}		处理器Key
	 */
	public static String toType(ElementType attack, ElementType target) {
		return new StringBuffer().append(attack).append(Splitable.ATTRIBUTE_SPLIT).append(target).toString();
	}

}
