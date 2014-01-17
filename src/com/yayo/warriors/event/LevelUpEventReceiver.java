package com.yayo.warriors.event;

import static com.yayo.warriors.module.achieve.model.AchieveType.*;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.AbstractReceiver;
import com.yayo.warriors.common.helper.TeamPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.friends.helper.FriendHelper;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.rank.manager.RankManager;
import com.yayo.warriors.module.title.facade.TitleFacade;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;

/**
 * 升级事件
 * 
 * @author Hyint
 */
@Component
public class LevelUpEventReceiver extends AbstractReceiver<LevelUpEvent> {
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private RankManager rankManager;
	@Autowired
	private TitleFacade titleFacade;
	@Autowired
	private AchieveFacade achieveFacade;
	
	
	@Override
	public String[] getEventNames() {
		return new String[] { LevelUpEvent.NAME };
	}

	@Override
	public void doEvent(LevelUpEvent levelUpEvent) {
		Fightable beforable = levelUpEvent.getBeforable();
		UserDomain userDomain = levelUpEvent.getUserDomain();
		long playerId = userDomain.getPlayerId();
		PlayerBattle playerBattle = userDomain.getBattle();
		int playerLevel = playerBattle.getLevel();
		
		FriendHelper.pushFriendLevelUp(userDomain);
		this.pushAttribute2AreaMembers(userDomain);
		TeamPushHelper.pushAttribute2TeamMembers(playerId);
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain);
		titleFacade.obtainNewTitleRelationLevel(playerId, playerLevel);
		rankManager.checkRankOpen(playerLevel);
		FriendHelper.pushFriendLevelChange(userDomain);
		achieveFacade.commonAchieved(playerId, LEVEL_ACHIEVE, playerLevel);
	}
	

	/**
	 * 推送属性给区域玩家
	 * 
	 * @param playerId		角色ID
	 */
	private void pushAttribute2AreaMembers(UserDomain userDomain) {
		long playerId = userDomain.getId();
		Object[] attributes = AttributeRule.AREA_MEMBER_VIEWS_PARAMS;
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), attributes);
		UserPushHelper.pushPlayerLevelUp(playerIdList, userDomain.getUnitId(), userDomain.getBattle().getLevel());
	}
}
