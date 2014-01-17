package com.yayo.warriors.module.monster.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.thread.NamedThreadFactory;
import com.yayo.warriors.common.helper.WorldPusherHelper;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.model.UserDomain.SpireQueueType;
import com.yayo.warriors.type.ElementType;

/**
 *	怪物动作执行器
 */
@Component
public class MonsterAction {
	@Autowired
	private WorldPusherHelper worldPusherHelper;
	@Autowired
	private MonsterManager monsterManager ;
	@Autowired
	private PetManager petManager;
	
	/** 日志 */
	private final Logger LOGGER = LoggerFactory.getLogger(MonsterAction.class);
	
	/** 怪物执行线程池 */
	private ExecutorService monsterPoolExecutor;
	private ThreadPoolExecutor dungeonMonsterPoolExecutor;
	/** 怪物是否加载完毕*/
	private boolean finish ;
	/** 调度线程休眠时间 */
	public static final int SLEEP_TIME = 200;
	/** 怪物线程超时时间 */
	@Autowired(required=false)
	@Qualifier(value = "MONSTER_RUN_TIME_OUT")
	public int RUN_TIME_OUT = 30;
	
	/** 普通怪物线程池大小 */
	@Autowired(required=false)
	@Qualifier(value = "MONSTER_THREAD_SIZE")
	private Integer MONSTER_THREAD_SIZE = Runtime.getRuntime().availableProcessors() * 5;
	
	/** 副本怪物线程池大小 */
	@Autowired(required=false)
	@Qualifier(value = "MONSTER_THREAD_SIZE")
	private Integer DUNGEON_MONSTER_THREAD_SIZE = Runtime.getRuntime().availableProcessors() * 5;
	
	/** 怪物线程池存活时间 */
	@Autowired(required=false)
	@Qualifier(value = "MONSTER_THREAD_KEEP_ALIVE_TIME")
	private Integer MONSTER_THREAD_KEEP_ALIVE_TIME = 180;
	
	/**
	 * 执行AI
	 */
	public void executeAi() {
		ThreadGroup threadGroup = new ThreadGroup("怪物AI线程组");
		NamedThreadFactory factory1 = new NamedThreadFactory(threadGroup, "野外怪AI线程组");
		NamedThreadFactory factory2 = new NamedThreadFactory(threadGroup, "副本怪AI线程组");
		
		monsterPoolExecutor = new ThreadPoolExecutor(1, MONSTER_THREAD_SIZE, MONSTER_THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), factory1);
		dungeonMonsterPoolExecutor = new ThreadPoolExecutor(1, DUNGEON_MONSTER_THREAD_SIZE, MONSTER_THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), factory2);
		
		//启动普通怪物AI线程
		Thread normalMonsterThread = factory1.newThread(new Runnable() {
			
			public void run() {
				final MonsterFutrue[] monsterFutrues = new MonsterFutrue[ MONSTER_THREAD_SIZE ];
				int i = 0;
				while(true){
					try {
						Collection<MonsterDomain> monsterDomainList =  monsterManager.getMonsterDomainMap().values();
						//运行地图怪物AI
						long start = System.currentTimeMillis();
						for (MonsterDomain monsterDomain : monsterDomainList) {
							GameScreen currentScreen = monsterDomain.getCurrentScreen();
							if(currentScreen == null){
								continue ;
							}
							int playerNums = currentScreen.getGameMap().getPlayerNums();
							if(playerNums <= 0){
								if(!monsterDomain.checkIsAtHome() || monsterDomain.getMonsterBattle().isDead() ){
									if(!monsterDomain.hasAttackTarget() || !monsterDomain.hasRouteTarget() || !monsterDomain.getPath().isEmpty() ){
										monsterDomain.clear();
									}
								}
								continue ;
							}
							if(currentScreen != null && playerNums > 0){
								if(monsterDomain.getRuning().compareAndSet(false, true)){
									Future<Boolean> future = monsterPoolExecutor.submit( new MonsterRun(monsterDomain) );
									if(future != null){
										MonsterFutrue monsterFutrue = monsterDomain.getMonsterFutrue();
										monsterFutrue.future = future;
										monsterFutrues[i++] = monsterFutrue;
										if(i > 0 && i % MONSTER_THREAD_SIZE == 0){
											i = 0;
											deadThreadCheck(monsterFutrues);
										}
									}
								}
							}
						}
						if(i > 0){
							deadThreadCheck(monsterFutrues);
						}
						long end = System.currentTimeMillis();
						Thread.sleep(Math.max( SLEEP_TIME - (end - start), SLEEP_TIME) );
	
					} catch (Exception e) {
						LOGGER.error("运行普通怪物AI时出错:{}", e);
						LOGGER.error("{}", e);
					}
				}
			}
		});
		normalMonsterThread.setDaemon(true);
		normalMonsterThread.start();
		
		//启动副本怪物AI线程
		Thread dungeonMonsterThread = factory2.newThread(new Runnable() {
			
			public void run() {
				final MonsterFutrue[] monsterFutrues = new MonsterFutrue[ DUNGEON_MONSTER_THREAD_SIZE ];
				int i = 0;
				while(true){
					try {
						Map<Long, MonsterDomain> monsterMap = monsterManager.getDungeonMonsterMap();
						//运行副本怪物AI
						Collection<MonsterDomain> values = monsterMap.values();
						long start = System.currentTimeMillis();
						for(MonsterDomain monsterModel : values){
							GameScreen currentScreen = monsterModel.getCurrentScreen();
							if(currentScreen == null){
								continue;
							}
							int playerNums = currentScreen.getGameMap().getPlayerNums();
							if(playerNums <= 0){
								if(!monsterModel.checkIsAtHome() || monsterModel.getMonsterBattle().isDead() ){
									if(!monsterModel.hasAttackTarget() || !monsterModel.hasRouteTarget() || !monsterModel.getPath().isEmpty() ){
										monsterModel.clear();
									}
								}
								continue ;
							}
							if(monsterModel.getRuning().compareAndSet(false, true)){
								Future<Boolean> future = dungeonMonsterPoolExecutor.submit( new DungeonMonsterRun(monsterModel) );
								if(future != null){
									MonsterFutrue monsterFutrue = monsterModel.getMonsterFutrue();
									monsterFutrue.future = future;
									monsterFutrues[i++] = monsterFutrue;
									if(i > 0 && i % DUNGEON_MONSTER_THREAD_SIZE == 0){
										i = 0;
										deadThreadCheck(monsterFutrues);
									}
								}
							}
						}
						if(i > 0){
							deadThreadCheck(monsterFutrues);
						}
						long end = System.currentTimeMillis();
						Thread.sleep(Math.max( SLEEP_TIME - (end - start), SLEEP_TIME) );
					} catch (Exception e) {
						LOGGER.error("运行副本怪物AI时出错:{}", e);
						LOGGER.error("{}", e);
					}
				}
			}
		});
		dungeonMonsterThread.setDaemon(true);
		dungeonMonsterThread.start();
		LOGGER.error("怪物线程启动完成..........");
		
	}

	/**
	 * 怪物AI执行线程
	 */
	class MonsterRun implements Callable<Boolean>{
		private MonsterDomain monsterAiDomain ;
		public MonsterRun(MonsterDomain monsterModel){
			this.monsterAiDomain = (MonsterDomain)monsterModel ;
		}
		
		
		public Boolean call() throws Exception {
			boolean result = false;
			try{
				action(monsterAiDomain);
				result = true;
			} catch(Exception e) {
				if(monsterAiDomain != null){
					LOGGER.error("地图[{}]中野外怪物[{}]AI运行异常(⊙o⊙ )", monsterAiDomain.getMapId(), monsterAiDomain.getMonsterConfig().getId() );
				}
				LOGGER.error("{}", e);
			} finally {
				monsterAiDomain.getRuning().set(false);
			}
			return result;
		}
	}
	
	/**
	 * 副本怪物AI执行线程
	 */
	class DungeonMonsterRun implements Callable<Boolean>{
		private MonsterDomain monsterAiDomain ;
		public DungeonMonsterRun(MonsterDomain monsterModel){
			this.monsterAiDomain = (MonsterDomain)monsterModel ;
		}
		
		
		public Boolean call() throws Exception {
			boolean result = false;
			try{
				action(monsterAiDomain);
				result = true;
			} catch(Exception e) {
				if(monsterAiDomain != null){
					LOGGER.error("地图[{}]中副本怪物[{}]AI运行异常(⊙o⊙ )", monsterAiDomain.getMapId(), monsterAiDomain.getMonsterConfig().getId() );
				}
				LOGGER.error("{}", e);
				
			} finally {
				monsterAiDomain.getRuning().set(false);
			}
			return result;
		}
	}
	
	/**
	 * 是否读取完成
	 */
	public void loadMonsterFinish() {
		if(!finish){
			finish = true ;
			executeAi();
		}
	}

	private void action(MonsterDomain monsterDomain) {
		GameMap gameMap = monsterDomain.getGameMap() ;
		if(gameMap == null || gameMap.isCleared() ){
			monsterManager.getDungeonMonsterMap().remove( monsterDomain.getId() );
			return;
		}
		
		Collection<GameScreen> gameScreenList = gameMap.calcViewScreen(monsterDomain);
		//怪物能看到的人物
		Collection<ISpire> spireCollection = GameMap.getSpires(gameScreenList, ElementType.PLAYER);
		//玩家都看不到了，直接不执行AI
		if(spireCollection.isEmpty() && monsterDomain.getPath().isEmpty() && !monsterDomain.hasAttackTarget() ){
			if( monsterDomain.needToClearData() ){
				monsterDomain.clear();
			}
			return ;
		}
		
		//怪物能看到的家将
		Collection<ISpire> petCollection = GameMap.getSpires(gameScreenList, ElementType.PET);
		List<ISpire> livePetList = new ArrayList<ISpire>();
		for(ISpire spire : petCollection){
			PetDomain petDomain = petManager.getPetDomain(spire.getId());
			if(petDomain == null){
				continue;
			}
			Pet pet = petDomain.getPet();
			if(pet == null){
				continue;
			}
			PetBattle petBattle = petDomain.getBattle();
			if(petBattle == null){
				continue;
			}
			if(!petBattle.isDeath() && pet.isFighting() ){
				livePetList.add(spire);
			}
		}
		spireCollection.addAll(livePetList);
		
		monsterDomain.setCanWatchPlayersAndPets(spireCollection);
		
		try{
			//执行怪物动作节点
			monsterDomain.runAi();
		} catch(Exception e) {
			LOGGER.error("{}", e);
			return ;
		}
		int oldX = monsterDomain.getX(), oldY = monsterDomain.getY();
		//指定玩家可以看到的精灵 key:目标角色精灵  	[0]:可见, [1]:隐藏, [2]:移动
		Map<ISpire, Set<ISpire>[] > spireMap = new HashMap<ISpire, Set<ISpire>[] >(3);
		//怪物在行走
		final boolean hasNewPath = monsterDomain.isHasNewPath();
		final boolean moving = monsterDomain.moving();
		if(moving){
			GameScreen gameScreenSwap = gameMap.getGameScreen( monsterDomain );
			if(monsterDomain.isChangeScreen(gameScreenSwap)){			//判断是否有换场景
				Collection<GameScreen> oldCanViewScreens = gameMap.calcViewScreen(oldX, oldY);
				monsterDomain.changeScreen(gameScreenSwap);
				Collection<GameScreen> newCanViewScreens = gameMap.calcViewScreenByScreen(monsterDomain);
				newCanViewScreens.removeAll(oldCanViewScreens);
				
				//隐藏看不到的怪
				oldCanViewScreens.removeAll( gameMap.calcViewScreenByScreen(monsterDomain) );
				Collection<ISpire> hideSpires = new HashSet<ISpire>( GameMap.getSpires(oldCanViewScreens, ElementType.PLAYER) );
				for(ISpire spire : hideSpires){
					worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.HIDE, monsterDomain);
				}
				//推送新看到的
				Collection<ISpire> viewSpires = new HashSet<ISpire>( GameMap.getSpires(newCanViewScreens, ElementType.PLAYER) );
				for(ISpire spire : viewSpires){
					if(spire instanceof UserDomain){
						UserDomain userDomain = (UserDomain)spire ;
						worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.VIEW, monsterDomain);
						worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.MOTION, monsterDomain);
					}
				}
			}
			
		}
		
		List<ISpire> removeSpires = new ArrayList<ISpire>();
		spireCollection = monsterDomain.getCanWatchSpires();
		for(Iterator<ISpire> iterator = spireCollection.iterator(); iterator.hasNext(); ){
			ISpire spire = iterator.next();
			if(spire.getGameMap() != gameMap ){
				removeSpires.add(spire);
				continue;
			}
			if(spire instanceof PetDomain){
				PetDomain petDomain = (PetDomain)spire;
				Pet pet = petDomain.getPet();
				PetBattle petBattle = petDomain.getBattle();
				if(pet == null || petBattle == null || petBattle.isDeath() || !pet.isFighting() ){
					removeSpires.add(spire);
					continue;
				}
				
			} else if(spire instanceof UserDomain){
				if(hasNewPath){
					worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.VIEW, monsterDomain);
					worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.MOTION, monsterDomain);
				}
			}
		}
		
		monsterDomain.removeFromMonsterView(removeSpires);
		spireCollection = null;
		
		//让该推送的玩家入列
		if(spireMap.size() > 0){
			worldPusherHelper.pushSpireChange(spireMap);
		}
		
	}

	/**
	 * 怪物future类
	 */
	public static class MonsterFutrue{
		MonsterDomain monsterAiDomain;
		Future<Boolean> future;
		
		public static MonsterFutrue valueOf(MonsterDomain monsterAiDomain){
			MonsterFutrue monsterFutrue = new MonsterFutrue();
			monsterFutrue.monsterAiDomain = monsterAiDomain;
			return monsterFutrue;
		}

		public Future<Boolean> getFuture() {
			return future;
		}
		
	}
	
	/**
	 * 怪物死锁检查消费线程
	 * @author jonsai
	 *
	 */
	private void deadThreadCheck(MonsterFutrue... monsterFutrues) {
		try {
			for(int i=0, k = monsterFutrues.length; i < k; i++){
				MonsterFutrue monsterFutrue = monsterFutrues[i];
				if(monsterFutrue == null){
					continue;
				}
				AtomicBoolean runing = monsterFutrue.monsterAiDomain.getRuning();
				try {
					monsterFutrue.future.get(RUN_TIME_OUT, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {
					monsterFutrue.future.cancel(true);
				} catch (CancellationException e) {
				} catch (Exception e) {
					LOGGER.error("检查死锁怪物线程异常, {}", e);
					LOGGER.error("{}", e);
				} finally {
					runing.compareAndSet(true, false);
					monsterFutrues[i] = null;
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("检查死锁怪物线程异常, {}", e);
			LOGGER.error("{}", e);
		}
				
	}
}
