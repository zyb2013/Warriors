package com.yayo.warriors.module.search.facade.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedService;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.module.horse.facade.HorseFacade;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.onhook.manager.TrainManager;
import com.yayo.warriors.module.onhook.model.UserSingleTrain;
import com.yayo.warriors.module.search.facade.SearchFacade;
import com.yayo.warriors.module.search.rule.SearchRule;
import com.yayo.warriors.module.search.vo.CommonSearchVo;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Job;

/**
 * 搜索接口实现
 * 
 * @author huachaoping
 */
@Component
public class SearchFacadeImpl implements SearchFacade {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CachedService cachedService;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private TrainManager trainManager;
	@Autowired
	private HorseFacade horseFacade;
	
	
	/**
	 * 查找同屏玩家名字
	 * 
	 * @param  keywords
	 * @return Collection<CommonSearchVo> - {玩家名字,玩家ID} 
	 */
	
	public Collection<CommonSearchVo> searchScreenPlayer(long playerId, String keywords) {
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		Collection<CommonSearchVo> searchList = new ArrayList<CommonSearchVo>();
		
		if (keywords == null) {
			return searchList;
		}
		
		for (long searchId : playerIdList) {
			if (playerId == searchId) {
				continue;
			}
			
			if (!sessionManager.isOnline(searchId)) {   //角色在线验证
				continue;
			}
			
			UserDomain userDomain = userManager.getUserDomain(searchId);
			if (userDomain == null) {
				continue;
			}
			
			UserSingleTrain singleTrain = trainManager.getUserSingleTrain(searchId);
			if (singleTrain.isCoupleTrain()) {
				continue;
			} 
			
			boolean ride = horseFacade.isRide(searchId);
			if (ride) {
				continue;
			}
			
			Player player = userDomain.getPlayer();
			PlayerBattle battle = userDomain.getBattle();
			String playerName = player.getName();
			int camp = player.getCamp().ordinal();
			if (playerName.contains(keywords)) {
				Job playerClazz = battle.getJob();
				int playerLevel = battle.getLevel();
				searchList.add(CommonSearchVo.valueOf(searchId, playerName, playerLevel, playerClazz,camp));
			}
		}
		return searchList;
	}
	
	
	/**
	 * 查找玩家 名字
	 * 
	 * @return Collection<CommonSearchVo> - {玩家名字,玩家ID}
	 */
	
	public Collection<CommonSearchVo> searchPlayerName(String keywords) {
		//StringUtils如果keywords为null则为true
		if(StringUtils.isBlank(keywords)) {
			return new ArrayList<CommonSearchVo>(0);
		}
	
		String subKey = this.getPlayerNameSubKey(keywords); // 构建二级缓存关键字
		Collection<CommonSearchVo> searchList = this.getCommonCache1min(subKey);
		if (searchList == null) {
			searchList = searchOnlinePlayers(keywords);		// 通用查询VO对象
			this.putCommonCache1min(subKey, searchList);
		}
		return searchList;
	}

	
	/**
	 * 查询在线玩家列表
	 *  
	 * @param  keywords	 			关键字. 一定要保证keywords不为null
	 * @return {@link Collection}	返回通用的查询VO
	 * @throws NullPointerException	当keywords == null则会抛出该异常
	 */
	private Collection<CommonSearchVo> searchOnlinePlayers(String keywords) {
		Collection<CommonSearchVo> searchList = new ArrayList<CommonSearchVo>();
		Collection<Long> onlinePlayerIdList = sessionManager.getOnlinePlayerIdList();	// 获取所有在线玩家IDS
		for (Long playerId : onlinePlayerIdList) {
			if(!sessionManager.isOnline(playerId)) {   //角色在线验证
				continue;
			}
			
			UserDomain userDomain = userManager.getUserDomain(playerId);
			if(userDomain == null) {
				continue;
			}
			
			Player player = userDomain.getPlayer();
			PlayerBattle battle = userDomain.getBattle();
			String playerName = player.getName();
			int camp = player.getCamp().ordinal();
			if (playerName.contains(keywords)) {
				Job playerClazz = battle.getJob();
				int playerLevel = battle.getLevel();
				searchList.add(CommonSearchVo.valueOf(playerId, playerName, playerLevel, playerClazz,camp));
			}
		}
		return searchList;
	}

	
	/**
	 * SubKey
	 * 
	 * @param playerName        角色名字
	 * @return {@link String}   角色名SubKey
	 */
	private String getPlayerNameSubKey(String playerName) {
		return SearchRule.HASH_KEY + SearchRule.PLAYER_NAME + (playerName == null ? "" : playerName);
	}
	
	/**
	 * 放入公共缓存(公共缓存设置为1分钟)
	 */
	private void putCommonCache1min(String subKey, Object obj) {
		cachedService.put2CommonHashCache(SearchRule.HASH_KEY, subKey, obj, TimeConstant.ONE_MINUTE_MILLISECOND);
	}
	
	/**
	 * 获取公共缓存中的对象
	 * 
	 * @param subKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Collection<CommonSearchVo> getCommonCache1min(String subKey) {
		return (Collection<CommonSearchVo>) cachedService.getFromCommonCache(SearchRule.HASH_KEY, subKey);
	}


}
