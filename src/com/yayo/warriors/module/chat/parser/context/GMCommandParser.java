package com.yayo.warriors.module.chat.parser.context;

import com.yayo.warriors.module.user.model.UserDomain;

 

/**
 * GM命令处理器
 * 
 * @author Hyint
 */
public interface GMCommandParser {
	
	/**
	 * 命令执行器
	 * 
	 * @param  userDomain		用户域模型
	 * @param  elements			命令执行器
	 * @return {@link Boolean}	执行是否成功
	 */
	boolean execute(UserDomain userDomain, String[] elements);
	
}
