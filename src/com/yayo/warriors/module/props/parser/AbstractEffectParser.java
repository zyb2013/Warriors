package com.yayo.warriors.module.props.parser;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.module.props.manager.PropsManager;

/**
 * 抽象的道具效果解析器
 * 
 * @author Hyint
 */
public abstract class AbstractEffectParser implements PropsParser {
	
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PropsManager itemManager;
	@Autowired
	private PropsParserContext context;

	@PostConstruct
	void init() {
		context.putParser(getType(), this);
	}

	/**
	 * 获取基础道具
	 * 
	 * @param  itemId				用户道具ID
	 * @return {@link PropsConfig}	查询用户道具类型
	 */
	protected PropsConfig getBaseItem(int itemId) {
		return itemManager.getPropsConfig(itemId);
	}

	/**
	 * 道具类型
	 * 
	 * @return {@link Integer}		返回值
	 */
	protected abstract int getType();
}
