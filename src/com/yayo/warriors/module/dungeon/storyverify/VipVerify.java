package com.yayo.warriors.module.dungeon.storyverify;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;

@Component
public class VipVerify implements VerifyHandler{

	@Autowired
	private VipManager vipManager;
	
	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	
	public int getType() {
		return StoryVerifType.VIP_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		VipDomain vipDomain = vipManager.getVip(userDomain.getId());
		if(vipDomain == null || !vipDomain.isVip()){
			return false;
		}
		
		if(vipDomain.vipLevel() >= storyVerify.getParam1()){
			return true;
		}
		
		return false;
	}

	
	@PostConstruct
	public void registerVerify() {
		dungeonVerify.putVerifyHandler(getType(), this);
	}

}
