package com.yayo.warriors.module.dungeon.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.adapter.MonsterService;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.common.helper.DungeonPushHelper;
import com.yayo.warriors.common.helper.WorldPusherHelper;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.types.DungeonState;
import com.yayo.warriors.module.dungeon.types.DungeonType;
import com.yayo.warriors.module.duntask.facade.DungeonTaskFacade;
import com.yayo.warriors.module.duntask.manager.DunTaskManager;
import com.yayo.warriors.module.duntask.types.TaskTypes;
import com.yayo.warriors.module.logger.log.DungeonLogger;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 副本玩法基础类
 * @author liuyuhua
 */
public abstract class BaseDungeonRule {
	
	@Autowired
	protected WorldPusherHelper worldPusherHelper ;
	@Autowired
	protected MonsterService monsterService; 
	@Autowired
	protected GameMapManager gameMapManager ;
	@Autowired
	protected MonsterManager monsterManager;
	@Autowired
	protected DungeonFacade dungeonFacade;
	@Autowired
	protected DungeonManager dungeonManager;
	@Autowired
	protected DbService cachedService;
	@Autowired
	protected DungeonPushHelper dungeonHelper;
	@Autowired
	protected DunTaskManager dunTaskManager;
	@Autowired
	protected DungeonTaskFacade dungeonTaskFacade;
	@Autowired
	protected DbService dbService;
	@Autowired
	protected DungeonPushHelper dungeonPushHelper;
	@Autowired
	protected UserManager userManager;
	
	/** 副本创建超过了这个时间才开始这个检测 (单位:秒)*/
	private final static int CHECKTIME = 1200; 
	
	protected final Logger logger = LoggerFactory.getLogger(getClass()); 
	

	/***
	 * 获取副本类型
	 * @return {@link DungeonType}副本类型
	 */
	public abstract int getDungeonType();
	
	/**
	 * 执行副本玩法
	 * @param dungeon   副本对象
	 */
	public abstract void action(Dungeon dungeon);

	/**
	 * 验证副本是否回收
	 * @param dungeon
	 * @return  true 成功回收,false没有回收
	 */
	public boolean validation(Dungeon dungeon){
		if(this.timeout(dungeon)){
			return true;
		}else if(this.outOfDungeon(dungeon)){
			return true;
		}else if(this.dungeonComplete(dungeon)){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 回收副本策略-超时回收
	 * <per>超过副本允许给予的存在时间</per>
	 * @param dungeon  副本对象
	 * @return true 处理完成 , false 不需要处理
	 */
	protected boolean timeout(Dungeon dungeon){
		if(dungeon == null){
			if(logger.isDebugEnabled()){
				logger.debug("检测副本过期时间，副本对象为空");
			}
			return false;
		}
		int branching = dungeon.getBranching();
		long dungeonId = dungeon.getDungeonId(); //副本增量ID
		int dungeonBaseId = dungeon.getBaseId(); //副本原型ID
		
		DungeonConfig dungeonConfig = dungeon.getDungeonConfig();
		if(dungeonConfig == null){
			if(logger.isDebugEnabled()){
				logger.debug("检测副本过期时间,副本原型ID[{}],副本配置为空.",dungeonBaseId);
			}
			return false;
		}
		
		long timeOut = dungeonConfig.getDungeonLiveDate(); //副本过期时间(单位:秒)
		long enterDate = dungeon.getCreateDate(); //进入副本时间(单位:秒)
		long currentTime = DateUtil.getCurrentSecond();//当前时间(单位:秒)
		long surplusTime = currentTime - enterDate; //剩余时间  
		
		if(surplusTime < timeOut){
			return false;
		}
		
		
		if(dungeon.getState() == DungeonState.SUCCESS){
			return false;
		}
		
		//开始回收流程
		Collection<Long> playerIds = dungeon.filterPlayers();
		if(playerIds == null || playerIds.isEmpty()){
			this.dungeonFacade.removeDungeon(dungeonId,branching);//回收副本
			if(logger.isDebugEnabled()){
				logger.debug("副本[{}],在时间回收方法中,执行回收操作.",dungeonId);
			}
			
		}else{
			List<Long> noticePlayer = new ArrayList<Long>(5);//需要通知主动退出的玩家
			for(long playerId : playerIds){
				PlayerDungeon playerDungeon = this.dungeonFacade.getPlayerDungeon(playerId);
				if(playerDungeon == null){
					continue;
				}
				if(playerDungeon.getDungeonId() == dungeonId){
					noticePlayer.add(playerId);
				}
			}
			
			if(noticePlayer.isEmpty()){
				this.dungeonFacade.removeDungeon(dungeonId, dungeon.getBranching());//回收副本
			}else{
				this.dungeonHelper.pushCoerceleave(noticePlayer);//通知在里面的玩家,强制退出副本
				if(logger.isDebugEnabled()){
					logger.debug("副本[{}],在'副本完成'回收方法中,执行回收操作.",dungeonId);
				}
			}
			
		}
		
		return true;
	}
	
	
	/**
	 *  回收副本策略 - 检测回收
	 *  <per>副本中,进入的玩家和退出副本的玩家数量一致时回收</per>
	 *  <per>该回收策略,必须要在副本创建的{@link BaseDungeonRule#CHECKTIME}后执行</per>
	 * @param dungeon  副本对象
	 * @return true 处理完成 , false 不需要处理
	 */
	protected boolean outOfDungeon(Dungeon dungeon){
		if(dungeon == null){
			if(logger.isDebugEnabled()){
				logger.debug("检测副本过期时间，副本对象为空");
			}
			return false;
		}
		
		long dungeonId = dungeon.getDungeonId();
		
		long createTime = dungeon.getCreateDate(); //副本创建时间(单位:秒)
		long currentTime = DateUtil.getCurrentSecond(); //当前时间(单位:秒)
		long surplusTime = currentTime - createTime; //剩余时间  
		
		if(surplusTime < CHECKTIME){
			return false;
		}
		
		Collection<Long> enterPlayers = dungeon.getEntrant();  //进入该副本的玩家ID集合
		Collection<Long> leavePlayers = dungeon.getLeaveIds();   //退出该副本的玩家ID集合
		
		int enterSize = enterPlayers.size();
		int leaveSize = leavePlayers.size();
		
		if(enterSize <= leaveSize){
			this.dungeonFacade.removeDungeon(dungeonId, dungeon.getBranching());
			
			if(logger.isDebugEnabled()){
				logger.debug("副本[{}],在'退出'回收方法中,执行回收操作.",dungeonId);
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * 回收副本策略 - 副本完成回收
	 * <per>副本在完成状态下执行</per>
	 * <per>该回收策略,必须要在副本完成的{@link BaseDungeonRule#CHECKTIME}后执行</per>
	 * @param dungeon   副本对象
	 * @return true 处理完成 , false 不需要处理
	 */
	protected boolean dungeonComplete(Dungeon dungeon){
		
		if(dungeon == null){
			if(logger.isDebugEnabled()){
				logger.debug("检测副本过期时间，副本对象为空");
			}
			return false;
		}
		
		if(dungeon.getState() != DungeonState.SUCCESS){
			return false;
		}
		
		long dungeonId = dungeon.getDungeonId();
		long successTime = dungeon.getCompleteDate(); //副本完成时间 (单位:秒)
		long currentTime = DateUtil.getCurrentSecond(); //当前时间(单位:秒)
		long surplusTime = currentTime - successTime; //剩余时间  
		
		if(surplusTime < CHECKTIME){
			return false;
		}
		
		Collection<Long> playerIds = dungeon.filterPlayers();
		if(playerIds == null || playerIds.isEmpty()){
			//回收副本
			this.dungeonManager.removeDungeon(dungeonId);
			if(logger.isDebugEnabled()){
				logger.debug("副本[{}],在'副本完成'回收方法中,执行回收操作.",dungeonId);
			}
			
		}else{
			List<Long> noticePlayer = new ArrayList<Long>();//需要通知主动退出的玩家
			for(long playerId : playerIds){
				PlayerDungeon playerDungeon = this.dungeonFacade.getPlayerDungeon(playerId);
				if(playerDungeon == null){
					continue;
				}
				
				if(playerDungeon.getDungeonId() == dungeonId){
					noticePlayer.add(playerId);
				}
			}
			
			if(noticePlayer.isEmpty()){
				this.dungeonFacade.removeDungeon(dungeonId, dungeon.getBranching());//回收副本
			}else{
				this.dungeonHelper.pushCoerceleave(noticePlayer);//通知在里面的玩家,强制退出副本
				if(logger.isDebugEnabled()){
					logger.debug("副本[{}],在'副本完成'回收方法中,执行回收操作.",dungeonId);
				}
			}
		}
		return true;
	}
	
    /**
     * 副本完成检测
     * @param dungeon  副本
     */
    protected void doSuccess(Dungeon dungeon){
		/** 当怪物全部被杀完,即副本已经完成*/
		if(dungeon.isEmpty4Monster() && (dungeon.getType() != DungeonType.HIGH_RICH)){
			ChainLock lock = LockUtils.getLock(dungeon);
			try {
				lock.lock();
				dungeon.setState(DungeonState.SUCCESS);
				dungeon.setCompleteDate(DateUtil.getCurrentSecond());
			} finally{
				lock.unlock();
			}
			
			if(dungeon.getState() == DungeonState.SUCCESS){
				Collection<Long> playerIds = dungeon.filterPlayers();
				DungeonConfig config = dungeon.getDungeonConfig();
				if(config != null && config.isStoryDungeon()){
					for(long playerId : playerIds){
						PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
						if(playerDungeon != null){
							playerDungeon.complete4Story(dungeon.getBaseId());
							dbService.submitUpdate2Queue(playerDungeon);
						}
					}
				}
				
				this.dungeonHelper.pushDungeonComplete(playerIds,dungeon.getBaseId());
				this.dungeonCompletedEvent(playerIds);
				this.log4Dungeon(playerIds, dungeon);
				if(logger.isDebugEnabled()){
					logger.debug("副本[{}],完成",dungeon.getDungeonId());
				}
			}
		}
    }
    
    /**
     * 副本日志
     * @param playerIds
     */
    private void log4Dungeon(Collection<Long> playerIds,Dungeon dungeon){
    	if(playerIds == null || playerIds.isEmpty() || dungeon == null){
    		return;
    	}
    	
    	for(long playerId : playerIds){
    		UserDomain userDomain = userManager.getUserDomain(playerId);
    		if(userDomain == null){
    			continue;
    		}
    		
    		Player player = userDomain.getPlayer();
    		PlayerBattle battle = userDomain.getBattle();
    		DungeonLogger.dungeonLogger(player, battle, dungeon);
    	}
    }
    
    /**
     * 副本完成事件
     * @param playerIds 玩家集合
     */
    private void dungeonCompletedEvent(Collection<Long> playerIds){
    	if(playerIds == null || playerIds.isEmpty()){
    		return;
    	}
    	for(long playerId : playerIds){
    		this.dunTaskManager.updateProgress(playerId , 0,TaskTypes.DUNGEON_COMPLETE);
    	}
    }
    
    /**
     * 回合完成事件
     * @param playerIds 玩家集合
     * @param round     当前完成回合数
     */
    protected void roundCompletedEvent(Collection<Long> playerIds,int round){
    	if(playerIds == null || playerIds.isEmpty()){
    		return;
    	}
    	
    	for(long playerId : playerIds){
    		this.dunTaskManager.updateProgress(playerId, round,TaskTypes.ROUND_COMPLETE);
    	}
    }

	
	/**
	 * 接受回合开始前的任务
	 * @param playerIds         玩家的集合
	 * @param round             当前回合数
	 * @param dungeonBaseId     副本基础ID
	 */
	protected void acceptRoundTask(Collection<Long> playerIds,int round,int dungeonBaseId){
		DungeonConfig config = this.dungeonManager.getDungeonConfig(dungeonBaseId);
		if(config == null){
			return;
		}
		
		Map<Integer,Collection<Integer>> roundTask = config.getRoundTasks();
		if(roundTask == null || roundTask.isEmpty()){
			return;
		}
		
		Collection<Integer> taskIds = roundTask.get(round);
		if(taskIds == null || taskIds.isEmpty()){
			return;
		}
		
		for(long playerId : playerIds){
			this.dungeonTaskFacade.accept(playerId, taskIds);
		}
	}

}
