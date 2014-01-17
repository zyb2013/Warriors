package com.yayo.warriors.module.fight.parsers.combat.context;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yayo.warriors.module.user.manager.UserManager;

/**
 * 抽象的战斗解析器
 * 
 * @author Hyint
 */
public abstract class AbstractFightParser implements FightParser {
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected FightParserContext context;

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	@PostConstruct
	void init() {
		context.putParser(getType(), this);
	}

	/**
	 * 战斗类型
	 * 
	 * @return {@link String}		返回值
	 */
	protected abstract String getType();
}
