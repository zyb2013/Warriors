package com.yayo.warriors.module.title.facade.impl;

import static com.yayo.warriors.module.title.rule.TitleRule.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.TitleDictionary;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.title.entity.PlayerTitle;
import com.yayo.warriors.module.title.facade.TitleFacade;
import com.yayo.warriors.module.title.helper.TitleHelper;
import com.yayo.warriors.module.title.manager.TitleManager;
import com.yayo.warriors.module.title.model.TitleType;
import com.yayo.warriors.module.title.type.TitleState;
import com.yayo.warriors.module.title.vo.TitleVo;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;

@Component
public class TitleFacadeImpl implements TitleFacade {

	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private TitleManager titleManager;
	@Autowired
	private TitleHelper titleHelper;

	
	/**
	 * 获得新称号
	 * 
	 * @param  playerId
	 * @param  titleType
	 * @param  param
	 * @return {@link TitleDictionary} 如果是null没有获得新称号
	 */
	
	public TitleDictionary obtainNewTitle(long playerId ,TitleType titleType, Object param) {
		PlayerTitle userTitle = titleManager.getUserTitle(playerId);
		if(userTitle == null) {
			return null;
		}
		
		ChainLock lock = LockUtils.getLock(userTitle);
		try {
			lock.lock();
			userTitle.alterParam(titleType, param.toString());
			if(userTitle.checkObtainTitle(titleType)){
				dbService.submitUpdate2Queue(userTitle);
				return titleManager.getTitleConfig(titleType.getId());
			}
		} finally {
			lock.unlock();
		}
		return null ;
	}
	
	/**
	 * 检查是否获取新称号，等级相关
	 * 
	 * @param playerId
	 * @param currentLevel
	 */
	
	public void obtainNewTitleRelationLevel(long playerId ,int currentLevel) {
		for (TitleType titleType : referLevelTitleType) {
			TitleDictionary config = obtainNewTitle(playerId, titleType, currentLevel);
			if (config != null) {
				titleHelper.pushObtainTitle(playerId, config.getId());
			}
		}
	}
	
	/**
	 * 经脉新称号
	 *  
	 * @param playerId
	 * @param passMeridians
	 */
	
	public void obtainNewTitleRelationMeridian(long playerId, int passMeridians) {
		for (TitleType titleType : referMeridianTitleType) {
			TitleDictionary config = obtainNewTitle(playerId, titleType, passMeridians);
			if (config != null) {
				titleHelper.pushObtainTitle(playerId, config.getId());
			}
		}
	}
	
	
	/**
	 * 获取玩家称号列表
	 * 
	 * @param  playerId				角色ID
	 * @return {@link Collection}	{@link TitleVo}列表
	 */
	
	public Collection<TitleVo> hasTitle(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return Collections.emptyList();
		}
		
		PlayerTitle userTitle = titleManager.getUserTitle(playerId);
		if(userTitle == null) {
			return Collections.emptyList();
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return Collections.emptyList();
		}
		
		int usedTitleId = player.getTitle() ;
		Collection<TitleVo> titleVOSet = new HashSet<TitleVo>();
		Set<Integer> obtainedTitles = userTitle.getGainedTItleCache();
		Collection<TitleDictionary> titleConfigList = titleManager.listAllTitleConfig();
		for (TitleDictionary titleConfig : titleConfigList) {
			int titleId = titleConfig.getId();
			TitleState titleState = TitleState.HAVENOT;
			if(obtainedTitles.contains(titleId)) {
				titleState = usedTitleId == titleId ? TitleState.USED : TitleState.OBTAIN;
			}
			titleVOSet.add(new TitleVo(titleConfig, titleState.ordinal()));
		}
		return titleVOSet;
	}
	
	/**
	 * 使用称号
	 * @param playerId
	 * @param titleId
	 * @return
	 */
	
	public int useTitle(long playerId, int titleId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return CommonConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return CommonConstant.PLAYER_NOT_FOUND;
		}
		
		PlayerTitle userTitle = titleManager.getUserTitle(playerId);
		
		Set<Integer> gainedTItleCache = userTitle.getGainedTItleCache();
		if(!gainedTItleCache.contains(titleId)) { //没有这个称号
			return CommonConstant.TITLE_INVALID;
		} 
		
		if(player.getTitle() == titleId) { 		//减少多次选择
			return CommonConstant.SUCCESS;
		}
		
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			player.setTitle(titleId);
			dbService.submitUpdate2Queue(player);
		} finally {
			lock.unlock();
		}
		
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, playerUnits, AttributeKeys.TITLE);
		
		return CommonConstant.SUCCESS ;
	}
	
	
	/**
	 * 解除称号
	 * @param playerId
	 * @return
	 */
	
	public Integer removeTitle(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return CommonConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if(player.getTitle() <= 0) {
			return CommonConstant.SUCCESS;
		}
		
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			player.setTitle(0);
			dbService.submitUpdate2Queue(player);
		} finally {
			lock.unlock();
		}
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.TITLE);
		
		return CommonConstant.SUCCESS ;
	}
}
