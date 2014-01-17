package com.yayo.warriors.module.dungeon.storyverify;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 等级验证
 * @author liuyuhua
 */
@Component
public class LevelVerifyHandler implements VerifyHandler{
	
	@Autowired
	private SotryDungeonVerify dungeonVerify;

	
	
	public int getType() {
		return StoryVerifType.LEVEL_TYPE;
	}
	
	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		if(userDomain.getBattle().getLevel() < storyVerify.getParam1()){
			return false;
		}
		
		return true;
	}

	
	@PostConstruct
	public void registerVerify() {
		dungeonVerify.putVerifyHandler(getType(), this);
	}


}
