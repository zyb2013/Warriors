package com.yayo.warriors.module.alliance.manager.impl;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.adapter.AllianceBuildService;
import com.yayo.warriors.basedb.model.AllianceConfig;
import com.yayo.warriors.basedb.model.AllianceShopConfig;
import com.yayo.warriors.basedb.model.AllianceSkillConfig;
import com.yayo.warriors.module.alliance.dao.AllianceDao;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.alliance.model.AllianceRanking;
import com.yayo.warriors.module.alliance.model.MemberRanking;
import com.yayo.warriors.module.alliance.rule.AllianceRule;
import com.yayo.warriors.module.alliance.types.AllianceState;
import com.yayo.warriors.module.alliance.types.Title;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Flushable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AllianceManagerImpl extends CachedServiceAdpter implements AllianceManager, DataRemoveListener {
	
	@Autowired
	private DbService dbService;
	@Autowired
	private AllianceDao allianceDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private AllianceBuildService allianceBuildService;
	
	/** 所有帮派的ID列表 集合*/
	private static final String ALLIANCEIDS = "ALLIANCE_IDS";
	/** 帮派名字集合*/
	private static final String ALLIANCENAMES = "ALLIANCE_NAMES";
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	
	public boolean createAlliance(PlayerAlliance playerAlliance, Alliance alliance) {
		if(alliance == null || playerAlliance == null){
			return false;
		}
		try {
			allianceDao.save(alliance);
			playerAlliance.setTitle(Title.MASTER);
			playerAlliance.setAllianceName(alliance.getName());
			playerAlliance.setAllianceId(alliance.getId());
			playerAlliance.setJiontime(System.currentTimeMillis());
			List<Alliance> list = put2EntityCache(alliance);
			cachedService.removeFromCommonCache(ALLIANCEIDS);   //新的公会创建,将删除类表缓存
			cachedService.removeFromCommonCache(ALLIANCENAMES); //新的公会创建,将删除类表缓存
			return list != null && !list.isEmpty();
		} catch (Exception e) {
			logger.error("玩家[{}],创建帮派异常:{}",playerAlliance.getId(),e);
			return false;
		}
	}


	
	public Alliance getAlliance(long allianceId) {
		return this.get(allianceId, Alliance.class);
	}

	
	@SuppressWarnings("unchecked")
	public List<Long> getAllianceIds() {
		List<Long> allianceIds =  (List<Long>)cachedService.getFromCommonCache(ALLIANCEIDS);
		if(allianceIds != null){
			return allianceIds;
		}
		
		allianceIds = allianceDao.getAllianceIds();
		
		List<AllianceRanking> alliancelist = new ArrayList<AllianceRanking>(allianceIds.size());
		for(Long allianceId : allianceIds){
			Alliance alliance = this.getAlliance(allianceId);
			if(alliance != null){
				long id = alliance.getId();
				int level = alliance.getLevel(); 
				alliancelist.add(AllianceRanking.valueOf(id, level));
			}
		}
		
		List<Long> rankingAlliance = new ArrayList<Long>(alliancelist.size());
		Collections.sort(alliancelist);
		for(AllianceRanking ranking : alliancelist){
			rankingAlliance.add(ranking.getAllianceId());
		}
		cachedService.put2CommonCache(ALLIANCEIDS,rankingAlliance);
		return allianceIds;
	}
	

	
	public Alliance getAlliance4Battle(PlayerBattle battle) {
		if(battle == null){
			return null;
		}
		
		PlayerAlliance playerAlliance = this.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return null;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return null;
		}
		
		return this.getAlliance(playerAlliance.getAllianceId());
	}
	
	
	public Alliance getAlliance4PlayerId(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = this.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return null;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return null;
		}
		
		return this.getAlliance(playerAlliance.getAllianceId());
	}

	
	public PlayerAlliance getPlayerAlliance(PlayerBattle battle) {
		if(battle == null || battle.getLevel() < AllianceRule.CREATE_AND_ADD_ALLIANCE_LEVEL_LIMIT){
			return null;
		}
		
		PlayerAlliance playerAlliance = this.get(battle.getId(), PlayerAlliance.class);
		boolean save = false;//是否需要保存
		if(playerAlliance != null){
			if(playerAlliance.isExistAlliance()){
				save = playerAlliance.refreshTime();//刷新帮派玩法
				Alliance alliance = this.getAlliance(playerAlliance.getAllianceId());
				if(alliance == null || alliance.isDrop()){
					ChainLock lock = LockUtils.getLock(playerAlliance);
					try {
						lock.lock();
						playerAlliance.leaveAlliance();
					}finally{
						lock.unlock();
					}
					save = true;
				}
				
				if(save){
					dbService.submitUpdate2Queue(playerAlliance);
				}
			}
		}
		
		this.calcAttribute(playerAlliance);//计算属性
		return playerAlliance;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String,Long> getAllianceNames() {
		Map<String,Long> allianceNames =  (Map<String,Long>)cachedService.getFromCommonCache(ALLIANCENAMES);
		if(allianceNames != null){
			return allianceNames;
		}
		allianceNames = allianceDao.getAllianceNames();
		if(allianceNames == null){
			allianceNames = new HashMap<String,Long>(1);
		}
		cachedService.put2CommonCache(ALLIANCENAMES,allianceNames);
		return (Map<String,Long>) cachedService.getFromCommonCache(ALLIANCENAMES);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Long> getAllianceMembers(long allianceId,boolean flush) {
		String key = getAllianceMemberIdKey(allianceId);
		List<Long> memberIds = (List<Long>)cachedService.getFromCommonCache(key);
		if(!flush && memberIds != null){
		    return memberIds;
		}

		
		List<Long> idList = new ArrayList<Long>();
		List<MemberRanking> rankings = new ArrayList<MemberRanking>();
		memberIds = allianceDao.getAllianceMember(allianceId);
		
		ChainLock lock = LockUtils.getLock(memberIds);
		try {
			lock.lock();
			
			//并发的一个判断
			List<Long> tmpMembers = (List<Long>)cachedService.getFromCommonCache(key);
			if(!flush && tmpMembers != null) {
				return tmpMembers;
			}
			
			for(long playerId : memberIds){
				UserDomain userDomain = userManager.getUserDomain(playerId);
				if(userDomain == null){
					continue;
				}
				
				PlayerBattle battle = userDomain.getBattle();
				PlayerAlliance playerAlliance = this.getPlayerAlliance(battle);
				if(playerAlliance == null){
					continue;
				}
				
				int online = userManager.isOnline(playerId) == true ? 1 : 0;
				int level = userDomain.getBattle().getLevel();
				int title = playerAlliance.getTitle().ordinal();
				rankings.add(MemberRanking.valueOf(playerId, level, title,online));
			}
			
			Collections.sort(rankings);
			for(MemberRanking memberRanking : rankings){
				idList.add(memberRanking.getPlayerId());
			}
			
			cachedService.put2EntityCache(key, idList, 30000);//缓存时间30秒
			return idList;
			
		}finally{
			lock.unlock();
		}

	}

	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == PlayerAlliance.class) {
			PlayerAlliance playerAlliance = allianceDao.get(id, PlayerAlliance.class);
			if(playerAlliance == null){
				try {
					playerAlliance = AllianceRule.createPlayerAlliance((Long) id);
					allianceDao.save(playerAlliance);
				} catch (Exception e) {
					playerAlliance = null;
					logger.error("角色:[{}] 创建个人帮派信息异常:{}", id, e);
				}
			}
			return (T) playerAlliance;
		}
		return super.getEntityFromDB(id, clazz);
	}
	

	
	public List<Long> disbandAlliance(PlayerAlliance playerAlliance) {
		if(playerAlliance == null){
			return null;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return null;
		}
		
		Alliance alliance =  this.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.getState() == AllianceState.DROP){
			return null;
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.DISBAND_ALLIANCE)){
			return null;
		}
		
		ChainLock lock = LockUtils.getLock(alliance);
		try {
			lock.lock();
			if(alliance.isDrop()){
				return null;
			}
			alliance.setState(AllianceState.DROP);
		}finally{
			lock.unlock();
		}
		
		List<Long> result = new ArrayList<Long>();
		result.addAll(this.getAllianceMembers(alliance.getId(),false));
		dbService.updateEntityIntime(alliance);
		
		cachedService.removeFromCommonCache(ALLIANCENAMES); //删除缓存中的名字
		cachedService.removeFromEntityCache(this.getAllianceMemberIdKey(alliance.getId()));
		return result;
	}
	
	
	public AllianceConfig getAllianceConfig(int level) {
		return resourceService.get(level, AllianceConfig.class);
	}
	
	/**
	 * 帮派成员列表缓存
	 * @param allianceId        帮派的ID
	 * @return {@link String}   键值
	 */
	private String getAllianceMemberIdKey(long allianceId) {
		return new StringBuffer().append("ALLIANCE_MEMBER_IDS_").append(allianceId).toString();
	}


	
	public boolean gquitAlliance(PlayerAlliance playerAlliance,PlayerBattle battle) {
	    if(playerAlliance == null){
	    	return false;
	    }
	    
	    if(playerAlliance.getTitle() == Title.MASTER){
	    	return false;
	    }
	    
	    long allianceId = playerAlliance.getAllianceId(); //帮派ID
	    Alliance alliance = this.getAlliance(playerAlliance.getAllianceId());
	    if(alliance == null || alliance.isDrop()){
	    	return false;
	    }
	    
	    ChainLock lock = LockUtils.getLock(playerAlliance,alliance,battle);
	    try {
			lock.lock();
    	    if(!playerAlliance.isExistAlliance()){
    	    	return false;
    	    }
    	    
			//如果之前已经有职位了,将来更改之前的职位人数
			Title afterTitle = playerAlliance.getTitle();
			if(Title.DEPUTYMASTER == afterTitle){
				alliance.decreaseDeputymaster(1);
			}else if(Title.PROLAW == afterTitle){
				alliance.decreaseProlawNum(1);
			}else if(Title.ELDER == afterTitle){
				alliance.decreaseElderNum(1);
			}
    	    
    	    playerAlliance.leaveAlliance();
    	    battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		}finally{
			lock.unlock();
		}
	    
	    dbService.updateEntityIntime(playerAlliance);
	    dbService.submitUpdate2Queue(alliance);
	    cachedService.removeFromCommonCache(this.getAllianceMemberIdKey(allianceId)); //删除帮派玩家成员缓存
		return true;
	}


	
	@SuppressWarnings("unchecked")
	public boolean appointTitle(PlayerAlliance playerAlliance,PlayerAlliance targetAlliance,Title title) {
		if(playerAlliance == null || targetAlliance == null){
			return false;
		}
		
		if(!playerAlliance.isExistAlliance() || !targetAlliance.isExistAlliance()){
			return false;
		}
		
		if(playerAlliance.getAllianceId() != targetAlliance.getAllianceId()){
			return false;
		}
		
		if(targetAlliance.getTitle() == title){
			return false;
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.APPOINT_TITLE)){
			return false;
		}
		
		if(playerAlliance.getTitle().ordinal() <= targetAlliance.getTitle().ordinal()){
			return false;
		}
		
		if(title.ordinal() >= playerAlliance.getTitle().ordinal()){ //任命玩家职位不得大于等于自己的职位
			return false;
		}
		
		Alliance alliance = this.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null){
			return false;
		}
		
		AllianceConfig allianceConfig = this.getAllianceConfig(alliance.getLevel());
		if(allianceConfig == null){
			return false;
		}
		
		
		ChainLock lock = LockUtils.getLock(alliance,targetAlliance);
		try {
			lock.lock();
			if(targetAlliance.getTitle() == title){
				return false;
			}
			
			//如果之前已经有职位了,将来更改之前的职位人数
			Title afterTitle = targetAlliance.getTitle();
			if(Title.DEPUTYMASTER == afterTitle){
				alliance.decreaseDeputymaster(1);
			}else if(Title.PROLAW == afterTitle){
				alliance.decreaseProlawNum(1);
			}else if(Title.ELDER == afterTitle){
				alliance.decreaseElderNum(1);
			}
			
			
			if(Title.DEPUTYMASTER == title){
				if(alliance.getDeputymasterNum() >= allianceConfig.getDeputymasterNum()){
					return false;
				}
				alliance.increaseDeputymaster(1);
				targetAlliance.setTitle(title);
			}else if(Title.ELDER == title){
				if(alliance.getElderNum() >= allianceConfig.getElderNum()){
					return false;
				}
				alliance.increaseElderNum(1);
				targetAlliance.setTitle(title);
			}else if(Title.PROLAW == title){
				if(alliance.getProlawNum() >= allianceConfig.getProlawNum()){
					return false;
			    }
			    alliance.increaseProlawNum(1);
			    targetAlliance.setTitle(title);
			}else if(Title.MEMBER == title){
				targetAlliance.setTitle(title);
			}
		}finally{
			lock.unlock();
		}
		
		dbService.updateEntityIntime(alliance,targetAlliance);
		cachedService.removeFromCommonCache(this.getAllianceMemberIdKey(alliance.getId())); //删除帮派玩家成员缓存
		return true;
	}


	
	@SuppressWarnings("unchecked")
	public boolean dismissMember(PlayerAlliance playerAlliance,PlayerAlliance targetAlliance,PlayerBattle targetBattle) {
		
		if(playerAlliance == null || targetAlliance == null){
			return false;
		}
		
		if(!playerAlliance.isExistAlliance() || !targetAlliance.isExistAlliance()){
			return false;
		}
		
		if(playerAlliance.getAllianceId() != targetAlliance.getAllianceId()){
			return false;
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.DISMISS_MEMBER)){
			return false;
		}
		
		if(playerAlliance.getTitle().ordinal() <= targetAlliance.getTitle().ordinal()){
			return false;
		}
		
		Alliance alliance = this.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(targetAlliance,alliance,targetBattle);
		try {
			lock.lock();
			//如果之前已经有职位了,将来更改之前的职位人数
			Title afterTitle = targetAlliance.getTitle();
			if(Title.DEPUTYMASTER == afterTitle){
				alliance.decreaseDeputymaster(1);
			}else if(Title.PROLAW == afterTitle){
				alliance.decreaseProlawNum(1);
			}else if(Title.ELDER == afterTitle){
				alliance.decreaseElderNum(1);
			}
			
			targetAlliance.leaveAlliance();
			targetBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		}finally{
			lock.unlock();
		}
		
		dbService.updateEntityIntime(targetAlliance,alliance);
		cachedService.removeFromCommonCache(this.getAllianceMemberIdKey(playerAlliance.getAllianceId())); //删除帮派玩家成员缓存
		return true;
	}


	
	public List<Long> getMembers4Player(long playerId) {
		List<Long> result = new ArrayList<Long>();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return result;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = this.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return result;
		}
		
		List<Long> members = this.getAllianceMembers(playerAlliance.getAllianceId(),false);
		if(members == null){
			return result;
		}
		
		result.addAll(members);
		
		return result;
	}


	
	public boolean joinAlliance(PlayerAlliance playerAlliance, Alliance alliance) {
		if(playerAlliance == null || alliance == null){
			return false;
		}
		
		if(alliance.getState() != AllianceState.ACTIVE){
			return false;
		}
		
		AllianceConfig config = this.getAllianceConfig(alliance.getLevel());
		if(config == null){
			return false;
		}
		
		List<Long> members = this.getAllianceMembers(alliance.getId(),false);
		if(members.size() >= config.getMemberLimit()){
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(playerAlliance,alliance);
		try {
			lock.lock();
			if(members.size() >= config.getMemberLimit()){
				return false;
			}
			if(playerAlliance.isExistAlliance()){
				return false;
			}
			
			playerAlliance.joinAlliance(alliance.getId(), alliance.getName(), Title.MEMBER);
			dbService.updateEntityIntime(playerAlliance);
			cachedService.removeFromCommonCache(this.getAllianceMemberIdKey(playerAlliance.getAllianceId())); //删除帮派玩家成员缓存
		}finally{
			lock.unlock();
		}
		
		return true;
	}

	
	public int sizeAllianceMembers(long allianceId) {
		List<Long> result = this.getAllianceMembers(allianceId,false);
		if(result != null){
			return result.size();
		}else{
			return 0;
		}
	}


	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), PlayerAlliance.class);
	}


	
	public AllianceShopConfig getAllianceShopConfig(int shopId) {
		return resourceService.get(shopId, AllianceShopConfig.class);
	}


	
	public AllianceSkillConfig getAllianceSkillConfig(int researchId) {
		return resourceService.get(researchId, AllianceSkillConfig.class);
	}
	
	/**
	 * 计算玩家的技能属性
	 * @param playerAlliance
	 */
	private void calcAttribute(PlayerAlliance playerAlliance){
		if(playerAlliance == null || !playerAlliance.isFlushable()){
			return;
		}
		
		ChainLock lock = LockUtils.getLock(playerAlliance);
		try {
			lock.lock();
			Fightable fightable = playerAlliance.getAttributes();
			fightable.clear();
			for(Entry<Integer, Integer> entry : playerAlliance.getSkillMap().entrySet()){
				int skillId = entry.getKey();
				int level = entry.getValue();
				AllianceSkillConfig config = allianceBuildService.getAllianceSkillConfig(skillId, level);
				if(config == null){
					continue;
				}
				fightable.add(config.getAttribute(), config.getAttrValue());
			}
			
			playerAlliance.setFlushable(Flushable.FLUSHABLE_NOT);
		}finally{
			lock.unlock();
		}
	}

}
