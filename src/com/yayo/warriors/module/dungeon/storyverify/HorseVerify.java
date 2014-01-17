package com.yayo.warriors.module.dungeon.storyverify;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 坐骑验证
 * @author liuyuhua
 */
@Component
public class HorseVerify implements VerifyHandler{

	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private HorseManager horseManager;
	
	
	public int getType() {
		return StoryVerifType.HORSE_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		if(horse == null || horse.getLevel() < storyVerify.getParam1()){
			return false;
		}
		
		return true;
	}

	
	@PostConstruct
	public void registerVerify() {
		dungeonVerify.putVerifyHandler(getType(), this);
	}

}
