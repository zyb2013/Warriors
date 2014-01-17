package com.yayo.warriors.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.AbstractReceiver;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.drop.facade.LootFacade;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.user.facade.UserFacade;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.FightMode;

/**
 * 进入场景事件
 * 
 * @author Hyint
 */
@Component
public class EnterScreenReceiver extends AbstractReceiver<EnterScreenEvent> {
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private LootFacade lootFacade;
	
	@Override
	public String[] getEventNames() {
		return new String[]{EnterScreenEvent.NAME};
	}

	@Override
	public void doEvent(EnterScreenEvent event) {
		GameMap toMap = event.getToMap();
		UserDomain userDomain = event.getUserDomain();
		if(userDomain != null) {
//			this.changeFightMode(userDomain);
			lootFacade.enterScreen(userDomain, toMap);
	//		TeamPushHelper.pushMemberChangeScreen(userDomain.getId());
		}
	}

	/**
	 * 角色切换场景切换战斗模式
	 * 
	 * @param  userDomain		用户域模型
	 */
	public void changeFightMode(UserDomain userDomain) {
		if(userDomain == null){
			return ;
		}
		
		int forceMode = FightMode.PEACE.ordinal();
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap != null) {
			int type = gameMap.getScreenType();
			ScreenType screenType = EnumUtils.getEnum(ScreenType.class, type);
			if(screenType == null) {
				return;
			}
			forceMode = screenType.getForceMode();
			
			if(forceMode < 0) {
				return;
			}
		}
		
		long playerId = userDomain.getPlayerId();
		ResultObject<Integer> result = userFacade.updateFightMode(playerId, forceMode, false);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("强制切换角色:[{}] 战斗模式:[{}] 返回值:[{}]", new Object[] { playerId, forceMode, result.getResult() });
		}
	}
}
