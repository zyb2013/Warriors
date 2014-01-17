package com.yayo.warriors.module.dungeon.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.basedb.model.MonsterDungeonConfig;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.types.DungeonState;
import com.yayo.warriors.module.dungeon.types.DungeonType;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.ElementType;

/**
 * 每波怪物都死亡以后刷新 
 * @author liuyuhua
 */
@Component
public class RoundDungeonRule extends BaseDungeonRule{
	
	@Override
	public int getDungeonType() {
		return DungeonType.ROUND;
	}

	@Override
	public void action(Dungeon dungeon) {
		if(dungeon == null){
			logger.error("副本对象为空");
			return;
		}
		
		if(this.validation(dungeon)){
			return;
		}
		
		if(dungeon.getState() == DungeonState.SUCCESS){
			return;
		}
		
		int dungeonBaseId = dungeon.getBaseId(); //副本基础ID
		DungeonConfig dungeonConfig = this.dungeonManager.getDungeonConfig(dungeonBaseId);
		if(dungeonConfig == null){
			logger.error("副本[{}]基础数据不存在", dungeonBaseId);
			return;
		}
		
		
		/** 检测副本完成*/
		int totleCount = dungeonConfig.getTotleRoundCount(); //总回合数
		int currentCount = dungeon.getRoundCount(); //当前回合数
		if(currentCount >= totleCount){
			this.checkComplete(dungeon);
		}else{
			
			/* 怪物还没有杀完不能开始下一波*/
			if(!dungeon.isEmpty4Monster()){
				return;
			}
			
			Collection<Long> playerIds = dungeon.filterPlayers();
			/* 所有怪杀死,当前回合完成*/
			this.roundCompletedEvent(playerIds, dungeon.getRoundCount());
			
			long currentDate = DateUtil.getCurrentSecond();
			long nextRound = dungeon.getNextRoundDate();
			
			//到达了下一波开始的时间后,开始生成怪物
			if(currentDate >= nextRound){
				int round = dungeon.getRoundCount() + 1; //下一波的数据
				
				List<MonsterDungeonConfig> dungeonMonsterConfigs = monsterService.getMonsters4Round(dungeonBaseId , round);
				if(dungeonMonsterConfigs == null){
					logger.error("副本基础ID[{}],副本类型[{}],第[{}]回合的怪物配置不存在.",new Object[]{dungeonBaseId,dungeon.getType(),round});
					return;
				}
				
				int branching = dungeon.getBranching();
				long dungeonId = dungeon.getDungeonId();
				GameMap map = this.gameMapManager.getTemporaryMap(dungeonId, branching);
				if(map == null){
					logger.error("副本基础ID[{}],第[{}]回合,副本地图[{}]不存在,无法创建下一回合怪物",new Object[]{dungeonBaseId,round,dungeonId});
					return;
				}
				
				
				dungeon.setRoundCount(round);
				List<Long> monsterIds = new ArrayList<Long>();         //怪物初始化
				this.acceptRoundTask(playerIds, round, dungeonBaseId); //接受副本回合任务
				Collection<ISpire> allSpireIdCollection = map.getAllSpireCollection(ElementType.PLAYER);
				for(MonsterDungeonConfig monsterConfig : dungeonMonsterConfigs){
					MonsterDomain monsterDomain = this.monsterManager.addDungeonMonster(map, monsterConfig, dungeonId, branching);
					if(monsterDomain != null){
						if( monsterDomain.getMonsterBattle().isDead() ){
							logger.error("增加副本新怪物时出错(HP为0), 怪物基础id:{} 怪物战斗属性id:{}", monsterConfig.getId(), monsterConfig.getMonsterFightId() );
							continue;
						}
						monsterIds.add(monsterDomain.getId());
					}
					for(ISpire spire : allSpireIdCollection){
						UserDomain userDomain = (UserDomain)spire ;
						userDomain.putCanViewSpire(monsterDomain);
					}
				}
				worldPusherHelper.putMessage2Queue(allSpireIdCollection);
				Map<String,Object> msg = dungeon.addDungeonMonster(round, monsterIds);
				if(msg != null){
					this.dungeonPushHelper.pushDungeonStatistics(dungeon.filterPlayers(), msg);
				}
			}
		}
		
	}
	
	/**
	 * 检测副本完成
	 * @param dungeon
	 */
	private void checkComplete(Dungeon dungeon){
		this.doSuccess(dungeon);
	}

}
