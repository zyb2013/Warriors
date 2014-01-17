package com.yayo.warriors.module.monster.facade.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.TaskService;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.TaskMonsterConfig;
import com.yayo.warriors.module.active.manager.ActiveManager;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.monster.action.MonsterAction;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.entity.MonsterInfo;
import com.yayo.warriors.module.monster.entity.MonsterInfo.DeathInfo;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.monster.model.Monster;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

@Component
public class MonsterFacadeImpl implements MonsterFacade {
	@Autowired
	private TaskService taskService;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private MonsterAction monsterAction;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private ResourceService resourceService;  //源数据
	@Autowired
	private GameMapManager gameMapManager; ///地图
	@Autowired
	private ActiveManager activeManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	
	/** 日志 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	
	public MonsterDomain getMonsterDomain(Long id) {
		return this.monsterManager.getMonsterDomain(id);
	}

	//这样创建不太好...以后改
	public void initMonster() {
		
		Collection<MonsterConfig> configCollection =  resourceService.listAll(MonsterConfig.class);
		if(configCollection == null){
			return;
		}
	   
		int totalCreateNum = 0 ;
		CopyOnWriteArrayList<Integer> branchSize = channelFacade.getCurrentBranching();
		for (MonsterConfig monsterConfig : configCollection) {
			int monsterBaseId = monsterConfig.getId();
			int monsterFightId = monsterConfig.getMonsterFightId();
			MonsterFightConfig monsterFight = this.getMonsterFightConfig(monsterFightId);
			if (monsterFight == null) {
				LOGGER.error("怪物的战斗信息基础表不存在,id:[{}]", monsterBaseId);
				continue;
			}

			int mapId = monsterConfig.getMapId();
			for(int branching : branchSize){
				GameMap gameMap = this.gameMapManager.getGameMapById(mapId, branching);
				if(gameMap == null){
					continue;
				}
				boolean update = false;
				MonsterInfo monsterInfo = monsterManager.getMonsterInfo(branching);
				ConcurrentMap<Integer, DeathInfo> resurrectionMap = monsterInfo.getResurrectionMap();
				
				Monster monster = Monster.valueOf(branching, monsterFight);
				MonsterBattle battle = MonsterBattle.valueOf(monster.getId(), monsterFight);
				MonsterDomain monsterDomain = monsterManager.addMonster(gameMap, monster, monsterConfig, battle);
				activeManager.put2Monsters(branching, monster);   // 增加怪物缓存
				if(monsterDomain != null){
					if( monsterDomain.getMonsterBattle().isDead() ){
						LOGGER.error("增加新怪物时出错(HP为0), 怪物基础id:{} 怪物战斗属性id:{}", monsterBaseId, monsterFightId);
						continue;
					}
					
					if(monsterFight.isBoss()){
						DeathInfo deathInfo = resurrectionMap.get(monsterBaseId);
						if(deathInfo != null){
							long reviveTime = deathInfo.reviveTime - System.currentTimeMillis();
							if(reviveTime > 10){
								monsterDomain.addTired(MonsterDomain.RESURRECTION_TIME, reviveTime);
								if(!monsterDomain.isTimeToResurrection()){
									monsterDomain.getMonsterBattle().setHp(0);
									UserDomain userDomain = userManager.getUserDomain(deathInfo.monsterKiller);
									if(userDomain != null){
										activeManager.modifyMonsterKiller(branching, monsterFight.getId(), userDomain.getPlayer().getName());
									}
								}
							} else {
								monsterInfo.removeResurrection(monsterBaseId);
								update = true;
							}
						}
					}
					
					gameMap.enterMap(monsterDomain);
					totalCreateNum++;
				}
				
				if(update){
					dbService.submitUpdate2Queue(monsterInfo);
				}
			}
		}
		LOGGER.info("{}线的怪创建完毕,共有{}只", branchSize, totalCreateNum);
		monsterAction.loadMonsterFinish();
		LOGGER.info("怪物创建完毕^_^");
	}
	
	/**
	 * 获取怪物配置
	 * @param monsterId    怪物原型ID
	 * @return
	 */
	public MonsterConfig getMonsterConfig(int monsterId){
		return this.resourceService.get(monsterId, MonsterConfig.class);
	}
	
	/**
	 * 获取怪物战斗属性配置
	 * @param monsterFightId  怪物战斗配置ID
	 * @return
	 */
	
	public MonsterFightConfig getMonsterFightConfig(int monsterFightId){
		return this.resourceService.get(monsterFightId, MonsterFightConfig.class);
	}

	/**
     * 随机获得一只怪物
     * 
     * @param  classification				怪物所在的区域
     * @param  monsterType					怪物的类型
     * @param  playerLevel					角色的等级
     * @return {@link MonsterConfig}		怪物信息
     */
	
	public MonsterConfig getRandomMonsterFight(int playerLevel) {
		int minLevel = Math.max(0, playerLevel - 5);
		int maxLevel = Math.max(0, playerLevel + 5);
		MonsterConfig monsterConfig = null;
		for(int i = 0 ; i < 10 ; i++){ //10次的随机保证他能够随机到值,只随机一次会出现问题
			List<TaskMonsterConfig> monsters = new LinkedList<TaskMonsterConfig>();
			for (int level = minLevel; level < maxLevel; level++) {
				monsters.addAll(taskService.getTaskMonsterConfigs(level));
			}
			
			if(monsters == null || monsters.isEmpty()) {
				return null;
			}
			
			TaskMonsterConfig taskMonsters = monsters.get(Tools.getRandomInteger(monsters.size()));
			if(taskMonsters == null) {
				return null;
			}
			
			monsterConfig = taskMonsters.getRandomMonster();
			if(monsterConfig != null){
				return monsterConfig;
			}
		}
		return monsterConfig;
	}

}
