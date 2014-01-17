package com.yayo.warriors.module.dungeon.storyverify;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 道具验证 
 * @author liuyuhua
 */
@Component
public class PropsVerify implements VerifyHandler{

	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private PropsManager propsManager;
	
	
	public int getType() {
		return StoryVerifType.PROPS_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		Collection<Long> propsIds = propsManager.getUserPropsIdList(userDomain.getId(), BackpackType.DEFAULT_BACKPACK);
		int totleNumber = 0;
		for(long userPropsId : propsIds) {
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if(userProps == null){
				continue;
			}
			
			if(userProps.getBaseId() == storyVerify.getParam1()){
				totleNumber += userProps.getCount();
			}
		}
		
		if(totleNumber < storyVerify.getParam2()){
			return false;
		}
		
		return true;
	}

	
	@PostConstruct
	public void registerVerify() {
		this.dungeonVerify.putVerifyHandler(getType(), this);
	}

}
