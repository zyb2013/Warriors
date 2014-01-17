package com.yayo.warriors.module.active.facade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.warriors.basedb.model.ActiveBossConfig;
import com.yayo.warriors.basedb.model.ActiveDungeonConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.module.active.facade.ActiveFacade;
import com.yayo.warriors.module.active.manager.ActiveManager;
import com.yayo.warriors.module.active.rule.ActiveRule;
import com.yayo.warriors.module.active.vo.ActiveBossRefreshVO;
import com.yayo.warriors.module.active.vo.ActiveDungeonVO;
import com.yayo.warriors.module.active.vo.ActiveTaskVO;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.task.entity.UserAllianceTask;
import com.yayo.warriors.module.task.entity.UserCampTask;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.task.entity.UserLoopTask;
import com.yayo.warriors.module.task.entity.UserPracticeTask;
import com.yayo.warriors.module.task.manager.AllianceTaskManager;
import com.yayo.warriors.module.task.manager.CampTaskManager;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.task.manager.LoopTaskManager;
import com.yayo.warriors.module.task.manager.PracticeTaskManager;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 活动接口实现类
 * 
 * @author huachaoping
 */
@Component
public class ActiveFacadeImpl implements ActiveFacade {
	
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private ActiveManager activeManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private LoopTaskManager loopTaskManager;
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private PracticeTaskManager practiceTaskManager;
	@Autowired
	private CampTaskManager campTaskManager;
	@Autowired
	private AllianceTaskManager allianceTaskManager;
	@Autowired
	private ResourceService resourceService;
	
	
	
	public List<ActiveBossRefreshVO> monsterRefreshActive(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return null;
		}
		
		List<ActiveBossRefreshVO> voList = new ArrayList<ActiveBossRefreshVO>(1);
		Collection<ActiveBossConfig> configs = resourceService.listAll(ActiveBossConfig.class);
		for (ActiveBossConfig config : configs) {
			MonsterConfig mConfig = resourceService.get(config.getMonsterId(), MonsterConfig.class);
			MonsterFightConfig monsterConfig = resourceService.get(mConfig.getMonsterFightId(), MonsterFightConfig.class);
			String monsterKiller = activeManager.getMonsterKiller(userDomain.getBranching(), monsterConfig.getId());
			ActiveBossRefreshVO vo = ActiveBossRefreshVO.valueOf(config.getId(), config.getMonsterId(), monsterKiller);
			if (monsterConfig.getBossConfigs() == null) {
				long monsterAutoId = activeManager.getMonsterAutoId(userDomain.getBranching(), monsterConfig.getId());
				MonsterDomain monsterDomain = monsterManager.getMonsterDomain(monsterAutoId);
				long refreshTime = monsterDomain.getTired();
				refreshTime = refreshTime - System.currentTimeMillis() > 0 ? refreshTime - System.currentTimeMillis() : 0;
				String refresh = new StringBuilder().append("#").append(refreshTime).toString();   // 这个客户端要区分...怪物刷新类型
				vo.setFreshTime(refresh); 
			} else {
				vo.setFreshTime(monsterConfig.getBossConfigs());
			}
			voList.add(vo);
		}
		return voList;
	}

	
	
	
	public List<ActiveDungeonVO> dailyDungeonActive(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return null;
		}
		
		List<ActiveDungeonVO> voList = new ArrayList<ActiveDungeonVO>(1);
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		Collection<ActiveDungeonConfig> configs = resourceService.listAll(ActiveDungeonConfig.class);
		for (ActiveDungeonConfig config : configs) {
			int times = playerDungeon.getEnterDungeonTimes(config.getDungeonId());
			ActiveDungeonVO vo = ActiveDungeonVO.valueOf(config.getId(), config.getDungeonId(), times);
			voList.add(vo);
		}
		return voList;
	}

	
	
	public List<ActiveTaskVO> dailyTaskActive(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return null;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserLoopTask loopTask = loopTaskManager.getUserLoopTask(playerId);
		UserCampTask campTask = campTaskManager.getUserCampTask(playerId);
		UserEscortTask escortTask = escortTaskManager.getEscortTask(battle);
		UserPracticeTask practiceTask = practiceTaskManager.getPracticeTask(userDomain);
		UserAllianceTask allianceTask = allianceTaskManager.getUserAllianceTask(battle);
		
		int loopCompletes = loopTask.getCompletes();
		boolean loopAccepted = loopTask.isAccepted();
		int practiceCompletes = practiceTask.getCompletes();
		boolean practiceAccepted = practiceTask.isAccepted();
		boolean camTaskAccepted = campTask != null && campTask.isAccepted();
		boolean escortAccepted = escortTask != null && escortTask.isAccepted();
		int escortCompletes = escortTask == null ? 0 : escortTask.getActionTimes();
		int campTaskCompletes = campTask == null ? 0 : campTask.getRewardsTasks().size();
		boolean allianceAccepted = allianceTask != null && allianceTask.getTasks().size() > 0;
		
		int allianceCompletes = 0;
		if (allianceTask != null) {
			for (int taskCount : allianceTask.getRewardsTasks().values()) {
				allianceCompletes += taskCount;
			}
		}
		
		List<ActiveTaskVO> voList = new ArrayList<ActiveTaskVO>(4);
		voList.add(ActiveTaskVO.valueOf(ActiveRule.ACTIVE_LOOP_ID, loopCompletes, loopAccepted));
		voList.add(ActiveTaskVO.valueOf(ActiveRule.ACITVE_CAMP_ID, campTaskCompletes, camTaskAccepted));
		voList.add(ActiveTaskVO.valueOf(ActiveRule.ACTIVE_ESCORT_ID, escortCompletes, escortAccepted));
		voList.add(ActiveTaskVO.valueOf(ActiveRule.ACTIVE_PRATICE_ID, practiceCompletes, practiceAccepted));
		voList.add(ActiveTaskVO.valueOf(ActiveRule.ACTIVE_ALLIANCE_ID, allianceCompletes, allianceAccepted));
		return voList;
	}


}
