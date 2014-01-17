package com.yayo.warriors.common.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.config.ServerConfig;
import com.yayo.common.socket.message.Response;
import com.yayo.common.thread.NamedThreadFactory;
import com.yayo.warriors.module.animal.facade.AnimalFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.model.UserDomain.SpireQueueType;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.map.MapCmd;
import com.yayo.warriors.type.ElementType;

/**
 * 消息主动推送类. 主要用于战斗, 战报, 战斗奖励, 属性推送等
 * 
 * @author Hyint
 */
@Component
public class WorldPusherHelper {
	
	/** 日志 */
	private static final Log log = LogFactory.getLog(WorldPusherHelper.class);	
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private VOFactory voFactory ;
	@Autowired
	private AnimalFacade animalFacade;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private MapFacade mapFacade;
	
	public static final String removeSpire = "removeSpire";
	public static final String addSpire = "addSpire";
	public static final String motionSpire = "motionSpire";
	
	private static final int keepAliveTime = 900;
	
	/** 队列数量*/
	private final int EUEUENUM = ServerConfig.getWorldQueueSize();
	
	@Autowired(required=false)
	@Qualifier(value = "WORLD_PUSH_MAX_THREAD_POOL_SIZE")
	private Integer maxPoolSize = Runtime.getRuntime().availableProcessors() * (EUEUENUM/2 + 1);
	
	/** 推送行线程池 */
	private static final ThreadFactory THREAD_FACTORY = new NamedThreadFactory(new ThreadGroup("世界推送线程"), "");
	private ExecutorService pusherExecuter = new ThreadPoolExecutor(1, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY);
	
	@SuppressWarnings("unchecked")
	private final BlockingQueue<ISpire> [] PUSH_QUEUER_ARRAY = new LinkedBlockingQueue[EUEUENUM];
	
	public void putMessage2Queue(ISpire spire) {
		try {
			if(spire != null && spire.getType() == ElementType.PLAYER ){
				int index = (int)(spire.getId()%EUEUENUM) ;
				BlockingQueue<ISpire> queue = PUSH_QUEUER_ARRAY[index];
				queue.add(spire);
			}
		} catch (Exception ex) {
			log.error("putMessage2Queue error: {}", ex);
			log.error("{}", ex);
		}
	}
	
	public void putMessage2Queue(Collection<ISpire> spires) {
		try {
			if(spires != null && spires.size() > 0){
				int index = (int)(System.currentTimeMillis()%EUEUENUM) ;
				BlockingQueue<ISpire> queue = PUSH_QUEUER_ARRAY[index];
				queue.addAll(spires);
			}
		} catch (Exception ex) {
			log.error("putMessage2Queue error: {}", ex);
			log.error("{}", ex);
		}
	}
	
	/**
	 * 登录初始化队列初始化
	 */
	@PostConstruct
	void initialize() {
		if(log.isDebugEnabled()){
			log.debug("Initialize World push Thread...");
		}
		String threadName = "异步世界推送线程";
		ThreadGroup group = new ThreadGroup(threadName);
		NamedThreadFactory factory = new NamedThreadFactory(group, threadName);
		for(int i = 0 ; i < PUSH_QUEUER_ARRAY.length ; i ++){
			BlockingQueue<ISpire> queue = PUSH_QUEUER_ARRAY[i];
			if(queue == null){
				queue = new LinkedBlockingQueue<ISpire>(); 
				PUSH_QUEUER_ARRAY[i] = queue ;
			}
			Thread thread = factory.newThread(new CustomerThread(queue));
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	/**
	 * 消费者线程
	 */
	class CustomerThread implements Runnable {
		
		BlockingQueue<ISpire> push_queuer ;
		
		public CustomerThread(BlockingQueue<ISpire> push_queuer ){
			this.push_queuer = push_queuer ;
		}
		
		
		public void run() {
			while (true) {
				try {
					ISpire spireTarget = this.push_queuer.take();
					if(spireTarget == null || spireTarget.getType() != ElementType.PLAYER) {
						continue;
					}
					
					UserDomain targetUserDomain = (UserDomain)spireTarget;
					GameMap gameMap = targetUserDomain.getGameMap();
					
					Set<ISpire> viewSpireSet = targetUserDomain.pollAllSpireQueue(SpireQueueType.VIEW);
					List<Map<String,Object>> viewList = null;
					if(viewSpireSet != null && viewSpireSet.size() > 0){
						viewList = new ArrayList<Map<String,Object>>(1);
						for(ISpire spire : viewSpireSet){
							//不发关于自己的信息，客户端要求
							if(spire.getType() == ElementType.PLAYER && spire.getId() == spireTarget.getId()){
								continue;
							}
							Map<String,Object> vM = animalFacade.getAnimal(spire, spire.getType()) ;
							if(vM != null){
								if(spire.getType() == ElementType.MONSTER){
//									MonsterAiDomain monsterAiDomain = (MonsterAiDomain)spire ;
//									if(gameMap == null || !gameMap.checkInViewScreen(targetUserDomain, monsterAiDomain)){
//										continue;
//									}
									viewList.add(vM);
//									System.err.println(String.format("玩家[%s]看到怪物[%d]", targetUserDomain.getPlayer().getName(), monsterAiDomain.getId()));
								} else if(spire.getType() == ElementType.NPC){
									Npc npc = npcFacade.getNpc((int)spire.getId());
									if(npc != null && gameMap != null && npc.isCanView(gameMap)){
										viewList.add(vM);
									}
								} else {
									viewList.add(vM);
								}
							}
						}
					}
					
					Set<ISpire> hideSpireSet = targetUserDomain.pollAllSpireQueue(SpireQueueType.HIDE);
					List<UnitId> hideList = null;
					if(hideSpireSet != null && hideSpireSet.size() > 0){
						hideList = new ArrayList<UnitId>();
						for(ISpire spire : hideSpireSet){
							//不发关于自己的信息，客户端要求
							if(spire.getType() == ElementType.PLAYER && spire.getId() == spireTarget.getId()){
								continue;
							}
							
							if(spire.getType() == ElementType.NPC){
								if(spire.getMapId() == targetUserDomain.getMapId()){
									Npc npc = npcFacade.getNpc((int)spire.getId());
									if(npc != null){
										hideList.add(npc.getUnitId());
									}
								}
								
							} else if(spire.getType() == ElementType.MONSTER ){
								if(spire.getMapId() == targetUserDomain.getMapId()){
									hideList.add(spire.getUnitId());
//									MonsterAiDomain monsterAiDomain = (MonsterAiDomain)spire ;
//									System.err.println(String.format("玩家[%s]隐藏怪物[%d]", targetUserDomain.getPlayer().getName(), monsterAiDomain.getId()));
								}
								
							} else {
								hideList.add(spire.getUnitId());
							}
						}
					}
					
					Set<ISpire> motionSpireSet = targetUserDomain.pollAllSpireQueue(SpireQueueType.MOTION);
					List<Map<String,Object>> motionList = null;
					if(motionSpireSet != null && motionSpireSet.size() > 0 ){
						if(mapFacade.isChangeScreen(targetUserDomain)){
							continue;
						}
						motionList = new ArrayList<Map<String,Object>>();
						for(ISpire spire : motionSpireSet){
							if(spire == null){
								log.error(String.format("精灵移动不存在") );
							}
							if(spire instanceof MonsterDomain){
								MonsterDomain monster = (MonsterDomain)spire ;
								if(!monster.getMonsterBattle().isDead() && monster.getMapId() == targetUserDomain.getMapId() ){
									Object[] path = monster.currentPointToArrays();
									if(path != null && path.length > 0){
//										if(monster.getId() == 11223){
//											System.err.println(String.format("{%s}收到{%s}的Path{%s}", targetUserDomain.getPlayer().getName(), monster.getId(), Arrays.toString(path)) );
//										}
										motionList.add(voFactory.getMonsterPathVo(monster, path));
									}
								}
							}
							if(spire instanceof UserDomain){
								//不发关于自己的信息，客户端要求
								if(spire.getType() == ElementType.PLAYER && spire.getId() == spireTarget.getId()){
									continue;
								}
								UserDomain userDomain = (UserDomain)spire ;
								PlayerMotion motion = userDomain.getMotion();
								Object[] path = motion.currentPointToArrays();
//								log.error(String.format("{%s}收到{%s}的Path{%s}", targetUserDomain.getPlayer().getName(), userDomain.getPlayer().getName(), Arrays.toString(path)) );
								if(path != null && path.length > 0){
									motionList.add(voFactory.getPlayerPathVo(userDomain, path));
									motion.removePath();
								}
							}
						}
					}
					
					Map<String,Object> result = new HashMap<String, Object>(3);
					if(hideList != null && hideList.size() > 0){
						result.put(removeSpire, hideList.toArray());
					}
					if(viewList != null && viewList.size() > 0){
						result.put(addSpire, viewList.toArray());
					}
					if(motionList != null && motionList.size() > 0 ){
						result.put(motionSpire, motionList.toArray());
					}
					if(!result.isEmpty()){
						pusherExecuter.execute(new PusherWorker(spireTarget,  Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_IPSIRE, result) ));
					}
				} catch (Exception ex) {
					log.error("世界推送消费线程报错:{} ", ex);
					log.error("{} ", ex);
				}
			}
		}
	}; 
	
	class PusherWorker implements Runnable{

		Response response = null ;
		ISpire spire = null ;
		
		public PusherWorker(ISpire spire,Response response){
			this.response = response ;
			this.spire = spire ;
		}
		
		
		public void run() {
			sessionManager.write(spire.getId(), response);
		}
		
	}
	public void pushSpireChange(Map<ISpire, Set<ISpire>[]> spireMap) {
		pushSpireChange(spireMap, false);
	}
	
	/**
	 * 推送用户的精灵变更
	 * @param spireMap
	 */
	public void pushSpireChange(Map<ISpire, Set<ISpire>[]> spireMap, boolean playerFresh) {
		for(Iterator<ISpire> iterator = spireMap.keySet().iterator(); iterator.hasNext(); ){
			UserDomain userDomain = (UserDomain)iterator.next();
			Set<ISpire>[] queues = spireMap.get(userDomain);
			
			for(Iterator<ISpire> viewIt = queues[0].iterator(); viewIt.hasNext(); ){
				ISpire spire = viewIt.next();
				if(spire.getType() != ElementType.PLAYER) {
					if(spire.getType() == ElementType.NPC){
						queues[1].remove(spire);
					}
					if(spire.getType() == ElementType.MONSTER){
						MonsterDomain monsterAiDomain = (MonsterDomain)spire;
						if(monsterAiDomain.getMonsterBattle().isDead() ){ 
							if( monsterAiDomain.needToRemoveCorpse() ){
								viewIt.remove();
							}
							queues[2].remove(spire);
							continue;
						}
					}
				} else if(!playerFresh){
					queues[1].remove(spire);
				}
			}
			//指定玩家可以看到的精灵 key:目标角色精灵  	[0]:可见, [1]:隐藏, [2]:移动
			userDomain.putCanViewSpire(queues[0]);
			
//			if(queues[1].size() > 0){
//				for(Iterator<ISpire> hideIt = queues[1].iterator(); hideIt.hasNext(); ){
//					ISpire spire = hideIt.next();
//					if(spire.getType() == ElementType.MONSTER){
//						MonsterAiDomain monsterAiDomain = (MonsterAiDomain)spire;
//						if(monsterAiDomain.getMonsterBattle().isDeath() ){ 
//							hideIt.remove();
//							queues[2].remove(spire);
//							continue;
//						}
//					}
//				}
//			}
			userDomain.putHideSpire(queues[1]);
			
			userDomain.putMotionSpire(queues[2]);
			
			putMessage2Queue(userDomain);
		}
	}
	
	/**
	 * 修改角色指定类型的精灵改变map
	 * @param targetISpire
	 * @param spireMap
	 * @param queueType
	 * @param spires
	 */
	public void put2SpireQueue(ISpire targetISpire, Map<ISpire, Set<ISpire>[] > spireMap, SpireQueueType spireQueueType, Collection<ISpire> spires){
		if(targetISpire != null && targetISpire instanceof UserDomain ){
			Set<ISpire>[] spireQueue = getSpireQueue(targetISpire, spireMap);
			spireQueue[spireQueueType.ordinal()].addAll(spires);
		}
	}
	public void put2SpireQueue(ISpire targetISpire, Map<ISpire, Set<ISpire>[] > spireMap, Collection<ISpire> spires, SpireQueueType... spireQueueTypes){
		if(targetISpire != null && targetISpire instanceof UserDomain ){
			Set<ISpire>[] spireQueue = getSpireQueue(targetISpire, spireMap);
			for(SpireQueueType spireQueueType : spireQueueTypes){
				spireQueue[spireQueueType.ordinal()].addAll(spires);
			}
		}
	}
	
	/**
	 * 修改角色指定类型的精灵改变map
	 * @param spires
	 * @param spireMap
	 * @param queueType
	 * @param targetISpire
	 */
	public void put2SpireQueue(Collection<ISpire> spires, Map<ISpire, Set<ISpire>[] > spireMap, ISpire targetSpire, SpireQueueType... spireQueueTypes){
		if(spires != null) {
			for(ISpire spire : spires){
				if(spire instanceof UserDomain ){
					Set<ISpire>[] spireQueue = getSpireQueue(spire, spireMap);
					for(SpireQueueType queueType :  spireQueueTypes){
						spireQueue[queueType.ordinal()].add(targetSpire);
					}
				}
			}
		}
	}
	
	/**
	 * 修改角色指定类型的精灵改变map
	 * @param targetISpire
	 * @param spireMap
	 * @param queueType
	 * @param spire
	 */
	public void put2SpireQueue(ISpire targetISpire, Map<ISpire, Set<ISpire>[] > spireMap, SpireQueueType queueType, ISpire spire){
		if(targetISpire != null && targetISpire instanceof UserDomain ){
			Set<ISpire>[] spireQueue = getSpireQueue(targetISpire, spireMap);
			spireQueue[queueType.ordinal()].add(spire);
		}
	}
	
	/**
	 * 获取角色指定类型的精灵改变map
	 * @param targetISpire
	 * @param spireMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<ISpire>[] getSpireQueue(ISpire targetISpire, Map<ISpire, Set<ISpire>[] > spireMap){
		Set<ISpire>[] queues = spireMap.get(targetISpire);
		if(queues == null){
			queues = new HashSet[3];
			queues[0] = new HashSet<ISpire>();
			queues[1] = new HashSet<ISpire>();
			queues[2] = new HashSet<ISpire>();
			spireMap.put(targetISpire, queues);
		}
		return queues;
	}
}
