package com.yayo.warriors.module.dungeon.storyverify;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 前置剧情副本验证
 * @author liuyuhua
 */
@Component
public class PerSotryVerify implements VerifyHandler{

	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private DungeonManager dungeonManager;
	
	
	public int getType() {
		return StoryVerifType.PRE_DUNGEON_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(userDomain.getId());
		if(playerDungeon == null){
			return false;
		}
		
		return playerDungeon.isCompleteOrFinishStory(storyVerify.getParam1());
	}

	
	@PostConstruct
	public void registerVerify() {
		this.dungeonVerify.putVerifyHandler(getType(), this);
	}

}
