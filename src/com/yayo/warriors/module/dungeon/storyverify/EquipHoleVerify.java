package com.yayo.warriors.module.dungeon.storyverify;

import java.util.Collection;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.HoleInfo;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 穿着在身上的装备 打孔宝石验证 
 * @author liuyuhua
 */
@Component
public class EquipHoleVerify implements VerifyHandler{

	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private PropsManager propsManager;
	
	
	public int getType() {
		return StoryVerifType.EQUIP_HOLE_TYPE;
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
			
			for(Entry<Integer, HoleInfo> entry : userEquip.getHoleInfos().entrySet()){
			     HoleInfo hole = entry.getValue();
			     PropsConfig propsConfig = propsManager.getPropsConfig(hole.getItemId());
			     if(propsConfig != null){
			    	 if(storyVerify.getParam1() >= propsConfig.getQuality()){
			    		 totleNumber += 1;
			    	 }
			     }
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
