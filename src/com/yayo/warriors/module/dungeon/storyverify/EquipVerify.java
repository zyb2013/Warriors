package com.yayo.warriors.module.dungeon.storyverify;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 装备验证 
 * @author liuyuhua
 */
@Component
public class EquipVerify implements VerifyHandler{

	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private PropsManager propsManager;
	
	
	public int getType() {
		return StoryVerifType.EQUIP_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		Collection<Long> equipIdList = propsManager.getUserEquipIdList(userDomain.getId(), storyVerify.getParam2());
		if(equipIdList == null || equipIdList.isEmpty()){
			return false;
		}
		
		for(long equipId : equipIdList){
			UserEquip userEquip = propsManager.getUserEquip(equipId);
			if(userEquip.getBaseId() == storyVerify.getParam1()){
				return true;
			}
		}
		
		return false;
	}

	
	@PostConstruct
	public void registerVerify() {
		dungeonVerify.putVerifyHandler(getType(), this);
	}

}
