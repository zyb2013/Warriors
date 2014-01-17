package com.yayo.warriors.module.campbattle.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.campbattle.dao.CampBattleDao;
import com.yayo.warriors.module.campbattle.entity.CampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleRecord;
import com.yayo.warriors.module.campbattle.manager.CampBattleManager;
import com.yayo.warriors.module.campbattle.model.CampBattle;
import com.yayo.warriors.module.campbattle.model.PlayerCampBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 阵营战业务接口实现
 * @author jonsai
 *
 */
@Component
public class CampBattleManagerImpl extends CachedServiceAdpter implements CampBattleManager {
	@Autowired
	private CampBattleDao campBattleDao;
	@Autowired
	private UserManager userManager;
	
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger( getClass() );
	
	/** 缓存前缀 */
	public final static String CAMP_BATTLE_PREFIX = "CAMP_BATTLE_HIS_PREFIX";
	public final static String PLAYER_CAMP_BATTLE_PREFIX = "PLAYER_CAMP_BATTLE_PREFIX";
	public final static String PLAYER_CAMP_BATTLE_SCORE_PREFIX = "PLAYER_CAMP_BATTLE_SCORE_PREFIX";
	public final static String PLAYER_CAMP_BATTLE_TITLE_PREFIX = "PLAYER_CAMP_BATTLE_TITLE_PREFIX";
	public final static String CAMP_BATTLE_LEADER_IDS = "CAMP_BATTLE_LEADER_IDS";
	public final static String CAMP_BATTLE_DATE_LIST = "CAMP_BATTLE_DATE_LIST";
	
	
	public PlayerCampBattleHistory getPlayerCampBattleHistory(long id) {
		return this.get(id, PlayerCampBattleHistory.class);
	}
	
	
	public PlayerCampBattleRecord getPlayerCampBattleRecord(long id) {
		PlayerCampBattleRecord playerCampBattleRecord = this.get(id, PlayerCampBattleRecord.class);
		Camp camp = playerCampBattleRecord.getCamp();
		if(camp == null || camp == Camp.NONE){
			UserDomain userDomain = userManager.getUserDomain(id);
			if(userDomain != null){
				ChainLock lock = LockUtils.getLock(playerCampBattleRecord);
				try {
					lock.lock();
					playerCampBattleRecord.setCamp(userDomain.getPlayer().getCamp());
				} finally {
					lock.unlock();
				}
			}
		}
		return playerCampBattleRecord;
	}
	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == PlayerCampBattleRecord.class){
			PlayerCampBattleRecord record = commonDao.get(id, PlayerCampBattleRecord.class);
			if(record == null){
				record = new PlayerCampBattleRecord();
				long playerId =  (Long)id ;
				record.setId( playerId );
				
				UserDomain userDomain = userManager.getUserDomain(playerId);
				if(userDomain != null){
					record.setCamp(userDomain.getPlayer().getCamp());
				}
				commonDao.save(record);
			}
			return (T) record;
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public CampBattleHistory getCampBattleHistory(long id) {
		return this.get(id, CampBattleHistory.class);
	}
	
	@SuppressWarnings("unchecked")
	
	public List<Date> getCampBattleDates() {
		List<Date> list = (List<Date>)cachedService.getFromCommonCache(CAMP_BATTLE_DATE_LIST);
		if(list == null){
			list = campBattleDao.getCampBattleDates();
			CopyOnWriteArrayList<Date> dList = new CopyOnWriteArrayList<Date>();
			if(list != null){
				dList.addAll(list);
			}
			cachedService.put2CommonCache(CAMP_BATTLE_DATE_LIST, dList);
			list = (List<Date>)cachedService.getFromCommonCache(CAMP_BATTLE_DATE_LIST);
		}
		return list;
	}
	
	
	public void saveCampBattleInfo(Date battleDate, Collection<CampBattle> campBattles, Collection<PlayerCampBattle> playerCampBattles, Collection<PlayerCampBattleRecord> playerCampBattleRecords) {
		if(battleDate == null || campBattles == null || playerCampBattles == null){
			logger.error("保存战场历史记录失败，参数不能为空");
			return ;
		}
		
		Collection<CampBattleHistory> campBattleHistorys = CampBattleHistory.valueOf(campBattles, battleDate);
		Collection<PlayerCampBattleHistory> playerCampBattleHistorys = PlayerCampBattleHistory.valueOf(playerCampBattles, battleDate);
		
		campBattleDao.saveCampBattleInfo(campBattleHistorys, playerCampBattleHistorys, playerCampBattleRecords);
		put2EntityCache(campBattleHistorys);
		put2EntityCache(playerCampBattleHistorys);
	}

	
	public CampBattleHistory getCampBattleHistory(Date date, Camp camp) {
		String subKey = buildCacheKey(date, camp);
		cachedService.removeFromCommonHashCache(CAMP_BATTLE_PREFIX, subKey);
		Long historyId = (Long)cachedService.getFromCommonCache(CAMP_BATTLE_PREFIX, subKey);
		if(historyId == null){
			CampBattleHistory campBattleHistory = campBattleDao.getCampBattleHistory(date, camp);
			if(campBattleHistory != null){
				historyId = campBattleHistory.getId();
			} else {
				historyId = -1L;
			}
			cachedService.put2CommonHashCache(CAMP_BATTLE_PREFIX, subKey, historyId);
			return campBattleHistory;
			
		} else if(historyId > 0L){
			return this.get(historyId, CampBattleHistory.class);
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	
	public List<Long> getPlayerCampBattleHistory(Date date, Camp camp, boolean queryDB) {
		if(date == null || camp == null){
			return null;
		}
		String subKey = buildCacheKey(date, camp);
		List<Long> idList = (List<Long>)cachedService.getFromCommonCache(PLAYER_CAMP_BATTLE_PREFIX, subKey);
		if(idList == null){
			idList = (List<Long>)cachedService.getFromCommonCache(PLAYER_CAMP_BATTLE_PREFIX, subKey);
			if(queryDB){
				idList = campBattleDao.getPlayerCampBattleHistory(date, camp);
			} else {
				idList = Collections.synchronizedList( new ArrayList<Long>() );
			}
			cachedService.put2CommonHashCacheIfAbsent(PLAYER_CAMP_BATTLE_PREFIX, subKey, idList);
			idList = (List<Long>)cachedService.getFromCommonCache(PLAYER_CAMP_BATTLE_PREFIX, subKey);
		}
		return idList;
	}
	
	@SuppressWarnings("unchecked")
	
	public List<Long> getPlayerTotalScoreList(Camp camp) {
		if(camp == null){
			return null;
		}
		String subKey = buildCacheKey(null, camp);
		List<Long> idList = (List<Long>)cachedService.getFromCommonCache(PLAYER_CAMP_BATTLE_SCORE_PREFIX, subKey);
		if(idList == null){
			CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<Long>();
			idList = campBattleDao.getPlayerTotalScoreList(camp);
			if(idList != null){
				list.addAll(idList);
			}
			cachedService.put2CommonHashCacheIfAbsent(PLAYER_CAMP_BATTLE_SCORE_PREFIX, subKey, list);
			idList = (List<Long>)cachedService.getFromCommonCache(PLAYER_CAMP_BATTLE_SCORE_PREFIX, subKey);
		}
		return idList;
	}
	
	@SuppressWarnings("unchecked")
	
	public List<Long> getCampTitlePlayers(Camp camp) {
		if(camp == null){
			return null;
		}
		String subKey = buildCacheKey(null, camp);
		List<Long> idList = (List<Long>)cachedService.getFromCommonCache(PLAYER_CAMP_BATTLE_TITLE_PREFIX, subKey);
		if(idList == null){
			CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<Long>();
			idList = campBattleDao.getCampTitlePlayers(camp);
			if(idList != null){
				list.addAll(idList);
			}
			cachedService.put2CommonHashCacheIfAbsent(PLAYER_CAMP_BATTLE_TITLE_PREFIX, subKey, list);
			idList = (List<Long>)cachedService.getFromCommonCache(PLAYER_CAMP_BATTLE_TITLE_PREFIX, subKey);
		}
		return idList;
	}

	
	public PlayerCampBattleHistory getPlayerCampBattleHistory(long playerId, Date date, Camp camp) {
		List<Long> hisIds = getPlayerCampBattleHistory(date, camp, true);
		if(hisIds != null){
			for(Long hId : hisIds){
				PlayerCampBattleHistory playerCampBattleHistory = this.getPlayerCampBattleHistory(hId);
				if(playerCampBattleHistory != null && playerCampBattleHistory.getPlayerId() == playerId){
					return playerCampBattleHistory;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	
	public List<Long> getCampLeaderPlayerIds(Camp camp) {
		String subKey = String.valueOf(camp);
		List<Long> idList = (List<Long>)cachedService.getFromCommonCache(CAMP_BATTLE_LEADER_IDS, subKey);
		if(idList == null){
			CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<Long>();
			idList = campBattleDao.getCampLeaderPlayerIds(camp);
			if(idList != null){
				list.addAll( idList );
			}
			cachedService.put2CommonHashCacheIfAbsent(CAMP_BATTLE_LEADER_IDS, subKey, list);
			idList = (List<Long>)cachedService.getFromCommonCache(CAMP_BATTLE_LEADER_IDS, subKey);
		}
		return idList;
	}

	
	public void clearPlayerCampBattleRecord() {
		this.campBattleDao.clearPlayerCampBattleRecord();
	}
	
	
	public void clearCampTitles() {
		this.campBattleDao.clearCampTitle();
	}

	/**
	 * 构造缓存子key
	 * @param date
	 * @param camp
	 * @return
	 */
	private String buildCacheKey(Date date, Camp camp){
		return new StringBuilder("_date_").append(date).append("_camp_").append(camp.ordinal()).toString();
	}
}
