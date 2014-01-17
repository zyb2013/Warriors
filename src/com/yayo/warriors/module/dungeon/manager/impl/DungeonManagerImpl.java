package com.yayo.warriors.module.dungeon.manager.impl;


import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.basedb.model.DungeonStoryConfig;
import com.yayo.warriors.common.helper.DungeonPushHelper;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.rule.BargedDungeonRule;
import com.yayo.warriors.module.dungeon.rule.BaseDungeonRule;
import com.yayo.warriors.module.dungeon.rule.HighRichDungeonRule;
import com.yayo.warriors.module.dungeon.rule.LayerTowerRule;
import com.yayo.warriors.module.dungeon.rule.RoundDungeonRule;
import com.yayo.warriors.module.dungeon.rule.RoundTimeDungeonRule;
import com.yayo.warriors.module.dungeon.rule.TreasureDungeonRule;
import com.yayo.warriors.module.dungeon.types.DungeonType;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 副本数据管理层实现类
 * @author liuyuhua
 */
@Service
public class DungeonManagerImpl extends CachedServiceAdpter implements DungeonManager, DataRemoveListener{
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private DungeonPushHelper dungeonPushHelper;
	@Autowired
	private BargedDungeonRule bargedDungeonRule;
	@Autowired
	private RoundDungeonRule roundDungeonRule;
	@Autowired
	private LayerTowerRule layerTowerRule;
	@Autowired
	private RoundTimeDungeonRule roundTimeDungeonRule;
	@Autowired
	private HighRichDungeonRule highRichDungeonRule;
	@Autowired
	private TreasureDungeonRule treasureDungeonRule;
	@Autowired
	private GameMapManager gameMapManager;
	
	private static final ConcurrentHashMap<Long, Dungeon> DUNGEONS = new ConcurrentHashMap<Long, Dungeon>(0);
	private static final ConcurrentHashMap<Integer, BaseDungeonRule> DUNGEONRULE = new ConcurrentHashMap<Integer, BaseDungeonRule>(0);
	
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	public PlayerDungeon getPlayerDungeon(long playerId) {
		if(playerId > 0L) {
			return this.get(playerId, PlayerDungeon.class);
		}
		return null;
	}
	
	
	public PlayerDungeon getPlayerDungeon(PlayerBattle battle) {
		if(battle == null){
			return null;
		}
		long playerId = battle.getId();
		return this.get(playerId, PlayerDungeon.class);
	}
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == PlayerDungeon.class) {
			PlayerDungeon playerDungeon = commonDao.get((Long)id, PlayerDungeon.class);
			if(playerDungeon == null){
				try {
					playerDungeon = PlayerDungeon.valueOf((Long)id);
					commonDao.save(playerDungeon);
				} catch (Exception e) {
					playerDungeon = null;
					logger.error("角色:[{}] 创建坐骑信息异常:{}", id, e);
				}
			}
			return (T) playerDungeon;
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public Dungeon createDungeon(UserDomain userDomain,DungeonConfig config) {
		if(userDomain == null || config == null){
			return null;
		}
		
		Player player = userDomain.getPlayer();
		Dungeon dungeon = Dungeon.valueOf(player.getBranching(), config, gameMapManager.getTemporaryGameMapId() );
	    DUNGEONS.putIfAbsent(dungeon.getDungeonId(), dungeon);
	    return DUNGEONS.get(dungeon.getDungeonId());
	}

	
	public Dungeon getDungeon(long dungeonId) {
		return DUNGEONS.get(dungeonId);
	}
	
	
	public DungeonConfig getDungeonConfig(int baseId) {
		return resourceService.get(baseId, DungeonConfig.class);
	}

	
	public void removeDungeon(long dungeonId) {
		DUNGEONS.remove(dungeonId);
	}

	
	public void killDungeonMonster(long dungeonId, long monsterId,int exp) {
		Dungeon dungeon = this.getDungeon(dungeonId);
		if(dungeon == null){
			return;
		}
		DungeonConfig dungeonConfig = dungeon.getDungeonConfig();
		if(dungeonConfig == null){
			return;
		}
//		logger.error("副本[{}]中怪物[{}]被杀死...", dungeonId, monsterId);
		Map<String,Object> msg = dungeon.removeMonsters(monsterId,exp);
		dungeonPushHelper.pushDungeonStatistics(dungeon.filterPlayers(), msg); //推送副本统计信息
		if(dungeon.getType() == DungeonType.ROUND){
			if(dungeon.isEmpty4Monster()){
				int nextRound = dungeonConfig.getNextRoundSec();
				long date =DateUtil.getCurrentSecond() + nextRound; //下一波开始的时间
				dungeon.setNextRoundDate(date);
				dungeonPushHelper.pushNoticeProgress(dungeon); //推送下一波即将开始的信息
			}
		}
	}
	
	
	public void processDungeonRule() {
		for(Iterator<Entry<Long, Dungeon>> iterator = DUNGEONS.entrySet().iterator();iterator.hasNext();){
			try {
				Entry<Long, Dungeon> entry = iterator.next();
				if(entry == null){
					continue;
				}
				
				Dungeon dungeon = entry.getValue();
				if(dungeon == null){
					logger.error("副本不存在:{}", entry.getKey() );
					continue;
				}
				BaseDungeonRule baseDungeonRule = DUNGEONRULE.get(dungeon.getType());
				if(baseDungeonRule == null){
					logger.error("副本玩法类型不存在:{}", dungeon.getType() );
					continue;
				}
				baseDungeonRule.action(dungeon);//开始处理
				
			} catch (Exception e) {
				logger.error("副本定时器,执行异常:{}",e);
				logger.error("{}",e);
			}
		}
	}
	
	
	public boolean canEnterDungeon(long playerId, int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return false;
		}
		
		PlayerDungeon playerDungeon = this.getPlayerDungeon(playerId);
		if(playerDungeon == null){
			return false;
		}
		
		DungeonConfig dungeonConfig = this.getDungeonConfig(dungeonBaseId);
		if(dungeonConfig == null){
			return false;
		}
		
		if(playerDungeon.getEnterDungeonTimes(dungeonBaseId) >= dungeonConfig.getEnterNum()){
			return false;
		}
		
		return true;
	}

	
	@PostConstruct
	protected void initialize(){
		DUNGEONRULE.putIfAbsent(layerTowerRule.getDungeonType(),       layerTowerRule);
		DUNGEONRULE.putIfAbsent(roundDungeonRule.getDungeonType(),     roundDungeonRule);
		DUNGEONRULE.putIfAbsent(bargedDungeonRule.getDungeonType(),    bargedDungeonRule);
		DUNGEONRULE.putIfAbsent(highRichDungeonRule.getDungeonType(),  highRichDungeonRule);
		DUNGEONRULE.putIfAbsent(roundTimeDungeonRule.getDungeonType(), roundTimeDungeonRule);
		DUNGEONRULE.putIfAbsent(treasureDungeonRule.getDungeonType(), treasureDungeonRule);
	}

	
	public boolean verifyStoryDungeon(long playerId, int stroyDungeonId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return false;
		}
		
		DungeonStoryConfig config = this.getDungeonStoryConfig(stroyDungeonId);
		if(config == null){
			return false;
		}
		
		return true;
	}

	
	public DungeonStoryConfig getDungeonStoryConfig(int baseId) {
		return resourceService.get(baseId, DungeonStoryConfig.class);
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		//移除用户的副本对象
		this.removeEntityFromCache(messageInfo.getPlayerId(), PlayerDungeon.class);
	}
	
}
