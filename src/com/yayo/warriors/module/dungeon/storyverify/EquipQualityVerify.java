package com.yayo.warriors.module.dungeon.storyverify;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 身上装备的品质,验证
 * @author liuyuhua
 */
@Component
public class EquipQualityVerify implements VerifyHandler{

	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private PropsManager propsManager;
	
	
	public int getType() {
		return StoryVerifType.EQUIP_QUALITY_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		Collection<Long> equipIds = propsManager.getUserEquipIdList(userDomain.getId(), BackpackType.DRESSED_BACKPACK);
		if(equipIds == null || equipIds.isEmpty()){
			return false;
		}
		
		int totleNumber = 0;
		for(Long equipId : equipIds){
			UserEquip userEquip = propsManager.getUserEquip(equipId);
			if(userEquip == null){
				continue;
			}
			
			if(userEquip.getQuality().ordinal() >= storyVerify.getParam1()){
				totleNumber += 1;
			}
		}
		
		if(totleNumber < storyVerify.getParam2()){
			return false;
		}
		
		return true;
	}

	
	@PostConstruct
	public void registerVerify() {
		dungeonVerify.putVerifyHandler(getType(), this);
	}

}
