package com.yayo.warriors.module.buffer.facade.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.EventBus;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.thread.NamedThreadFactory;
import com.yayo.common.utility.CollectionUtils;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.common.helper.FightPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.event.DeadEvent;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.facade.BufferFacade;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.buffer.vo.DOTInfoVO;
import com.yayo.warriors.module.fight.facade.FightFutureFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.model.MonsterBuffer;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.team.manager.TeamManager;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.ElementType;

/**
 * BUFF接口实现类
 * 
 * @author Hyint
 */
@Component
public class BufferFacadeImpl implements BufferFacade, LogoutListener {
	@Autowired
	private EventBus eventBus;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private PetManager petManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TeamManager teamManager;
	@Autowired
	private MonsterFacade monsterFacade;
	@Autowired
	private FightFutureFacade fightFutureFacade;
	
	//日志对象
	private static final Logger LOGGER = LoggerFactory.getLogger(BufferFacadeImpl.class);
	/** BUFF定时计算容器 */
	private static final ConcurrentHashSet<UnitId> BUFFER_CONTEXT = new ConcurrentHashSet<UnitId>();
 
	
	//------------------------------------------------------------------------------------------------------------
	
	/** 最小的池尺寸*/
	private static final int MIN_POOL_SIZE = 2;
	/** 最大的池尺寸*/
	private static final int MAX_POOL_SIZE = 10;
	/** 默认的线程组大小  */
	private static final String GROUP_NAME = "BUFF 线程组";
	/** 线程组 */
	private static final ThreadGroup THREAD_GROUP = new ThreadGroup(GROUP_NAME);
	/** 线程组工厂 */
	private static final NamedThreadFactory NAMED_THREAD_FACTORY = new NamedThreadFactory(THREAD_GROUP, GROUP_NAME);
	/** 线程池队列信息*/
	private final BlockingQueue<List<UnitId>> TASK_QUEUE = new LinkedBlockingQueue<List<UnitId>>(Integer.MAX_VALUE);
	/** BUFF线程执行器 */
	private final ExecutorService EXECUTOR = new ThreadPoolExecutor(MIN_POOL_SIZE, MAX_POOL_SIZE, 900, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), NAMED_THREAD_FACTORY);
	
	@PostConstruct
	protected void init() {
		String threadName = "BUFF Daemon 线程类";
		ThreadGroup group = new ThreadGroup(threadName);
		NamedThreadFactory factory = new NamedThreadFactory(group, threadName);
		Thread thread = factory.newThread(COSTOMER_TASK);
		thread.setDaemon(true);
		thread.start();
	}
	
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		this.removeBufferFromScheduler(UnitId.valueOf(playerId, ElementType.PLAYER));
	}


	/**
	 * 移除上下文的战斗单位ID.
	 * 
	 * @param  unitId		战斗单位ID
	 */
	
	public void removeBufferFromScheduler(UnitId unitId) {
		if(unitId != null) {
			BUFFER_CONTEXT.remove(unitId);
		}
	}
	
	/**
	 * 把战斗单位设置到缓存中
	 * 
	 * @param  unitId		战斗单位ID
	 */
	private void addUnitIdToContext(UnitId unitId) {
		if(unitId != null) {
			BUFFER_CONTEXT.add(unitId);
		}
	}
	
	/**
	 * 把角色的加入到检索队列中
	 * 
	 * @param playerId					角色ID
	 * @param resetAll					是否重置
	 */
	
	public void addUserBufferQueue(long playerId, boolean resetAll) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		UnitId unitId = userDomain.getUnitId();
		if(!userManager.isOnline(unitId.getId())) {
			removeBufferFromScheduler(unitId);
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色:[{}] 尚未登录, 将BUFF设置到队列中", playerId);
			}
			return;
		}
		
		//如果这个队列中已经有该玩家, 则直接返回了
		if(BUFFER_CONTEXT.contains(unitId)) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("队列中已经有该玩家:[{}] 的DEBUF计算, 不需要重复增加了", playerId);
			}
			return;
		}
		
		
		UserBuffer userBuffer = userDomain.getUserBuffer();
		if(userBuffer.isAllBufferEmpty()) {
			return;
		}
		
//		if(BUFFER_CONTEXT.contains(unitId)) {
//			LOGGER.debug("队列中已经有该玩家:[{}] 的DEBUF计算, 不需要重复增加了", playerId);
//			return;
//		}
			
		userBuffer.resetBufferInfos(resetAll);
		if(!userBuffer.isAllBufferEmpty()) {
			this.addUnitIdToContext(unitId);
		}
	}
	
	/**
	 * 把怪物的BUFF的加入到检索队列中
	 * 
	 * @param monsterId					怪物ID
	 */
	
	public void addMonsterBufferQueue(long monsterId) {
		MonsterDomain monsterDomain = monsterFacade.getMonsterDomain(monsterId);
		if(monsterDomain == null) {
			return;
		}

		UnitId unitId = monsterDomain.getUnitId();
		if(BUFFER_CONTEXT.contains(unitId)) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("队列中已经有该怪物:[{}] 的DEBUF计算, 不需要重复增加了", unitId);
			}
			return;
		}
		
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		if(monsterBattle.isDead()) {
			return;
		}
		
		MonsterBuffer monsterBuffer = monsterDomain.getMonsterBuffer(true);
		if(monsterBuffer.isAllBufferEmpty()) {
			return;
		}
		
//		if(BUFFER_CONTEXT.contains(unitId)) {
//			LOGGER.debug("队列中已经有该怪物:[{}] 的DEBUF计算, 不需要重复增加了", unitId);
//			return;
//		}
		
		monsterBuffer = monsterDomain.getMonsterBuffer(true);
		if(!monsterBuffer.isAllBufferEmpty()) {
			this.addUnitIdToContext(unitId);
		}
	}
	
	/**
	 * 查询角色的Buffer对象
	 * 
	 * @param  playerId					角色ID
	 * @return {@link UserBuffer}		用户BUFF对象
	 */
	
	public UserBuffer getUserBuffer(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain != null) {
			return userDomain.getUserBuffer();
		}
		return null;
	}

	
	public void onLoginEvent(UserDomain userDomain, int branching) {
		if(userDomain != null) {
			addUserBufferQueue(userDomain.getPlayerId(), true);
		}
	}


	/** 
	 * 处理BUFF定时任务
	 */
	
	public void processBufferScheduling() {
		if(!BUFFER_CONTEXT.isEmpty()) {
			TASK_QUEUE.add(new ArrayList<UnitId>(BUFFER_CONTEXT));
		}
	}
	
	/**
	 * 处理怪物DEBUFF技能计时.
	 * 
	 * @param playerId		角色ID列表
	 */
	private boolean processMonsterDebuffer(final UnitId unitId) {
		long monsterId = unitId.getId();
		MonsterDomain monsterDomain = monsterFacade.getMonsterDomain(monsterId);
		if(monsterDomain == null) {
			return true;
		}
		
		MonsterBattle battle = monsterDomain.getMonsterBattle();
		MonsterBuffer monsterBuffer = monsterDomain.getMonsterBuffer(true);
		if(monsterBuffer.isDeBufferEmpty()) {
			return monsterBuffer.isAllBufferEmpty();
		}
		
		boolean flushable = false;
		List<DOTInfoVO> dotInfos = new ArrayList<DOTInfoVO>();		//持续掉血信息
		ChainLock lock = LockUtils.getLock(battle, monsterBuffer);
		try {
			lock.lock();
			Map<Integer, Buffer> debuffers = monsterBuffer.getDebufferInfoMap();
			if(debuffers == null || debuffers.isEmpty()) {
				return monsterBuffer.isAllBufferEmpty();
			}
			
			Set<Entry<Integer, Buffer>> entrySet = debuffers.entrySet();
			for (Iterator<Entry<Integer, Buffer>> it = entrySet.iterator(); it.hasNext();) {
				Entry<Integer, Buffer> entry = it.next();
				Integer effectId = entry.getKey();
				Buffer buffer = entry.getValue();
				if(effectId == null || buffer == null) {
					it.remove();
					flushable = true;
					continue;
				}
				
				//BUFF还没生效.
				if(!buffer.isStart()) {
					continue;
				}
				
				if(buffer.isTimeOut()) {
					it.remove();
					flushable = true;
					continue;
				}
				
				if(buffer.getCycle() <= 0) {												//计算周期小于0, 表示不用跳秒
					continue;
				} 
				
				int period = buffer.getCycle();												//BUFF的计算周期
				int damage = buffer.getDamage();											//DEBUFF的伤害值
				long castId = buffer.getCastId();											//释放的单位ID
				long endTime = buffer.getEndTime();											//结束时间
				int unitType = buffer.getUnitType();										//单位类型
				long currentTime = System.currentTimeMillis();								//当前时间
				long lastReduceTime = buffer.getLastCalcTime();								//上次计算的时间
				long reduceTime = endTime <= currentTime ? endTime : lastReduceTime;		//扣减的时间
				int reduceCount = (int)((currentTime - reduceTime) / period);				//计算需要扣减的次数
				if(reduceCount <= 0 && endTime > currentTime) {								//未超时, 直接跳过
					continue;
				}
				
				//总共扣减的时间. 单位:秒
				int totalTimes = reduceCount * period;
				int totalDamage = reduceCount * damage;
				if(totalDamage > 0) {
					dotInfos.add(DOTInfoVO.valueOf(effectId, totalDamage, castId, unitType));
				}
				
				//更新上次扣除的时间
				buffer.setLastCalcTime(lastReduceTime + totalTimes);
				if(DateUtil.toSecond(endTime) <= DateUtil.toSecond(currentTime)) {
					it.remove();
					flushable = true;
				}
			}
			
			if(flushable) {
				battle.updateFlushable(Flushable.FLUSHABLE_NORMAL);
			}
		} finally {
			lock.unlock();
		}
		
		processMonsterBattleDotInfos(monsterDomain, dotInfos);
		return monsterBuffer.isAllBufferEmpty();
	}
	
	/**
	 * 处理BUFF技能计时
	 * 
	 * @param playerId		角色ID列表
	 */
	private boolean processPlayerDebuffer(final UnitId unitId) {
		long playerId = unitId.getId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return true;
		}
		
		UserBuffer userBuffer = userDomain.getUserBuffer();
		Map<Integer, Buffer> bufferInfos = userBuffer.getDeBufferInfos();
		if(bufferInfos == null || bufferInfos.isEmpty()) {
			return userBuffer.isAllBufferEmpty();
		}
		
		boolean flushable = false;
		List<DOTInfoVO> dotInfos = new ArrayList<DOTInfoVO>(1);		//持续掉血信息
		ChainLock lock = LockUtils.getLock(userBuffer);
		try {
			lock.lock();
			bufferInfos = userBuffer.getDeBufferInfos();
			if(bufferInfos == null || bufferInfos.isEmpty()) {
				return userBuffer.isAllBufferEmpty();
			}
			
			Set<Entry<Integer, Buffer>> entrySet = bufferInfos.entrySet();
			for (Iterator<Entry<Integer, Buffer>> it = entrySet.iterator(); it.hasNext();) {
				Entry<Integer, Buffer> entry = it.next();
				Integer effectId = entry.getKey();
				Buffer buffer = entry.getValue();
				if(effectId == null || buffer == null) {
					it.remove();
					flushable = true;
					continue;
				}

				//BUFF还没生效.
				if(!buffer.isStart()) {
					continue;
				}
				
				if(buffer.isTimeOut()) {
					it.remove();
					flushable = true;
					continue;
				}
				
				if(buffer.getCycle() <= 0) {												//计算周期小于0, 表示不用跳秒
					continue;
				} 

				int period = buffer.getCycle();												//BUFF的计算周期
				int damage = buffer.getDamage();											//DEBUFF的伤害值
				long endTime = buffer.getEndTime();											//结束时间
				long currentTime = System.currentTimeMillis();								//当前时间
				long lastReduceTime = buffer.getLastCalcTime();								//上次计算的时间
				long reduceTime = endTime <= currentTime ? endTime : lastReduceTime;		//扣减的时间
				int reduceCount = (int)((currentTime - reduceTime) / period);				//计算需要扣减的次数
				if(reduceCount <= 0 && endTime > currentTime) {								//未超时, 直接跳过
					continue;
				}
				
				//总共扣减的时间. 单位:秒
				long castId = buffer.getCastId();
				int unitType = buffer.getUnitType();
				int totalTimes = reduceCount * period;
				int totalDamage = reduceCount * damage;
				if(totalDamage > 0) {
					dotInfos.add(DOTInfoVO.valueOf(effectId, totalDamage, castId, unitType));
				}
				
				//更新上次扣除的时间
				buffer.setLastCalcTime(lastReduceTime + totalTimes);
				if(DateUtil.toSecond(endTime) <= DateUtil.toSecond(currentTime)) {
					it.remove();
					flushable = true;
				}
			}
		} finally {
			lock.unlock();
		}
		
		if(flushable) {
			userDomain.updateFlushable(true, Flushable.FLUSHABLE_NORMAL);
		}
		processPlayerDotInfos(userDomain, dotInfos);
		return userBuffer.isAllBufferEmpty();
	}
	
	/**
	 * 更新怪物的HP属性
	 * 
	 * @param battle
	 * @param unitId
	 * @param receives
	 * @param dotInfos
	 */
	private List<DOTInfoVO> updateMonsterAttributes(MonsterDomain monsterDomain, Collection<Long> receives, List<DOTInfoVO> dotInfos) {
		List<DOTInfoVO> dotInfoList = new ArrayList<DOTInfoVO>(1);
		if(monsterDomain == null || dotInfos == null || dotInfos.isEmpty()) {
			return dotInfoList;
		}
		
		long castId = -1L;
		int unitType = -1;
		boolean isMonsterDead = false;
		MonsterBattle battle = monsterDomain.getMonsterBattle();
		Map<UnitId, Integer> hurtMaps = new HashMap<UnitId, Integer>();
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			for (DOTInfoVO dotInfoVO : dotInfos) {
				dotInfoList.add(dotInfoVO);
				battle.alterHp(-dotInfoVO.getDamage());
				isMonsterDead = battle.isDead();
				castId = dotInfoVO.getCastId();
				unitType = dotInfoVO.getUnitType();
				UnitId unitId = UnitId.valueOf(castId, unitType);
				Integer cachedHurtHP = hurtMaps.get(unitId);
				cachedHurtHP = cachedHurtHP == null ? 0 : cachedHurtHP;
				hurtMaps.put(unitId, cachedHurtHP + dotInfoVO.getDamage());
				if(battle.isDead()) {
					isMonsterDead = true;
					dotInfoVO.setState(true);
					castId = dotInfoVO.getCastId();
					unitType = dotInfoVO.getUnitType();
					break;
				}
			}
		} finally {
			lock.unlock();
		}
		
		long monsterId = battle.getMonsterId();
		UserPushHelper.pushAttribute2AreaMember(monsterId, receives, Arrays.asList(monsterDomain.getUnitId()), AttributeKeys.HP);
		processMonsterDead(monsterDomain, castId, unitType, isMonsterDead);
		processBufferDamageMonsterHp(monsterDomain, hurtMaps);
		return dotInfoList;
	}
	
	/**
	 * 处理怪物BUFF对怪物造成的伤害量
	 * 
	 * @param  monsterDomain		怪物域模型
	 * @param  hurtInfos			伤害值集合
	 */
	private void processBufferDamageMonsterHp(MonsterDomain monsterDomain, Map<UnitId, Integer> hurtInfos) {
		if(hurtInfos != null && !hurtInfos.isEmpty() && monsterDomain != null) {
			for (Entry<UnitId, Integer> entry : hurtInfos.entrySet()) {
				UnitId unitId = entry.getKey();
				Integer hurtHp = entry.getValue();
				fightFutureFacade.processMonsterFightHurt(monsterDomain, unitId, hurtHp);
			}
		}
	}
	/**
	 * 处理怪物死亡
	 * @param monsterId
	 * @param castId
	 * @param unitType
	 * @param monsterDead
	 */
	private void processMonsterDead(MonsterDomain monsterDomain, long castId, int unitType, boolean monsterDead) {
		if(!monsterDead || castId <= 0 || unitType < 0) {
			return;
		}
		
		long playerId = 0L;
		if(unitType == ElementType.PLAYER.ordinal()) {
			playerId = castId;
		} else if(unitType == ElementType.PET.ordinal()) {
			PetDomain petDomain = petManager.getPetDomain(castId);
			if(petDomain != null) {
				playerId = petDomain.getPlayerId();
			}
		} else {
			return;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		Set<Long> memberIds = new HashSet<Long>();
		Team playerTeam = teamManager.getPlayerTeam(playerId);
		if(playerTeam == null) {
			memberIds.add(playerId);
		} else {
			memberIds.addAll(playerTeam.getMembers());
		}
		
		try {
			fightFutureFacade.processMonsterDead(monsterDomain, castId, unitType, memberIds);
		} catch (Exception e) {
			LOGGER.error("DEBUFF照成怪物死亡 {}", e);
		}
	}
	/**
	 * 更新角色的HP属性
	 * 
	 * @param userDomain
	 * @param unitId
	 * @param receives
	 * @param dotInfos
	 */
	private List<DOTInfoVO> updatePlayerAttributes(UserDomain userDomain, Collection<Long> receives, List<DOTInfoVO> dotInfos) {
		List<DOTInfoVO> dotInfoList = new ArrayList<DOTInfoVO>(dotInfos.size());
		if(dotInfos.isEmpty() || receives.isEmpty()) {
			return dotInfoList;
		}
		
		UnitId attackerUnitId = null;
		boolean isPlayerDead = false;
		PlayerBattle battle = userDomain.getBattle();
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			for (DOTInfoVO dotInfoVO : dotInfos) {
				dotInfoList.add(dotInfoVO);
				int damage = dotInfoVO.getDamage();
				battle.setHp(Math.max(0, battle.getHp() - damage));
				if(isPlayerDead = battle.isDead()) {
					dotInfoVO.setState(true);
					long castId = dotInfoVO.getCastId();
					int unitType = dotInfoVO.getUnitType();
					attackerUnitId = UnitId.valueOf(castId, unitType);
					break;
				}
			}
		} finally {
			lock.unlock();
		}
		
		if(isPlayerDead) {
			eventBus.post(DeadEvent.valueOf(userDomain, attackerUnitId));
		}
		
		UnitId unitId = userDomain.getUnitId();
		UserPushHelper.pushAttribute2AreaMember(unitId.getId(), receives, Arrays.asList(unitId), AttributeKeys.HP);
		return dotInfoList;
	}
	
	//一条线程执行200个人或者怪物的BUFF跳动, 那么 20条线程并发可以达到4000个怪物或者人跳动
	final int TEMP = 200;	
	/**
	 * 消费者线程
	 */
	private Runnable COSTOMER_TASK = new Runnable() {
		
		public void run() {
			while (true) {
				try {
					List<UnitId> unitIds = TASK_QUEUE.take();
					if(unitIds == null || unitIds.isEmpty()) {
						continue;
					}
					
					int unitSize = unitIds.size();
					int schedulerCount = (int) Math.ceil(unitSize / (double) TEMP);
					for (int index = 0; index < schedulerCount; index++) {
						List<UnitId> unitIdList = CollectionUtils.subListCopy(unitIds, index * TEMP, TEMP);
						if(unitIdList != null && !unitIdList.isEmpty()) {
							EXECUTOR.submit(createWorkerThread(unitIdList));
						}
					}
				} catch (Exception ex) {
					LOGGER.error("Error: " + ex.getMessage());
				}
			}
		}
	};
	
	/**
	 * 获得线程工作者线程
	 * 
	 * @param  unitIdList			战斗单位ID列表
	 * @return {@link Runnable}
	 */
	private Runnable createWorkerThread(final List<UnitId> unitIdList) {
		return new Runnable() {
			
			public void run() {
				try {
					for(UnitId unitId : unitIdList) {
						if(!BUFFER_CONTEXT.contains(unitId)) {
							continue;
						}
						
						boolean removeFromCache = false;
						if(unitId.getType() == ElementType.PLAYER) {
							removeFromCache = processPlayerDebuffer(unitId);
						} else if(unitId.getType() == ElementType.MONSTER) {
							removeFromCache = processMonsterDebuffer(unitId);
						}
						
						if(removeFromCache) {
							removeBufferFromScheduler(unitId);
						}
					}
				} catch (Exception e) {
					LOGGER.error("工作者线程执行处理单位ID列表: {} 异常:{}", unitIdList, e);
				}
			}
		};
	}

	/**
	 * 处理角色DOT信息
	 * 
	 * @param  player			角色对象
	 * @param  battle			角色战斗对象
	 * @param  dotInfos			DOT掉血列表
	 */
	private void processPlayerDotInfos(UserDomain userDomain, List<DOTInfoVO> dotInfos) {
		if(dotInfos != null && !dotInfos.isEmpty()) {
			long playerId = userDomain.getPlayerId();
			Collection<Long> receives = mapFacade.getScreenViews(playerId);
			List<DOTInfoVO> dotInfoVOS = this.updatePlayerAttributes(userDomain, receives, dotInfos);
			FightPushHelper.pushDOTDamage2Client(userDomain.getUnitId(), receives, dotInfoVOS);
		}
	}

	/**
	 * 处理角色DOT信息
	 * 
	 * @param  battle			角色战斗对象
	 * @param  dotInfos			DOT掉血列表
	 */
	private void processMonsterBattleDotInfos(MonsterDomain monsterDomain, List<DOTInfoVO> dotInfos) {
		if(monsterDomain == null || dotInfos == null || dotInfos.isEmpty()) {
			return;
		}
		
		GameScreen currentScreen = monsterDomain.getCurrentScreen();
		if(currentScreen == null) {
			return;
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return;
		}
		
		Collection<Long> receives = gameMap.getCanViewsSpireIdCollection(monsterDomain, ElementType.PLAYER);
		List<DOTInfoVO> dotInfoList = this.updateMonsterAttributes(monsterDomain, receives, dotInfos);
		FightPushHelper.pushDOTDamage2Client(monsterDomain.getUnitId(), receives, dotInfoList);
	}
	
}
