package com.yayo.warriors.module.rank.manager.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.common.scheduling.ValueType;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.basedb.model.PetConfig;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.rank.dao.RankDao;
import com.yayo.warriors.module.rank.manager.RankManager;
import com.yayo.warriors.module.rank.rule.RankRule;
import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.rank.vo.RankInfoVO;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.syscfg.entity.SystemConfig;
import com.yayo.warriors.module.syscfg.manager.SystemConfigManager;
import com.yayo.warriors.module.syscfg.type.ConfigType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.ElementType;

@Component
public class RankManagerImpl extends CachedServiceAdpter implements RankManager, ApplicationListener<ContextRefreshedEvent>, LogoutListener{
	@Autowired
	private RankDao rankDao;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private SystemConfigManager systemConfigManager;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private HorseManager horseManager;
	@Autowired
	private CampBattleFacade campBattleFacade;
	
	private ConcurrentHashMap<String, List<RankInfoVO>> rankListMap = new ConcurrentHashMap<String, List<RankInfoVO> >(32);
	private AtomicBoolean isOpen = new AtomicBoolean(false);
	private final String RANK_START_SERVER_REFRESH_TIME_MILS  = "RANK_START_SERVER_REFRESH_TIME_MILS";
	private final int FIGHTING_CALC_PLAYER_COUNT = 1000;
	private final ConcurrentHashMap<Long, int[]> PLAYER_TOP_RANK_TITLES = new ConcurrentHashMap<Long, int[]>();
	private final ConcurrentHashSet<Long> PLAYER_NOT_IN_TOP_RANK_TITLES = new ConcurrentHashSet<Long>();
	private final RankType[] RANK_TITLE_TYPES = {null, RankType.PLAYER_LEVEL, RankType.PLAYER_FIGHTING, RankType.PLAYER_MERIDIAN, RankType.PLAYER_MORTAL, RankType.PLAYER_SILVER, RankType.PLAYER_SKILL_LEVEL, 
			RankType.PET_FIGHTING, RankType.HORSE_LEVEL
		};
	@Scheduled(name="排行榜刷新", type= ValueType.BEANNAME, value ="REFRESH_RANK", defaultValue="0 0 0 * * *")
	public void refreshAllRank() {
		if(isOpen.get()){
			long start = System.currentTimeMillis();
			try {
				refreshPlayerLevelRank();		
			} catch (Exception e) {
			}
			try {
				refreshPlayerMeridianRank();	
			} catch (Exception e) {
			}
			try {
				refreshPlayerMortalRank();		
			} catch (Exception e) {
			}
			try {
				refrseshPlayerSilverRank();		
			} catch (Exception e) {
			}
			try {
				refreshPlayerSkillLevelsRank();	
			} catch (Exception e) {
			}
			try {
				refreshHorseRank();				
			} catch (Exception e) {
			}
			try {
				refreshPetFightingRank();		
			} catch (Exception e) {
			}
			try {
				refreshPlayerFightingRank();	
			} catch (Exception e) {
			}
			long end = System.currentTimeMillis();
			clearPlayerRankTitles();
			Collection<Long> onlinePlayerIdList = sessionManager.getOnlinePlayerIdList();
			freshRankTitle(onlinePlayerIdList);
		} 
	}

	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event != null){
			SystemConfig systemConfig = systemConfigManager.getSystemConfig(ConfigType.RANK_OPEN);
			if(systemConfig != null){
				String info = systemConfig.getInfo();
				isOpen.compareAndSet(false, StringUtils.isNotBlank(info) && info.trim().equals("1") );
			}
			new Thread(new Runnable() {
				public void run() {
					refreshAllRank();
				}
			}, "启服计算排行榜").start();
		}
	}
	
	
	public void checkRankOpen(int playerLevel) {
		if(!isOpen.get() && playerLevel >= RankRule.RANK_OPEN_PLAYER_LEVEL_LIMIT){
			isOpen.set(true);
			systemConfigManager.updateSystemConfig(ConfigType.RANK_OPEN, "1");
			cachedService.put2CommonCache(RANK_START_SERVER_REFRESH_TIME_MILS, System.currentTimeMillis(), TimeConstant.ONE_HOUR_MILLISECOND);
			refreshAllRank();
			
		}
	}
	private void refreshPlayerLevelRank(){
		String propertyName = "level";
		Map<Long, RankInfoVO> voMap = new HashMap<Long, RankInfoVO>(RankRule.RANK_LENGTH);
		for(Job job : Job.values()){
			DetachedCriteria dc = DetachedCriteria.forClass(PlayerBattle.class);
					if(job != Job.COMMON){
						dc.add(Restrictions.eq("job", job));
					}
					dc.add(Restrictions.ge(propertyName, RankRule.RANK_PLAYER_LEVEL_LIMIT))
					.addOrder(Order.desc(propertyName))
					.addOrder(Order.desc("exp"))
					.addOrder(Order.asc("id"));
			int fetchCount = job == Job.COMMON ? RankRule.RANK_LENGTH : RankRule.JOB_RANK_LENGTH;
			List<PlayerBattle> sources = rankDao.listRankSources(dc, -1, fetchCount);
			List<RankInfoVO> list = new ArrayList<RankInfoVO>();
			if(sources != null && sources.size() > 0 ) {
				int rankIndex = 0;
				for(PlayerBattle playerBattle : sources){
					RankInfoVO vo = voMap.get(playerBattle.getId());
					if(vo == null){
						vo = new RankInfoVO();
						voMap.put(playerBattle.getId(), vo);
						
						UserDomain userDomain = userManager.getUserDomain( playerBattle.getId() );
						if(userDomain != null){
							Player player = userDomain.getPlayer();
							vo.setPlayerId(player.getId());
							vo.setName( player.getName() );
							vo.setCamp(player.getCamp().ordinal());
							vo.setSex(player.getSex().ordinal());
						}
						Alliance alliance = allianceManager.getAlliance4PlayerId( playerBattle.getId() );
						if(alliance != null){
							vo.setName2(alliance.getName());
						}
						vo.setJob(playerBattle.getJob().ordinal());
						vo.setLevel(playerBattle.getLevel());
					}
					if(job == Job.COMMON){
						vo.setRankIndex(++rankIndex);
					}
					list.add(vo);
				}
			}
			
			String key = buildRankKey(RankType.PLAYER_LEVEL, job);
			rankListMap.put(key, list);
		}
	}
	
	private void refreshPlayerFightingRank(){
		
		DetachedCriteria dc = DetachedCriteria.forClass(PlayerBattle.class);
		dc.add(Restrictions.ge("level", RankRule.RANK_PLAYER_FIGHTING_LIMIT))
			.setProjection(Projections.id())
			.addOrder(Order.desc("level"))
			.addOrder(Order.asc("id"));
		
		List<Long> playerIds = rankDao.listRankSources(dc, -1, FIGHTING_CALC_PLAYER_COUNT);
		if(playerIds == null || playerIds.isEmpty()){
			return ;
		}
		
		List<RankInfoVO> list = new ArrayList<RankInfoVO>();
		for(long playerId : playerIds){
			UserDomain userDomain = userManager.getUserDomain(playerId);
			Player player = userDomain.getPlayer();
			PlayerBattle battle = userDomain.getBattle();
			int fightCapacity = battle.getAttribute(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY);
			RankInfoVO vo = new RankInfoVO();
			vo.setPlayerId(playerId);
			vo.setName(player.getName());
			vo.setSex(player.getSex().ordinal());
			vo.setJob(battle.getJob().ordinal());
			vo.setCamp(player.getCamp().ordinal());
			vo.setValue(fightCapacity);
			
			Alliance alliance = allianceManager.getAlliance4PlayerId( playerId );
			if(alliance != null){
				vo.setName2(alliance.getName());
			}
			
			list.add(vo);
		}
		
		Collections.sort(list, new Comparator<RankInfoVO>() {
			
			public int compare(RankInfoVO vo1, RankInfoVO vo2) {
				if(vo1.getValue() >  vo2.getValue()){
					return -1;
				} else if(vo1.getValue() <  vo2.getValue() ){
					return 1;
				}
				return 0;
			}
			
		});
		
		Job[] jobs = Job.values();
		Map<Job, List<RankInfoVO>> ranksMap = new HashMap<Job, List<RankInfoVO> >(jobs.length);
		int complete = 0;	
		for(RankInfoVO vo : list){
			
			for(Job job : jobs){
				int fetchCount = job == Job.COMMON ? RankRule.RANK_LENGTH : RankRule.JOB_RANK_LENGTH;
				List<RankInfoVO> rankList = ranksMap.get(job);
				if(rankList == null) {
					rankList = new ArrayList<RankInfoVO>(fetchCount);
					ranksMap.put(job, rankList);
				}
				
				if(rankList.size() < fetchCount){
					if( job == Job.COMMON || vo.getJob() == job.ordinal() ){
						rankList.add(vo);
					}
				} else {
					complete++;
					break;
				}
				
			}
			
			if(complete >= jobs.length){
				break;
			}
		}
		
		for(Job job : jobs){
			List<RankInfoVO> rankList = ranksMap.get(job);
			if(rankList != null){
				String key = buildRankKey(RankType.PLAYER_FIGHTING, job);
				rankListMap.put(key, rankList );
				if(job == Job.COMMON){
					int rankIndex = 0;
					for(RankInfoVO vo : rankList){
						vo.setRankIndex(++rankIndex);
					}
				}
			}
		}
		
	}

	private void refreshPlayerMeridianRank(){
		Map<Long, RankInfoVO> voMap = new HashMap<Long, RankInfoVO>(RankRule.RANK_LENGTH);
		for(Job job : Job.values()) {
			int fetchCount = job == Job.COMMON ? RankRule.RANK_LENGTH : RankRule.JOB_RANK_LENGTH;
			StringBuilder sql = new StringBuilder("select meridian.* from meridian meridian ");
			sql.append(" left join playerBattle on playerBattle.playerId = meridian.playerId ");
			sql.append(" where meridian.meridianIds >= ").append(RankRule.RANK_PLAYER_MERIDIAN_LIMIT);
			if(job != Job.COMMON){
				sql.append(" and playerBattle.job = ").append(job.ordinal());
			}
			sql.append(" order by meridian.meridianIds desc, playerBattle.level desc, meridian.playerId asc ");
			
			List<Meridian> sources = rankDao.listRankSources(sql.toString(), Meridian.class, -1, fetchCount);
			List<RankInfoVO> list = new ArrayList<RankInfoVO>();
			int rankIndex = 0;
			if(sources != null && sources.size() > 0 ) {
				for(Meridian meridian : sources){
					RankInfoVO vo = voMap.get(meridian.getId());
					if(vo == null){
						vo = new RankInfoVO();
						voMap.put(meridian.getId(), vo);
						UserDomain userDomain = userManager.getUserDomain( meridian.getId() );
						if(userDomain != null){
							Player player = userDomain.getPlayer();
							vo.setPlayerId( player.getId() );
							vo.setName( player.getName() );
							vo.setCamp(player.getCamp().ordinal());
							vo.setSex(player.getSex().ordinal());
							
							PlayerBattle playerBattle = userDomain.getBattle();
							if(playerBattle != null){
								vo.setJob(playerBattle.getJob().ordinal());
								vo.setLevel(playerBattle.getLevel());
							}
						}
						vo.setValue(meridian.getMeridianIds());
						Alliance alliance = allianceManager.getAlliance4PlayerId( meridian.getId() );
						if(alliance != null){
							vo.setName2(alliance.getName());
						}
					}
					if(job == Job.COMMON){
						vo.setRankIndex(++rankIndex);
					}
					list.add(vo);
				}
			}
			
			String key = buildRankKey(RankType.PLAYER_MERIDIAN, job);
			rankListMap.put(key, list);
		}
	}
	
	private void refreshPlayerMortalRank(){
		Map<Long, RankInfoVO> voMap = new HashMap<Long, RankInfoVO>(RankRule.RANK_LENGTH);
		for(Job job : Job.values()){
			int fetchCount = job == Job.COMMON ? RankRule.RANK_LENGTH : RankRule.JOB_RANK_LENGTH;
			StringBuilder sql = new StringBuilder("select userMortalBody.* from userMortalBody userMortalBody ");
			sql.append(" left join playerBattle on playerBattle.playerId = userMortalBody.playerId ");
			sql.append(" where userMortalBody.mortalLevel > ").append(RankRule.RANK_PLAYER_MORTAL_LIMIT);
			if(job != Job.COMMON){
				sql.append(" and playerBattle.job = ").append(job.ordinal());
			}
			sql.append(" order by userMortalBody.mortalLevel desc, playerBattle.level desc, userMortalBody.playerId asc ");
			
			List<UserMortalBody> sources = rankDao.listRankSources(sql.toString(), UserMortalBody.class, -1, fetchCount);
			List<RankInfoVO> list = new ArrayList<RankInfoVO>();
			if(sources != null && sources.size() > 0 ) {
				int rankIndex = 0;
				for(UserMortalBody userMortalBody : sources){
					RankInfoVO vo = voMap.get(userMortalBody.getId());
					if(vo == null){
						vo = new RankInfoVO();
						voMap.put(userMortalBody.getId(), vo);
						UserDomain userDomain = userManager.getUserDomain( userMortalBody.getId() );
						if(userDomain != null){
							Player player = userDomain.getPlayer();
							vo.setPlayerId( player.getId() );
							vo.setName( player.getName() );
							vo.setCamp(player.getCamp().ordinal());
							vo.setSex(player.getSex().ordinal());
							
							PlayerBattle playerBattle = userDomain.getBattle();
							if(playerBattle != null){
								vo.setJob(playerBattle.getJob().ordinal());
								vo.setLevel(playerBattle.getLevel());
							}
						}
						vo.setValue(userMortalBody.getMortalLevel());
						Alliance alliance = allianceManager.getAlliance4PlayerId( userMortalBody.getId() );
						if(alliance != null){
							vo.setName2(alliance.getName());
						}
					}
					if(job == Job.COMMON){
						vo.setRankIndex(++rankIndex);
					}
					
					list.add(vo);
				}
			}
			
			String key = buildRankKey(RankType.PLAYER_MORTAL, job);
			rankListMap.put(key, list);
		}
	}
	
	
	private void refreshPlayerSkillLevelsRank(){
		Map<Long, RankInfoVO> voMap = new HashMap<Long, RankInfoVO>(RankRule.RANK_LENGTH);
		for(Job job : Job.values()) {
			int fetchCount = job == Job.COMMON ? RankRule.RANK_LENGTH : RankRule.JOB_RANK_LENGTH;
			StringBuilder sql = new StringBuilder("select userSkill.* from userSkill userSkill ");
			sql.append(" left join playerBattle on playerBattle.playerId = userSkill.playerId ");
			sql.append(" where userSkill.skillLevels >= ").append(RankRule.RANK_SKILL_LEVEL_LIMIT);
			if(job != Job.COMMON){
				sql.append(" and playerBattle.job = ").append(job.ordinal());
			}
			sql.append(" order by userSkill.skillLevels desc, playerBattle.level desc, userSkill.playerId asc ");
			
			List<UserSkill> sources = rankDao.listRankSources(sql.toString(), UserSkill.class, -1, fetchCount);
			List<RankInfoVO> list = new ArrayList<RankInfoVO>();
			if(sources != null && sources.size() > 0 ){
				int rankIndex = 0;
				for(UserSkill userSkill : sources){
					RankInfoVO vo = voMap.get(userSkill.getId());
					if(vo == null){
						vo = new RankInfoVO();
						voMap.put(userSkill.getId(), vo);
						UserDomain userDomain = userManager.getUserDomain(userSkill.getId());
						Player player = userDomain.getPlayer();
						vo.setPlayerId( player.getId() );
						vo.setName(player.getName());
						vo.setSex(player.getSex().ordinal());
						vo.setJob(userDomain.getBattle().getJob().ordinal());
						vo.setCamp(player.getCamp().ordinal());
						vo.setValue(userSkill.getSkillLevels());
					}
					if(job == Job.COMMON){
						vo.setRankIndex(++rankIndex);
					}
					
					list.add(vo);
				}
				
			}
			
			String key = buildRankKey(RankType.PLAYER_SKILL_LEVEL, job);
			rankListMap.put(key, list);
		}
	}
	
	private void refreshPetFightingRank() {
		StringBuilder sql = new StringBuilder("select petBattle.* from userPetBattle petBattle ");
		sql.append(" left join userPet on userPet.petId = petBattle.petId ");
		sql.append(" where (userPet.status >= 0 and userPet.status <= 2)  and petBattle.fighting >= ").append(RankRule.RANK_PET_FIGHTING_LIMIT);
		sql.append(" order by petBattle.fighting desc, petBattle.level desc, petBattle.petId asc ");	
		List<PetBattle> sources = rankDao.listRankSources(sql.toString(), PetBattle.class, -1, RankRule.RANK_LENGTH);
		List<RankInfoVO> list = new ArrayList<RankInfoVO>();
		if(sources != null && sources.size() > 0 ) {
			int rankIndex = 0;
			for(PetBattle petBattle : sources){
				RankInfoVO vo = new RankInfoVO();
				vo.setLevel( petBattle.getLevel() );
				
				Pet pet = get(petBattle.getId(), Pet.class);
				if(pet != null){
					PetConfig petConfig = resourceService.get(pet.getBaseId(), PetConfig.class);
					if(petConfig == null){
						continue;
					}
					vo.setName( petConfig.getName() );
					UserDomain userDomain = userManager.getUserDomain(pet.getPlayerId());
					if(userDomain != null){
						Player player = userDomain.getPlayer();
						vo.setPlayerId( player.getId() );
						vo.setName2( player.getName() );
						vo.setCamp(player.getCamp().ordinal());
					}
				}
				
				vo.setBaseId(pet.getBaseId());
				vo.setAutoId(pet.getId());
				vo.setValue( petBattle.getFighting() );
				
				vo.setRankIndex(++rankIndex);
					
				list.add(vo);
			}
		}
		
		String key = buildRankKey(RankType.PET_FIGHTING, null);
		rankListMap.put(key, list);
	}
	
	private void refreshHorseRank(){
		StringBuilder sql = new StringBuilder("select horse.* from userHorse horse ");
		sql.append(" left join playerBattle on playerBattle.playerId = horse.playerId ");
		sql.append(" where horse.level >= ").append(RankRule.RANK_HORSE_LEVEL_LIMIT);
		sql.append(" order by horse.level desc, playerBattle.level desc, horse.playerId asc ");		
		
		List<Horse> sources = rankDao.listRankSources(sql.toString(), Horse.class, -1, RankRule.RANK_LENGTH);
		List<RankInfoVO> list = new ArrayList<RankInfoVO>();
		if(sources != null && sources.size() > 0 ){
			int rankIndex = 0;
			for(Horse horse : sources){
				RankInfoVO vo = new RankInfoVO();
				HorseConfig horseConfig = resourceService.get(horse.getLevel(), HorseConfig.class);
				if(horseConfig != null){
					vo.setName(horseConfig.getName());
					UserDomain userDomain = userManager.getUserDomain( horse.getId() );
					if(userDomain != null){
						Player player = userDomain.getPlayer();
						vo.setPlayerId( player.getId() );
						vo.setName2(player.getName());
						vo.setCamp(player.getCamp().ordinal());
						vo.setJob(userDomain.getBattle().getJob().ordinal());
					}
					vo.setLevel(horse.getLevel());
				}
				vo.setRankIndex(++rankIndex);
				
				list.add(vo);
			}
			
		}
		
		String key = buildRankKey(RankType.HORSE_LEVEL, null);
		rankListMap.put(key, list);
	}
	private void refrseshPlayerSilverRank(){
		Map<Long, RankInfoVO> voMap = new HashMap<Long, RankInfoVO>(RankRule.RANK_LENGTH);
		for(Job job : Job.values()){
			int fetchCount = job == Job.COMMON ? RankRule.RANK_LENGTH : RankRule.JOB_RANK_LENGTH;
			StringBuilder sql = new StringBuilder("select player.* from player player ");
			sql.append(" left join playerBattle on playerBattle.playerId = player.playerId ");
			sql.append(" where player.silver >= ").append(RankRule.RANK_WEALTH_SILVER_LIMIT);
			if(job != Job.COMMON){
				sql.append(" and playerBattle.job = ").append(job.ordinal());
			}
			sql.append(" order by player.silver desc, playerBattle.level desc, player.createTime asc ");		
			
			List<Player> sources = rankDao.listRankSources(sql.toString(), Player.class, -1, fetchCount);
			List<RankInfoVO> list = new ArrayList<RankInfoVO>();
			String key = buildRankKey(RankType.PLAYER_SILVER, job);
			if(sources != null && sources.size() > 0 ){
				int rankIndex = 0;
				for(Player player : sources){
					RankInfoVO vo = voMap.get(player.getId());
					if(vo == null){
						vo = new RankInfoVO();
						voMap.put(player.getId(), vo);
						vo.setPlayerId( player.getId() );
						vo.setName(player.getName());
						vo.setSex(player.getSex().ordinal());
						
						UserDomain userDomain = userManager.getUserDomain(player.getId());
						if(userDomain != null){
							PlayerBattle battle = userDomain.getBattle();
							if(battle != null){
								vo.setJob(battle.getJob().ordinal());
							}
						}
						vo.setCamp( player.getCamp().ordinal() );
						vo.setValue( player.getSilver() );
					}
					if(job == Job.COMMON){
						vo.setRankIndex(++rankIndex);
					}
					list.add(vo);
				}
				
				rankListMap.put(key, list);
				
			} else {
				rankListMap.remove(key);
			}
		}
	}

	
	public List<RankInfoVO> getRankVO(RankType rankType, Job job) {
		String key = buildRankKey(rankType, job);
		return rankListMap.get(key);
	}

	private String buildRankKey(RankType rankType, Job job){
		return new StringBuilder().append(rankType.name()).append(Splitable.ATTRIBUTE_SPLIT).append(job != null ? job.ordinal() : Job.COMMON.ordinal() ).toString();
	}
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		clearPlayerRankTitles(userDomain.getPlayerId());
	}

	private void clearPlayerRankTitles(long... playerId) {
		if(playerId != null && playerId.length > 0){
			if(PLAYER_TOP_RANK_TITLES.remove(playerId[0]) == null){
				PLAYER_NOT_IN_TOP_RANK_TITLES.remove(playerId[0]);
			}
		} else {
			PLAYER_TOP_RANK_TITLES.clear();
			PLAYER_NOT_IN_TOP_RANK_TITLES.clear();
		}
	}

	
	public int[] getRankTitleByPlayerId(long playerId) {
		int[] ranks = PLAYER_TOP_RANK_TITLES.get(playerId);
		if(ranks != null){
			return ranks;
			
		}else if(PLAYER_NOT_IN_TOP_RANK_TITLES.contains(playerId)){
			return null;
		}
		int rankFlag = 0;
		ranks = new int[ RANK_TITLE_TYPES.length ];
		for(int i = 0; i < RANK_TITLE_TYPES.length; i++){
			RankType rankType = RANK_TITLE_TYPES[i];
			if(rankType == null){
				continue;
			}
			List<RankInfoVO> values = this.getRankVO(rankType, Job.COMMON);
			if(values == null){
				continue;
			}
			
			int len = Math.min( RankRule.RANK_TOP, values.size() );		//前10名
			for(int r = 0; r < len; r++){
				RankInfoVO rankInfoVO = values.get(r);
				if(rankInfoVO.getPlayerId() == playerId){
					ranks[i] = r + 1;
					rankFlag++;
					break;
				}
			}
		
		}
		
		CampTitle campBattleTitle = campBattleFacade.getCampBattleTitle(playerId, null);
		ranks[0] = campBattleTitle == null ? 0 : campBattleTitle.ordinal();
		
		if(rankFlag != 0 || ranks[0] > 0){
			PLAYER_TOP_RANK_TITLES.put(playerId, ranks);
			return ranks;
		} else {
			PLAYER_NOT_IN_TOP_RANK_TITLES.add(playerId);
		}
		return null;
	}

	
	public void freshRankTitle(Collection<Long> playerIds) {
		if(playerIds != null && playerIds.size() > 0 ){
			for(Long playerId : playerIds){
				clearPlayerRankTitles(playerId);
				Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
				List<UnitId> playerUnits = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
				UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, playerUnits, AttributeKeys.RANK_TITLE, AttributeKeys.CAMP_TITLE);
			}
		}
	}
	
}
