package com.yayo.warriors.common.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.campbattle.model.CampBattle;
import com.yayo.warriors.module.campbattle.model.PlayerCampBattle;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.campbattle.vo.CampBattleVO;
import com.yayo.warriors.module.campbattle.vo.PlayerBattleVO;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.notice.vo.NoticeVo;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.campbattle.CampBattleCmd;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;

@Component
public class CampBattlePushHelper {
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private WorldPusherHelper worldPusherHelper;
	@Autowired
	private UserManager userManager;
	@Autowired
	private CampBattleFacade campBattleFacade;
	@Autowired
	private MapFacade mapFacade;
	
	/** 静态引用  */
	private static ObjectReference<CampBattlePushHelper> ref = new ObjectReference<CampBattlePushHelper>();
	/** 日志  */
	private static final Logger logger = LoggerFactory.getLogger(CampBattlePushHelper.class);
	
	@PostConstruct
	private void init(){
		ref.set(this);
	}
	
	/**
	 * 推送战场信息
	 * 
	 * @param campBattles			阵营战场信息对象
	 * @param userDomain			玩家域对象
	 * @param playerCampBattle		玩家战场信息对象
	 */
	public static void pushBattleInfo(Collection<CampBattle> campBattles, List<Integer> noCampPointId, PlayerCampBattle playerCampBattle) {
		pushBattleInfo(campBattles, noCampPointId, playerCampBattle, false);
	}
	public static void pushBattleInfo(Collection<CampBattle> campBattles, List<Integer> noCampPointId, PlayerCampBattle playerCampBattle, boolean over) {
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		Response response = Response.defaultResponse(Module.CAMP_BATTLE, CampBattleCmd.PUSH_CAMP_BATTLE_INFO);
		for(CampBattle campBattle : campBattles){
			resultMap.put("campBattleVO" + campBattle.getCamp().ordinal(), CampBattleVO.valueOf(campBattle) );
		}
		resultMap.put("playerBattleVO", PlayerBattleVO.valueOf(playerCampBattle, null) );
		resultMap.put("noCampPointId", noCampPointId.toArray() );
		if(over){
			resultMap.put("over", over );
		}
		response.setValue( resultMap );
		
		ref.get().sessionManager.write(playerCampBattle.getPlayerId(), response);
	}

	/**
	 * 推送据点得分信息
	 * 
	 * @param campBattles			阵营战场信息对象
	 */
	public static void pushBattleInfo(Collection<CampBattle> campBattles, List<Integer> noCampPointId) {
		pushBattleInfo(campBattles, noCampPointId, false);
	}
	public static void pushBattleInfo(Collection<CampBattle> campBattles, List<Integer> noCampPointId, boolean over) {
		for(CampBattle campBattle : campBattles){
			List<PlayerCampBattle> players = null;
			Set<PlayerCampBattle> playerCBs = campBattle.getPlayers();
			synchronized (playerCBs) {
				players = new ArrayList<PlayerCampBattle>( playerCBs );
			}
			
			for(PlayerCampBattle pcb : players){
				if(pcb == null){
					continue ;
				}
				pushBattleInfo(campBattles, noCampPointId, pcb, over);
			}
		}
	}
	
	/**
	 * 强制玩家离开战场
	 * @param campBattles
	 */
	public static void pushForseLeaveCampBattle(Collection<PlayerCampBattle> players){
		for(PlayerCampBattle pcb : players){
			long playerId = pcb.getPlayerId();
			UserDomain userDomain = ref.get().userManager.getUserDomain(playerId);
			GameMap gameMap = userDomain != null ? userDomain.getGameMap() : null;
			if(gameMap != null && userDomain.getMapId() == CampBattleRule.CAMP_BATTLE_MAPID){
				try {
					GameMap targetGameMap = ref.get().gameMapManager.getGameMapById(MapRule.DEFAUL_REVIVE_MAPID, userDomain.getBranching() );
					ChangeScreenVo changeScreenVo = ref.get().mapFacade.leaveMap(userDomain, targetGameMap, MapRule.DEFAUL_REVIVE_X, MapRule.DEFAUL_REVIVE_Y);
					
					Map<String, Object> resultMap = new HashMap<String, Object>(2);
					resultMap.put("result", CampBattleConstant.SUCCESS);
					resultMap.put("changeScreenVO", changeScreenVo );
					
					Response response = Response.defaultResponse(Module.CAMP_BATTLE, CampBattleCmd.EXIST_CAMPBATTLE, resultMap);
					ref.get().sessionManager.write(playerId, response);
					
				} catch (Exception e) {
					logger.error("推送玩家退出出错：{}", e);
					logger.error("{}", e);
				}
			}
			
		}
	}
	
	/**
	 * 推送阵营据点改变
	 * @param monsterAiDomain
	 */
	public static void pushCampPointChange(MonsterDomain monsterAiDomain){
		GameMap gameMap = monsterAiDomain.getGameMap();
		Set<ISpire> canViewsPlayers = gameMap.getCanViewsSpireCollection(monsterAiDomain, ElementType.PLAYER);
		for(ISpire spire : canViewsPlayers){
			UserDomain userDomain = (UserDomain)spire;
			userDomain.putHideSpire(monsterAiDomain);
			userDomain.putCanViewSpire(monsterAiDomain);
		}
		ref.get().worldPusherHelper.putMessage2Queue(canViewsPlayers);
	}
	
	/**
	 * 邀请战场命令
	 * @param players	玩家列表
	 * @param cmd		1-报名玩家进入战场, 2-邀请其他玩家进入战场, 3-推送战场结束
	 * @return 
	 */
	public static void pushCampBattleCmd(Collection<Long> players, int cmd){
		Response response = Response.defaultResponse(Module.CAMP_BATTLE, CampBattleCmd.PUSH_CAMP_BATTLE_CMD);
		response.setValue(cmd);
		ref.get().sessionManager.write(players, response);
	}
	
	/**
	 * 
	 * @param players
	 */
	public static void pushCampBattleStop(Collection<PlayerCampBattle> players){
		Response response = Response.defaultResponse(Module.CAMP_BATTLE, CampBattleCmd.PUSH_CAMP_BATTLE_CMD);
		response.setValue(3);
		if(players != null){
			for(PlayerCampBattle pcb : players){
				ref.get().sessionManager.write(pcb.getPlayerId(), response);
			}
		}
	}
	
	/**
	 * 推送公告
	 * @param noticeID
	 * @param players
	 * @param params
	 */
	public static void pushNotice(int noticeID, Collection<Long> playerIds, Map<String, Object> params){
		BulletinConfig bulletinConfig = NoticePushHelper.getConfig(noticeID, BulletinConfig.class);
		NoticeVo noticeVO = NoticeVo.valueOf(noticeID, NoticeType.HONOR, params, bulletinConfig.getPriority());
		
		NoticePushHelper.pushNotice(playerIds, noticeVO);
	}
	
	/**
	 * 推送公告
	 * @param noticeID
	 * @param camp
	 * @param params
	 */
	public static void pushNotice(int noticeID, Camp camp, Map<String, Object> params){
		List<CampBattle> campBattles = ref.get().campBattleFacade.getCampBattles(camp);
		List<Long> playerIds = new ArrayList<Long>();
		if(campBattles != null) {
			for(CampBattle campBattle : campBattles) {
				Set<PlayerCampBattle> players = campBattle.getPlayers();
				for(PlayerCampBattle playerCampBattle : players){
					playerIds.add( playerCampBattle.getPlayerId() );
				}
			}
			
		}
		pushNotice(noticeID, playerIds, params);
	}
	
	/**
	 * 推送据点被攻击信息
	 * @param noticeID
	 * @param camp
	 * @param params
	 */
	public static void pushPointAttacked(int monsterConfigId, Camp camp, boolean attacked){
		List<CampBattle> campBattles = ref.get().campBattleFacade.getCampBattles(camp);
		List<Long> playerIds = new ArrayList<Long>();
		if(campBattles != null) {
			for(CampBattle campBattle : campBattles) {
				Set<PlayerCampBattle> players = campBattle.getPlayers();
				for(PlayerCampBattle playerCampBattle : players){
					playerIds.add( playerCampBattle.getPlayerId() );
				}
			}
			
		}
		
		Response response = Response.defaultResponse(Module.CAMP_BATTLE, CampBattleCmd.PUSH_POINT_ATTACKED);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.ID, monsterConfigId);
		resultMap.put("attacked", attacked);
		response.setValue(resultMap);
		
		ref.get().sessionManager.write(playerIds, response);
	}
	
	/**
	 * 推送杀人公告
	 * @param killPlayers
	 * @param firstBlood
	 * @param player
	 * @param target
	 */
	public static int pushKillPlayerNotice(Player player, Player target, int killPlayers, ConcurrentMap<Integer, Object> noticeMap){
		Integer bloodFlag = (Integer)noticeMap.putIfAbsent(NoticeID.CAMP_BATTLE_FIRST_BLOOD, 1);
		boolean firstBlood = bloodFlag == null || bloodFlag != 1;
		
		//第一滴血
		Camp playerCamp = player.getCamp();
		BulletinConfig bulletinConfig = NoticePushHelper.getConfig(NoticeID.CAMP_BATTLE_FIRST_BLOOD, BulletinConfig.class);
		if(bulletinConfig != null){
			if(firstBlood){
				Map<String, Object> params = new HashMap<String, Object>(4);
				params.put(NoticeRule.campName, playerCamp.getName());
				params.put(NoticeRule.playerName, player.getName());
				params.put(NoticeRule.campName2, target.getCamp().getName());
				params.put(NoticeRule.targetName, target.getName());
				
				pushNotice(bulletinConfig.getId(), playerCamp, params);
			}
			
		} else {
			logger.error("公告基础数据不存在:{}", NoticeID.CAMP_BATTLE_FIRST_BLOOD);
		}
		
		int noticeId = 0;
		//击杀10， 20， 30
		bulletinConfig = NoticePushHelper.getConfig(NoticeID.CAMP_BATTLE_KILL_PLAYERS, BulletinConfig.class);
		if(bulletinConfig != null){
			if(bulletinConfig.getConditions().contains(killPlayers) ){
				noticeId = bulletinConfig.getId();
				Map<String, Object> params = new HashMap<String, Object>(4);
				params.put(NoticeRule.campName, playerCamp.getName() );
				params.put(NoticeRule.playerName, player.getName() );
				params.put(NoticeRule.number, killPlayers );
				params.put(NoticeRule.campName2, target.getCamp().getName() );
				
				pushNotice( noticeId, playerCamp, params );
			}
			
		} else {
			logger.error("公告基础数据不存在:{}", NoticeID.CAMP_BATTLE_KILL_PLAYERS);
		}
		
		//击杀50，60，70, 80, 90, 100
		bulletinConfig = NoticePushHelper.getConfig(NoticeID.CAMP_BATTLE_KILL_PLAYERS_COUNT, BulletinConfig.class);
		if(bulletinConfig != null){
			if(bulletinConfig.getConditions().contains(killPlayers) ){
				noticeId = bulletinConfig.getId();
				Map<String, Object> params = new HashMap<String, Object>(4);
				params.put(NoticeRule.campName, playerCamp.getName() );
				params.put(NoticeRule.playerName, player.getName() );
				params.put(NoticeRule.number, killPlayers );
				params.put(NoticeRule.campName2, target.getCamp().getName() );
				
				pushNotice( noticeId, playerCamp, params );
			}
			
		} else {
			logger.error("公告基础数据不存在:{}", NoticeID.CAMP_BATTLE_KILL_PLAYERS_COUNT);
		}
		
		//人头数达到50个，本阵营击杀人数最多者
		bulletinConfig = NoticePushHelper.getConfig(NoticeID.CAMP_BATTLE_KILL_MAX_PLAYERS, BulletinConfig.class);
		if(bulletinConfig != null){
			if(bulletinConfig.getConditions().contains(killPlayers) ){
				noticeId = bulletinConfig.getId();
				Map<String, Object> params = new HashMap<String, Object>(4);
				params.put(NoticeRule.campName, playerCamp.getName() );
				params.put(NoticeRule.playerName, player.getName() );
				params.put(NoticeRule.number, killPlayers );
				params.put(NoticeRule.campName2, target.getCamp().getName() );
				
				pushNotice( noticeId, playerCamp, params );
			}
			
		} else {
			logger.error("公告基础数据不存在:{}", NoticeID.CAMP_BATTLE_KILL_MAX_PLAYERS);
		}
		
		return noticeId;
	}
	
}
