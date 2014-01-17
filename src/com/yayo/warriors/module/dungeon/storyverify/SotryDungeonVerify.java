package com.yayo.warriors.module.dungeon.storyverify;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 基础验证对象 
 * @author liuyuhua
 */
@Component
public class SotryDungeonVerify {
	
	@Autowired
	private UserManager userManager;
	
	/** 验证器集合{类型,验证器处理类}*/
	private static final Map<Integer, VerifyHandler> VERIFY = new HashMap<Integer, VerifyHandler>();
	
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 加入验证器
	 * @param type      类型
	 * @param handler   处理类
	 */
	public void putVerifyHandler(int type,VerifyHandler handler) {
		VERIFY.put(type, handler);
	}

	
	/**
	 * 验证剧情副本条件
	 * @param playerId     玩家的ID 
	 * @param storyVerify  需要验证的类型
	 * @return true 通过 false 反之
	 */
	public boolean verify(long playerId,StoryVerify...storyVerifys){
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null || storyVerifys.length <= 0){
			return false;
		}
		
		for(StoryVerify storyVerify : storyVerifys){
			VerifyHandler handler = VERIFY.get(storyVerify.getType());
			if(handler == null){
				if(logger.isDebugEnabled()){
					logger.debug("玩家[{}],验证剧情副本条件 类型[{}],不存在!",userDomain.getId(),storyVerify.getType());
				}
				return false;
			}
			
			if(!handler.verify(userDomain, storyVerify)){
				if(logger.isDebugEnabled()){
					logger.debug("玩家[{}],验证剧情副本条件 类型[{}],不符合条件!",userDomain.getId(),storyVerify.getType());
				}
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 验证剧情副本条件
	 * @param playerId     玩家的ID 
	 * @param storyVerify  需要验证的类型
	 * @return true 通过 false 反之
	 */
	public boolean verify(long playerId, Collection<StoryVerify> storyVerifys){
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null || storyVerifys == null || storyVerifys.isEmpty()){
			return false;
		}
		
		for(StoryVerify storyVerify : storyVerifys){
			VerifyHandler handler = VERIFY.get(storyVerify.getType());
			if(handler == null){
				if(logger.isDebugEnabled()){
					logger.debug("玩家[{}],验证剧情副本条件 类型[{}],不存在!",userDomain.getId(),storyVerify.getType());
				}
				return false;
			}
			
			if(!handler.verify(userDomain, storyVerify)){
				if(logger.isDebugEnabled()){
					logger.debug("玩家[{}],验证剧情副本条件 类型[{}],不符合条件!",userDomain.getId(),storyVerify.getType());
				}
				return false;
			}
		}
		
		return true;
	}
	
}
