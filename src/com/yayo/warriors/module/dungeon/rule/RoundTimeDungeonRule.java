package com.yayo.warriors.module.dungeon.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
 * 固定时间刷新每波怪物的玩法
 * @author liuyuhua
 */
@Component
public class RoundTimeDungeonRule extends BaseDungeonRule{
	
	@Override
	public int getDungeonType() {
		return DungeonType.ROUND_INTIME;
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
			Map<Integer,Integer> intime = dungeonConfig.getRoundIntimes();
			if(intime == null || intime.isEmpty()){
				logger.error("副本[{}],没有时间推进所需要的回合数据.",dungeonBaseId);
				return;
			}
			int round = currentCount + 1; //下一波回合数
			long consume = DateUtil.getCurrentSecond() - dungeon.getCreateDate();
			
			for(Entry<Integer,Integer> entry : intime.entrySet()){
				int time = entry.getKey();
				int timeRound = entry.getValue();
				
				/** 怪物已经被清空,并且达到回合数,自动出怪 20120620新增规则*/
				if(dungeon.isEmpty4Monster() && round == timeRound){
					this.createMonster(dungeon, round);
					break;//如果满足条件,每次只需要处理一次
				}
				
				/** 时间满足条件,并且下一回合的数也达到条件则运行*/
				if(consume >= time && round == timeRound){
					this.createMonster(dungeon, round);
					break;//如果满足条件,每次只需要处理一次
				}
			}
		}
	}
	
	/**
	 * 创建回合怪物
	 * @param dungeon   副本对象
	 * @param round     需要创建的回合数
	 */
	private void createMonster(Dungeon dungeon,int round){
		if(dungeon == null){
			return;
		}
		
		long dungeonId = dungeon.getDungeonId(); //增量ID
		int dungeonBaseId = dungeon.getBaseId(); //基础ID
		int branching = dungeon.getBranching();  //分线
		
		List<MonsterDungeonConfig> dungeonMonsterConfigs = monsterService.getMonsters4Round(dungeonBaseId , round);
		if(dungeonMonsterConfigs == null){
			logger.error("副本基础ID[{}],副本类型[{}],第[{}]回合的怪物配置不存在.",new Object[]{dungeonBaseId,dungeon.getType(),round});
			return;
		}
		
		GameMap map = this.gameMapManager.getTemporaryMap(dungeonId, branching);
		if(map == null){
			logger.error("副本基础ID[{}],第[{}]回合,临时地图[{}]不存在,无法创建下一回合怪物",new Object[]{dungeonBaseId,round,dungeonId});
			return;
		}
		
		dungeon.setRoundCount(round);
		List<Long> monsterIds = new ArrayList<Long>();         //怪物初始化
		Collection<Long> playerIds =  dungeon.filterPlayers(); //过滤不在副本中的玩家
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
		Map<String,Object> msg = dungeon.addDungeonMonster(round, monsterIds);
		if(msg != null){
			this.dungeonPushHelper.pushDungeonStatistics(dungeon.filterPlayers(), msg);
		}
		worldPusherHelper.putMessage2Queue(allSpireIdCollection);
	}
	
	
	/**
	 * 检测副本完成
	 * @param dungeon
	 */
	private void checkComplete(Dungeon dungeon){
		this.doSuccess(dungeon);
	}

}
