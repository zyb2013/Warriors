package com.yayo.warriors.module.monster.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.common.helper.MonsterHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.util.astar.DirectionUtil;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.action.MonsterAction.MonsterFutrue;
import com.yayo.warriors.module.monster.model.AStartAiNode;
import com.yayo.warriors.module.monster.model.AbstractAiNode;
import com.yayo.warriors.module.monster.model.AiActionMonitor;
import com.yayo.warriors.module.monster.model.AiLevel;
import com.yayo.warriors.module.monster.model.HurtInfo;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.module.monster.model.Monster;
import com.yayo.warriors.module.monster.model.MonsterAiLevel;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.model.MonsterBuffer;
import com.yayo.warriors.module.monster.model.SkillMonitor;
import com.yayo.warriors.module.monster.type.MoveUtil;
import com.yayo.warriors.module.monster.type.WalkType;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.helper.PetHelper;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.ElementType;

/**
 * 
 * @author haiming
 *怪物AI模型
 */
public class MonsterDomain implements ISpire {
	
	/** 怪物移除尸体时间 */
	private static final String REMOVE_CORPSE = "removecorpse";
	/** 怪物全局技能cd */
	private static final String GOLBAL_SKILLCD = "golbalskillcd" ;
	/** 怪物下次复活时间 */
	public static final String RESURRECTION_TIME = "ResurrectionTime";
	/** 怪物散步时间cd */
	public static final String WALK_CD_TIME = "WALK_CD_TIME";
	/** 怪物复活下次活动cd */
	public static final String REVIVE_ACTION_DELAY = "REVIVE_ACTION_DELAY";
	/** 怪物追击巡路cd */
	public static final String ASTAR_CD_TIME = "ASTAR_CD_TIME";
	
	
	/** 是否需要清除数据*/
	private boolean clear;
	/** 战斗单位ID */
	private UnitId unitId;
	/** 是否正在回出生地*/
	private boolean goHome;
	/** 怪物对象 */
	private Monster monster;
	/** 怪物所在地图*/
	private GameMap gameMap;
	/** 移动延迟*/
	private long actionDelay;
	/**	是否需要发送路径到前端*/
//	private boolean doBehiver;
	/** 目标路径*/
	private Point routeTarget;
	/** 攻击目标*/
	private ISpire attackTarget;
	/** 尸体是否被移除，这个字段有点多余*/
	private boolean removeCorpse;
	/** 怪物移动对象 */
//	private MonsterMotion motion;
	/** 准备要释放的技能*/
	private SkillMonitor cacheSkill ;
	/** 怪物基础信息 */
	private IMonsterConfig monsterConfig;
	/** 怪物战斗信息 */
	private MonsterBattle monsterBattle;
	/** 怪物AI 等级*/
	private MonsterAiLevel monsterAiLevel ;
	/** AI动作管理者*/
	private AiActionMonitor aiActionMonitor;
	/** 上次允许与目标最大距离 */
	private int redRange = 0;
	/** 怪物看到的精灵  */
	private Set<ISpire> spireCollection = Collections.synchronizedSet( new HashSet<ISpire>() );
	/** 怪物尸体移除时间(毫秒) */
	private static final long REMOVE_CORPSE_CD = 2000;	// 2 *　1000
	/** 怪物复活时间(毫秒)，临时  */
//	private long RESURRECTION_TIME_CD = 6;
	/** 是否停止运行此怪物 */
	private boolean stopRun = false;
	/** 是否有新的path */
	private volatile boolean hasNewPath = false;
	
	/** 怪物的BUFF信息 */
	private MonsterBuffer monsterBuffer = new MonsterBuffer();
	/** 是否在运行 */
	private AtomicBoolean runing = new AtomicBoolean(false);
	
	/** 场景 */
	private GameScreen gameScreen;

	//----------------原来怪物的montion对象属性-------------------------
	/**怪物ID*/
//	private Long monsterId;
	
	/**行走的X坐标*/
	private int x;
	
	/**行走的Y坐标*/
	private int y;
	
	/**所属的地图ID*/
	private int mapId;

	///Getter and Setter
	private LinkedList<Integer> path = new LinkedList<Integer>();
	//-----------------------------------------------------
	
	public MonsterDomain(Monster monster, GameMap map,
			IMonsterConfig monsterConfig, MonsterBattle monsterBattle) {
		this.gameMap = map;
		this.monster = monster;
		this.monsterConfig = monsterConfig;
		this.monsterBattle = monsterBattle;
		this.unitId = UnitId.valueOf(monster.getId(), ElementType.MONSTER);
		this.monsterAiLevel = MonsterAiLevel.getAiByLevel(monsterBattle.getMonsterFight());
		this.aiActionMonitor = new AiActionMonitor(monsterBattle.getMonsterFight().getSkills());
		
		this.mapId = map.getMapId();
		this.x = monsterConfig.getBornX();
		this.y = monsterConfig.getBornY();
	}
	
	
	public UnitId getUnitId() {
		return this.unitId;
	}
	
	public AtomicBoolean getRuning() {
		return runing;
	}
	
	/**
	 * 是否需要掉落运营活动相关的道具
	 * @return {@link Boolean} true 需要掉落 false 不掉落
	 */
	public boolean isDropActiveItem(){
		if(this.gameMap == null){
			return false;
		}
		
		if(gameMap.getScreenType() == ScreenType.BATTLE_FIELD.ordinal() 
		   || gameMap.getScreenType() == ScreenType.DUNGEON.ordinal()){
			return false;
		}
		
		return true;
	}

	/**
	 * 是否停止运行
	 * @return
	 */
	public boolean isStopRun() {
		return stopRun;
	}
	
	public void setStopRun(boolean stopRun) {
		this.stopRun = stopRun;
	}

	public void dispose() {
		this.aiActionMonitor.removeAllTired();
		this.monsterBattle.forgetAllEnemy();
		this.monsterBuffer.clearAllBuffer();
		synchronized (this.spireCollection) {
			this.spireCollection.clear();
		}
		this.leaveScreen();
	}
	
	/**
	 * 是否是新路径 
	 */
	public boolean isHasNewPath() {
		if(this.hasNewPath){
			synchronized (this.path) {
				if(this.hasNewPath){
					this.hasNewPath = false;
					return true;
				}
			}
		}
		return this.hasNewPath;
	}

	/**
	 * 获取怪物AI级别
	 * @return
	 */
	public int getAiLevel(){
		return this.getMonsterFightConfig().getAilevel();
	}
	
	/**
	 * 获取怪物模型
	 * @return {@link Monster}
	 */
	public Monster getMonster() {
		return monster;
	}
	
	/**
	 * 添加疲劳值
	 * @param clazz
	 * @param ss 毫秒
	 */
	public void addTired(Class<?> clazz , long ss){
		aiActionMonitor.addTired(clazz.getSimpleName(),ss);
	}
	
	/**
	 * 添加疲劳值
	 * @param name
	 * @param ss 毫秒
	 */
	public void addTired(String name , long ss){
		aiActionMonitor.addTired(name,ss);
	}
	
	/**
	 * 疲劳值是否已过期
	 * @param clazz
	 * @return
	 */
	public boolean isOverTired(Class<?> clazz){
		return aiActionMonitor.isOverTired(clazz.getSimpleName());
	}
	
	/**
	 * 疲劳值是否已过期
	 * @param clazz
	 * @return
	 */
	public boolean isOverTired(String name){
		return aiActionMonitor.isOverTired(name);
	}
	
	/**
	 * 获取受伤信息
	 * @return
	 */
	public Map<ISpire, HurtInfo> getHurtInfo(){
		return this.getMonsterBattle().getHurtInfo();
	}
	
	/**
	 * 获取怪物战斗信息类
	 * @return
	 */
	public MonsterBattle getMonsterBattle(){
		return MonsterHelper.calcMonsterBattle(this.monsterBattle, this);
	}
	
	/**
	 * 移除buffer效果
	 * @param key
	 */
	public void removeBuffer(int key, boolean isBuffer){
		if(this.getMonsterBuffer(true).removeBuffer(key, isBuffer)) {
			this.getMonsterBattle().updateFlushable(Flushable.FLUSHABLE_NORMAL);
		}
	}
	
	/**
	 * 添加Buffer效果
	 * 
	 * @param buffer		BUFFER对象
	 * @param isBuffer		true-BUFF, false-
	 */
	public void addBuffer(Buffer buffer, boolean isBuffer) {
		if(this.getMonsterBuffer(true).addBuffer(buffer, isBuffer)) {
			this.getMonsterBattle().updateFlushable(Flushable.FLUSHABLE_NORMAL);
		};
	}
	
	/**
	 * 清除所有buffer效果
	 */
	public void clearAllBuffer() {
		MonsterBuffer mofferEntity = getMonsterBuffer(true);
		mofferEntity.getBufferInfoMap().clear();
		mofferEntity.getDebufferInfoMap().clear();
		this.getMonsterBattle().updateFlushable(Flushable.FLUSHABLE_NORMAL);
	}
	
	
	public GameMap getGameMap(){
		return this.gameMap ;
	}
	
	public int getMapId(){
		return this.gameMap.getMapId() ;
	}
	
	public int getMonsterAiLevel(){
		return this.getMonsterFightConfig().getAilevel();
	}

	/**
	 * 移动的X坐标
	 */
	public int getX() {
		return this.x;
	}

	/**
	 * 移动的Y坐标
	 */
	public int getY() {
		return this.y;
	}

	public long getDungeonId() {
		return this.monster.getDungeonId();
	}

	public int getBranching() {
		return this.monster.getBranching();
	}

	public long getId() {
		return this.monster.getId();
	}

	
	public Object[] currentPointToArrays() {
//		Object[] currentPointToArrays = this.motion.currentPointToArrays();
//		System.err.println(String.format("怪物[%d]发送新Path:%s", this.getId(), Arrays.toString(currentPointToArrays)) );
//		return currentPointToArrays;
		
		synchronized (this.path) {
			if(this.path.size() > 0){
				return this.path.toArray();
			} else {
				return new Object[]{};
			}
		}
	}

	public IMonsterConfig getMonsterConfig() {
		return monsterConfig;
	}

	/**
	 * 运行怪物AI
	 */
	public void runAi() {
		if(monsterAiLevel != null){
			monsterAiLevel.execute(this);
		}
	}

	/**
	 * 用算法到目的地的路径
	 * @param useAStar
	 */
	public boolean walkToTarget(WalkType type){
		if(this.isArrivalTarget() || !hasRouteTarget() || actionDelay - System.currentTimeMillis() > 0){
			return false;
		}
		Point routeTarget = this.routeTarget;
		if(routeTarget == null){
			return  false;
		}
		List<Point> astartPoints = null;
		if(type.isStand()){
			routeTarget = correctStandPoint(routeTarget);
		}
		astartPoints = MapUtils.findAllPointByDistance(this.x, this.y, routeTarget.x, routeTarget.y, this.gameMap);
		if(type == WalkType.ASTAR) {			//追人
			if(astartPoints != null && astartPoints.size() < 2 ){
				boolean boss = getMonsterFightConfig().isBoss();
//				long start = System.currentTimeMillis();
				astartPoints = this.gameMap.findForAStar(this.x, this.y, routeTarget.x, routeTarget.y, boss ? 120 : 60);//A星寻路超时时间(毫秒)
//				long end = System.currentTimeMillis();
//				System.err.println(String.format("耗时：%d path:%s", (end-start), astartPoints != null ? astartPoints.toString() : null ));
			}
			
		}
		else if(type == WalkType.OPTIMIZE) {	//回家
			if(astartPoints == null || astartPoints.isEmpty() || !astartPoints.get(astartPoints.size() - 1).equals(routeTarget) ){
				boolean boss = getMonsterFightConfig().isBoss();
//				long start = System.currentTimeMillis();
				astartPoints = this.gameMap.findForAStar(this.x, this.y, routeTarget.x, routeTarget.y, boss ? 120 : 60);//A星寻路超时时间(毫秒)
//				long end = System.currentTimeMillis();
//				System.err.println(String.format("耗时：%d path:%s", (end-start), astartPoints != null ? astartPoints.toString() : null ));
			}
			
		}
		setMontionPath(astartPoints);
		return astartPoints != null;
	}

	/**
	 * 是否到达目的地
	 * @return
	 */
	public boolean isArrivalTarget() {
		if(routeTarget == null) {
			return true ;
		}
		if(this.routeTarget.x == this.getX() && this.routeTarget.y == this.getY()){
			return true ;
		}
		return false ;
	}
	
	/**
	 * 修正站立点
	 * @param targetPoint
	 * @return
	 */
	public Point correctStandPoint(Point targetPoint){
		//检查下一步是否有怪物 或玩家，如果有就随机散下步，如果连散步的地方都有怪物的话 那就不走动...
		if(targetPoint != null) {
			int count = 0;
			do{
				if( gameScreen.hasSpireInThisPoint(targetPoint.x, targetPoint.y, ElementType.MONSTER,ElementType.PLAYER) ){
					Point randomPos = DirectionUtil.getRandomPos(this.x, this.y, targetPoint, this.gameMap);
					if(gameMap != null && gameMap.isPathPass(targetPoint.x, targetPoint.y)) {
						targetPoint.setLocation( randomPos );
						break;
					}
					
					if(count++ > 5){
						break;
					}
					
				} else {
					break;
				}
				
			} while(true);
			
		}
		return targetPoint;
	}
	
	public boolean moving(){
		if(actionDelay - System.currentTimeMillis() > 0){
			return false;
		}
		synchronized (path) {
			if(path.size() >= 2){
				if(BufferHelper.isMonsterInImmobilize(this.monsterBuffer)){
					return false;
				}
				Integer x = path.remove(0);
				Integer y = path.remove(0);
				if(this.x == x && this.y == y){
					if(path.size() >= 2 ){
						x = path.remove(0);
						y = path.remove(0);
					} else {
						return false;
					}
				}
				if(!gameMap.isPathPass(x, y)){
					path.clear();
//					removeRouteTarget();
					return false;
				}
				
				//行动延迟 怪物走动得越快值越小
				int moveStepTime = MoveUtil.getMoveStepTime(this.x, this.y, x, y, this.getMonsterBattle().getMoveSpeedServerData());
//				if(this.getId() == 11075){
//					System.err.println(String.format("怪物[%d]移动延时[%d]", this.getId(), moveStepTime));
//					System.err.println(String.format("怪物[%d]移动方向[%d]", this.getId(), DirectionUtil.direction(this.x, this.y, x, y)));
//					System.err.println(String.format("怪物[%d]移动到坐标[%d, %d]", this.getId(), this.x, this.y ) );
//				}
				actionDelay = System.currentTimeMillis() + moveStepTime;
				this.x = x;
				this.y = y;
				
				return true;
			} else{
				path.clear();
			}
			
			if(path.isEmpty()){
				removeRouteTarget();
			}
			
		}
		return false;
	}

	/**
	 * 给予行走路径
	 * @param points
	 */
	public void setMontionPath(List<Point> points) {
		if(points == null || points.isEmpty()) {
			return ;
		}
		
		synchronized (path) {
			path.clear();
			if(points.size() > 1){	//给两个坐标，让客户端显示行走更流畅
				List<Integer> list = new ArrayList<Integer>();
				for(Point p : points){
					list.add(p.x);
					list.add(p.y);
				}
				path.addAll(list);
			}
			this.hasNewPath = true;
//			actionDelay = System.currentTimeMillis() + 20;
		}
//		if(this.getId() == 10110){
//			System.err.println(String.format("怪物[%d]设置新的Path:%s", this.getId(), Arrays.toString(points.toArray())) );
//		}
	}

	/**
	 * 是否不在出生地
	 * @return
	 */
	public boolean notInBoth() {
		return this.x != monsterConfig.getBornX() && this.y != monsterConfig.getBornY();
	}

	/**
	 * 是否有攻击目标
	 * @return
	 */
	public boolean hasAttackTarget() {
		return getCanAttackPlayer() != null;
	}
	
	public long getMonsterId() {
		return this.monster.getId();
	}

	/**
	 * 查询怪物的BUFF信息
	 * 
	 * @param  checkBufferTimeOut		是否检测超时
	 * @return {@link MonsterBuffer} 	怪物的BUFF信息
	 */
	public MonsterBuffer getMonsterBuffer(boolean checkBufferTimeOut) {
		if(checkBufferTimeOut && MonsterHelper.calcMonsterBufferTimeOut(monsterBuffer)) {
			this.monsterBattle.updateFlushable(Flushable.FLUSHABLE_NORMAL);
			Collection<Long> playerIdList = this.gameMap.getCanViewsSpireIdCollection(this, ElementType.PLAYER);
			UserPushHelper.pushAttribute2AreaMember(monster.getId(), playerIdList, Arrays.asList(this.unitId), AttributeRule.MONSTER_BUFFER_TIMEOUT_PARAMS);
		}
		return monsterBuffer;
	}
	
	/**
	 * 获取当前可用技能
	 * @return
	 */
	public SkillMonitor getCurrentUseSkillMonitor() {
		if(!aiActionMonitor.isOverTired(GOLBAL_SKILLCD)){
			return null ;
		}
		if(cacheSkill == null){
			SkillMonitor skillMonitor = aiActionMonitor.getSkillByIdPercent(this.getMonsterBattle().getMonsterHpPercent());
			if( skillMonitor != null && !aiActionMonitor.isOverTired(String.valueOf(skillMonitor.getSkillId()) ) ){
				return null;
			} else {
				cacheSkill = skillMonitor;
			}
		}
		return cacheSkill;
	}

	/**
	 * 获取当前要释放技能的距离
	 * @return
	 */
	public int getCurrentUseSkillAttackDistance(){
		SkillMonitor monitor = this.getCurrentUseSkillMonitor();
		if(monitor == null){
			return -1 ;
		}
		int distance = monitor.getSkillAttackDistance() ;
		//释放单位是0 的话是对自身释放，当玩家接近警戒范围开始释放
		return distance == 0 ? 2 : distance ;
	}
	
	/**
	 * 添加冷却时间
	 * @param skillMonitor
	 * @param coolTime
	 */
	public void addTired(SkillMonitor skillMonitor, int coolTime) {
		aiActionMonitor.addTired(String.valueOf(skillMonitor.getSkillId()), coolTime);
		aiActionMonitor.addTired(GOLBAL_SKILLCD, Math.max(this.getMonsterFightConfig().getAttackInterval(),300));
	}

	/**
	 * 是否准备复活
	 * @return
	 */
	public boolean isPrepareResurrection() {
		return aiActionMonitor.isHasTired(RESURRECTION_TIME);
	}

	/**
	 * 准备复活
	 */
	public void prepareResurrection() {
		int reviveTime = this.getMonsterFightConfig().getReviveTime() * 1000;
		aiActionMonitor.addTired(RESURRECTION_TIME, reviveTime);
		//TODO 复活时间CD
//		aiActionMonitor.addTired(RESURRECTION_TIME, this.RESURRECTION_TIME_CD);
	}

	/**
	 * 是否到复活时间
	 * @return
	 */
	public boolean isTimeToResurrection() {
		return aiActionMonitor.isOverTired(RESURRECTION_TIME);
	}

	/**
	 * 复活
	 */
	public void resurrection(boolean force) {
		MonsterBattle battle = this.getMonsterBattle();
		MonsterFightConfig monsterFight = battle.getMonsterFight();
		if(!force && monsterFight.getReviveTime() == -1) {
			return ;
		}
		MonsterHelper.pushBossResurrection(this);
		removeAttackTarget();
		clearPath();
		removeRouteTarget();
		synchronized (this) {
			if(this.gameScreen != null){
				this.gameScreen.leaveScreen(this);
			}
			aiActionMonitor.removeAllTired();
			aiActionMonitor.addTired(REVIVE_ACTION_DELAY, 2000);
			battle.resurrection();
			this.getMonsterBattle();
			this.x = this.monsterConfig.getBornX();
			this.y = this.monsterConfig.getBornY();
			this.removeCorpse = false ;
			if(gameMap != null){
				gameMap.enterMap(this);
			}
		}
		MonsterHelper.pushBossResurrection(this);
	}
	
	/**
	 * 满血
	 */
	public void fullHP(){
		MonsterBattle battle = this.getMonsterBattle();
		if(gameMap != null && gameMap.getMapId() == CampBattleRule.CAMP_BATTLE_MAPID){
			return ;
		}
		synchronized (this) {
			if(this.gameScreen != null){
				this.gameScreen.leaveScreen(this);
			}
			aiActionMonitor.removeAllTired();
			aiActionMonitor.addTired(REVIVE_ACTION_DELAY, 2000);
			removeAttackTarget();
			battle.resurrection();
			this.getMonsterBattle();
			this.x = this.monsterConfig.getBornX();
			this.y = this.monsterConfig.getBornY();
			this.removeCorpse = false ;
			if(gameMap != null){
				gameMap.enterMap(this);
			}
			this.redRange = this.getMonsterFightConfig().getWarnRange();
		}
	}
	
	public void resurrection() {
		resurrection(false);
	}

	/**
	 * 移除攻击目标
	 */
	public void removeAttackTarget(){
		if(this.attackTarget != null){
			getMonsterBattle().removeHurtInfo(this.attackTarget);
			this.attackTarget = null ;
		}
	}
	
	/**
	 * 是否需要移除尸体
	 * @return
	 */
	public boolean needToRemoveCorpse() {
		return aiActionMonitor.isOverTired(REMOVE_CORPSE);
	}

	/**
	 * 设置可看玩家
	 * @param playerWatcherList
	 */
	public void setCanWatchPlayersAndPets(Collection<ISpire> spireCollection) {
		synchronized (this.spireCollection) {
			this.spireCollection.clear();
			this.spireCollection.addAll(spireCollection) ;
		}
	}

	/**
	 * 在视野范围寻找攻击目标
	 */
	public void findTargetPlayer() {
		MonsterFightConfig monsterFightConfig = this.getMonsterFightConfig();
		//从追击和警界格子数中取最大的格子数
		if(this.hasAttackTarget()){
			if( attackTarget != null && this.getMapId() != attackTarget.getMapId()){
				this.removeAttackTarget();
			}
			if(!inViews(attackTarget)){	//是否在视野中
				this.removeAttackTarget();
			}
			if( shouldGoHome() ){
				this.removeAttackTarget();
			}
		}
		//寻找仇人&寻找仇恨值最高的人
		this.redictAttackPlayer();
		
		//从可视区域中寻找攻击目标
		findAttackTargetInView(monsterFightConfig);
	}

	/**
	 * 寻找找到攻击目标
	 * @param spires
	 */
	private void findAttackTargetInView(MonsterFightConfig monsterFightConfig) {
		if(!this.hasAttackTarget() && this.spireCollection.size() > 0){
			findAttackTargetFromSpires(this.spireCollection);
		}
		if(!this.hasAttackTarget()){
			Set<ISpire> canViewsMonsters = this.gameMap.getCanViewsSpireCollection(this, ElementType.MONSTER);
			if(canViewsMonsters.size() > 1){		//不要包括怪物自己
				TreeSet<ISpire> spires = new TreeSet<ISpire>(new Comparator<ISpire>() {
					
					public int compare(ISpire spire1, ISpire spire2) {
						int monsterCamp1 = ((MonsterDomain)spire1).getMonsterCamp();
						int monsterCamp2 = ((MonsterDomain)spire2).getMonsterCamp();
						if(monsterCamp1 == monsterCamp2 ){
							return 0;
						}else if(monsterCamp1 == 0 && monsterCamp2 > 0 ){
							return 1;
						}else if(monsterCamp1 > 0 && monsterCamp2 > 0 ){
							return 0;
						}else if(monsterCamp1 > 0 && monsterCamp2 == 0 ){
							return -1;
						}
						return 1;
					}
				});
				spires.addAll(canViewsMonsters);
				findAttackTargetFromSpires(spires);
			}
		}
		if(!this.hasAttackTarget()){
			this.clear = true;
			this.redRange = monsterFightConfig.getWarnRange();
		}
	}
	
	/**
	 * 从指定精灵中找到攻击目标
	 * @param spires
	 */
	private void findAttackTargetFromSpires(Collection<ISpire> spires) {
		final int monsterCamp = this.getMonsterCamp();
		MonsterFightConfig monsterFightConfig = this.getMonsterFightConfig();
		
		if(!this.hasAttackTarget() && spires.size() > 0){
			for(ISpire watcher : spires){
				ElementType type = watcher.getType();
				if(type != ElementType.PLAYER && type != ElementType.PET && type != ElementType.MONSTER){
					continue;
				}
				if(watcher.getGameMap() != this.gameMap){
					continue;
				}
				boolean isChecked = false;
				if(redRange == 0){
					redRange = monsterFightConfig.getWarnRange();
				}
				if(redRange >= monsterFightConfig.getWarnRange() && MapUtils.checkPosScopeInfloat(watcher,this, monsterFightConfig.getWarnRange()) ){
					redRange = monsterFightConfig.getPursueRange();
					isChecked = true;
				}
//					System.err.println(String.format("target:[%d,%d], watcher:[%d,%d] 红色警戒:%d", this.getX(), this.getY(), watcher.getX(), watcher.getY(), redRange) );
				if(isChecked || MapUtils.checkPosScopeInfloat(watcher,this, redRange) ){
//					System.err.println(String.format("--monsterId[%s]发现目标----target:[%d,%d], watcher:[%d,%d] 红色警戒:%d", this.getId(), this.getX(), this.getY(), watcher.getX(), watcher.getY(), redRange) );
					GameScreen currentScreen = watcher.getCurrentScreen();
					if(currentScreen == null || currentScreen.getGameMap() != this.gameMap){	//不在同一个地图中
						getMonsterBattle().removeHurtInfo(watcher);
						if(watcher == this.attackTarget){
							removeAttackTarget();
						}
						continue;
					}
					if(type == ElementType.PLAYER){
						UserDomain userDomain = (UserDomain)watcher ;
						if(monsterCamp > 0 && userDomain.getPlayer().getCamp().ordinal() == monsterCamp){
							continue;
						}
						//玩家死了消除仇恨值
						PlayerBattle battle = userDomain.getBattle();
						if(battle.isDead()){
							getMonsterBattle().removeHurtInfo(watcher);
							if(watcher == this.attackTarget){
								removeAttackTarget();
							}
							continue ;
						} else if(userDomain.getPlayer().isReviveProteTime()) {
							if(watcher == this.attackTarget){
								removeAttackTarget();
							}
							continue;
						}else{
							setAttackTarget(watcher);
							break ;
						}
						
					} else if(type == ElementType.PET){
						PetDomain petDomain = (PetDomain)watcher ;
						UserDomain userDomain = PetHelper.getUserDomain(petDomain.getId());
						if(userDomain == null){
							continue;
						}
						if(monsterCamp > 0 && userDomain.getPlayer().getCamp().ordinal() == monsterCamp){
							continue;
						}
						
						PetBattle petBattle = petDomain.getBattle();
						Pet pet = petDomain.getPet();
						//家将死了或没有出战消除仇恨值
						if(petBattle.isDeath() || !pet.isFighting() ){
							getMonsterBattle().removeHurtInfo(petDomain);
							if(watcher == this.attackTarget){
								removeAttackTarget();
							}
							continue ;
						} else if(userDomain.getPlayer().isReviveProteTime()) {
							if(watcher == this.attackTarget){
								removeAttackTarget();
							}
							continue;
						} else {
							setAttackTarget(watcher);
							break ;
						}
					} else if(type == ElementType.MONSTER){
						MonsterDomain monster = (MonsterDomain)watcher;
						if(monster.getMonsterCamp() == this.getMonsterCamp() ){	//同阵营怪不能攻击
							continue;
						}
						if(monster.getMonsterBattle().isDead()){
							getMonsterBattle().removeHurtInfo(monster);
							if(watcher == this.attackTarget){
								removeAttackTarget();
							}
						} else {
							setAttackTarget(watcher);
							break;
						}
						
					} else{
						setAttackTarget(watcher);
						break ;
					}
					
				}
			}
		}
		
	}
	
	/**
	 * 是否在视野范围内
	 * @param attackTarget2
	 * @return
	 */
	public boolean inViews(ISpire attackTarget) {
		if(this.spireCollection != null){
			synchronized (spireCollection) {
				return spireCollection.contains(attackTarget);
			}
		}
		return false;
//		return this.findSpireById(attackTarget.getId()) != null;
	}

	/**
	 * 根据编号寻找Spire
	 * @param id
	 * @return
	 */
	public ISpire findSpireById(long id){
		if(spireCollection != null && spireCollection.size() > 0){
			synchronized (spireCollection) {
				for(ISpire spire : spireCollection){
					if(spire.getId() == id){
						return spire ;
					}
				}
			}
		}
		return null ;
	}
	
	/**
	 * 获取可攻击目标
	 * @return
	 */
	public ISpire getCanAttackPlayer() {
		return this.attackTarget;
	}
	
	/**
	 * 获取可看到的Spire
	 * @return
	 */
	public Collection<ISpire> getCanWatchSpires() {
		return this.spireCollection;
	}

	/**
	 * 是否是时候回家了
	 * @return
	 */
	public boolean shouldGoHome() {
		int pursueRange = this.getMonsterFightConfig().getPursueRange();
		int attactdistance = this.getCurrentUseSkillAttackDistance();
		int range = this.hasAttackTarget() ? pursueRange + (attactdistance > 0 ? attactdistance : 0) : pursueRange;
		if(!MapUtils.checkPosScopeInfloat(this.getX(), this.getY(), this.monsterConfig.getBornX(), this.monsterConfig.getBornY(), range, false)){
			this.redRange = getMonsterFightConfig().getWarnRange();
			return true;
		}
		return false;
	}

	/**
	 * 回家咯
	 */
	public void goHome() {
		this.goHome = true ;
	}

	/**
	 * 是否正在回家
	 * @return
	 */
	public boolean goHomeing() {
		return this.goHome;
	}
	
	/**
	 * 到家了
	 */
	public void atHome(){
		this.goHome = false ;
	}

	/**
	 * 是否在家
	 */
	public boolean checkIsAtHome() {
		if(this.getX() == monsterConfig.getBornX() && this.getY() == monsterConfig.getBornY()){
			atHome() ;
			return true;
		}
		return false;
	}

	/**
	 * 动手打人
	 * @param skillMonitor
	 * @param coolTime
	 */
	public void attacked(SkillMonitor skillMonitor, int coolTime) {
		cacheSkill = null ;
		this.addTired(skillMonitor, coolTime);
	}

	/**
	 * 设置路径目标
	 * @param point
	 */
	public void setRouteTarget(Point point) {
		this.routeTarget = point ;
	}
	
	public boolean isRouteTargetChange(Point point){
		return !point.equals(this.routeTarget);
	}

	/**
	 * 移除路劲
	 */
	public void removeRouteTarget() {
		if(this.routeTarget != null){
			this.routeTarget = null ;
		}
		
//		synchronized (this.path) {
//			this.path.clear();	//设置path为空
//		}
	}

	/**
	 * 是否有要去的地方
	 * @return
	 */
	public boolean hasRouteTarget() {
		return this.routeTarget != null ;
	}
	
	/**
	 * 检查目标位置是否变了
	 * @param spire
	 * @return	true:目标位置没有变, false:目标位置变了
	 */
	public boolean checkTargetPoint(ISpire spire, int ingoreOnePoint) {
		if(this.routeTarget != null){	
			return MapUtils.checkPosScopeInfloat(spire.getX(), spire.getY(), this.routeTarget.x, this.routeTarget.y, ingoreOnePoint);
		}
		return false;
	}

	public MonsterFightConfig getMonsterFightConfig() {
		return this.getMonsterBattle().getMonsterFight();
	}

	/**
	 * 寻找仇人&寻找仇恨值最高的人
	 */
	public void redictAttackPlayer() {
		MonsterBattle battle = getMonsterBattle();
		Map<ISpire, HurtInfo> hurtInfoMap = new HashMap<ISpire, HurtInfo>( battle.getHurtInfo() );
		if ( hurtInfoMap.isEmpty() ) {
			return;
		}
		
		List<HurtInfo> hurtInfos = new ArrayList<HurtInfo>( hurtInfoMap.values() );
		Collections.sort( hurtInfos );
		
		for(HurtInfo hurtInfo : hurtInfos) {
			ISpire spire = hurtInfo.getSpire();
			if (spire == null) {
				continue;
			}
			
			if( spire.getGameMap() != this.gameMap ){
				battle.removeHurtInfo( spire );
				continue;
			}
			
			if (spire instanceof UserDomain) {
				UserDomain userDomain = (UserDomain) spire;
				// 玩家死了消除仇恨值
				if ( userDomain.getBattle().isDead() ) {
					battle.removeHurtInfo(spire);
					if(spire == this.attackTarget){
						this.removeAttackTarget();
					}
					continue;
				}
				
			} else if(spire instanceof PetDomain){
				PetDomain petDomain = (PetDomain)spire ;
				PetBattle petBattle = petDomain.getBattle();
				if(petBattle == null){
					continue;
				}
				Pet pet = petDomain.getPet();
				if(pet == null){
					continue;
				}
				//家将死了或没有出战消除仇恨值
				if(petBattle.isDeath() || !pet.isFighting()){
					getMonsterBattle().removeHurtInfo(spire);
					if(spire == this.attackTarget){
						this.removeAttackTarget();
					}
					continue ;
					
				}
				
			} else if(spire instanceof MonsterDomain){
				MonsterDomain monster = (MonsterDomain)spire;
				if(monster.getMonsterCamp() == this.getMonsterCamp() ){	//同阵营怪不能攻击
					continue;
				}
				if(monster.getMonsterBattle().isDead()){
					getMonsterBattle().removeHurtInfo(monster);
					if(spire == this.attackTarget){
						removeAttackTarget();
					}
					
				}
				
			} else {	//从怪物的可视区移除了
				battle.removeHurtInfo(spire);
				if(spire == this.attackTarget){
					this.removeAttackTarget();
				}
				
				continue;
			}
			
			setAttackTarget(spire);	//设置目标
			break;
			
		}
		
		if( this.hasAttackTarget() ){
			
		} else {
			this.clear = true;		//没有目标清理
		}
	}
	
	private boolean setAttackTarget(ISpire spire){
		this.attackTarget = spire;
		return true;
	}

	/**
	 * 是否准备删除尸体
	 * @return
	 */
	public boolean isPrepareRemoveCorpse() {
		return aiActionMonitor.isHasTired(REMOVE_CORPSE);
	}

	/**
	 * 准备删除尸体
	 */
	public void prepareRemoveCorpse() {
		aiActionMonitor.addTired(REMOVE_CORPSE, REMOVE_CORPSE_CD);
	}

	/**
	 * 移除尸体
	 */
	public boolean removeCorpse() {
		this.removeCorpse = aiActionMonitor.removeTired(REMOVE_CORPSE);
		return this.removeCorpse;
	}
	
	public boolean isPushView(){
		if(getMonsterBattle().isDead()){
			long tiredTime = aiActionMonitor.getTiredTime(REMOVE_CORPSE);
			long time = System.currentTimeMillis() - tiredTime;
			return !( this.removeCorpse || time >= 0 && time < 350 );
		}
		return true;
	}
	
	public long getTiredTime(String key){
		return aiActionMonitor.getTiredTime(key);
	}

	/**
	 * 是否已经移除尸体
	 * @return
	 */
	public boolean isRemoveCorpse() {
		return this.removeCorpse;
	}

	public boolean needToClearData() {
		return this.clear;
	}

	public void clear() {
		if(isTimeToResurrection() && this.getMonsterFightConfig().canRevive() ){
			if(this.gameMap != null && this.gameMap.getMapId() == CampBattleRule.CAMP_BATTLE_MAPID){
			} else {
				this.resurrection();
			}
			
		} else {
			removeAttackTarget();
			clearPath();
			removeRouteTarget();
		}
		this.clear = false ;
		this.redRange = this.getMonsterFightConfig().getWarnRange();
	}

	
	/**
	 * 获取当前场景
	 */
	public GameScreen getCurrentScreen() {
		if(this.gameScreen == null){
			return null;
		}
		GameMap gameMap = this.gameScreen.getGameMap();
		if(gameScreen.checkInThisGameScreen(this)){
			return this.gameScreen;
		}
		this.gameScreen = gameMap.getGameScreen(this) ;
		return gameScreen;
	}

	
	public void recordScreen(GameScreen gameScreen) {
		synchronized (this) {
			this.gameScreen = gameScreen ;
		}
	}

	
	public ElementType getType() {
		return ElementType.MONSTER;
	}

	/**
	 * 切换场景
	 */
	
	public void changeScreen(GameScreen toGameScreen) {
		this.leaveScreen();
		enterScreen(toGameScreen);
	}

	/**
	 * 切换地图
	 */
	
	public boolean changeMap(GameMap targetGameMap, int x, int y) {
		synchronized (this) {
			GameScreen screen = targetGameMap.getGameScreen(x, y);
			if(screen == null){
				return false ;
			}
			this.leaveScreen();
			this.enterScreen(screen);
			this.gameMap = targetGameMap;
			this.mapId = targetGameMap.getMapId();
			this.x = x; 
			this.y = y;
		}
		return true ;
	}

	public void clearPath(){
		synchronized (this.path) {
			this.path.clear();
		}
	}
	
	public LinkedList<Integer> getPath() {
		return path;
	}

	/**
	 * 进入场景
	 * @param screen
	 */
	public void enterScreen(GameScreen screen) {
		screen.enterScreen(this);
		this.gameScreen = screen ;
	}

	/**
	 * 离开场景
	 */
	public void leaveScreen(){
		if(gameScreen != null){
			this.gameScreen.leaveScreen(this);
		}
	}
	
	/**
	 * 场景是否转换了
	 * @param gameScreen
	 * @return
	 */
	public boolean isChangeScreen(GameScreen gameScreen) {
		if(this.gameScreen == null || gameScreen != this.gameScreen ){
			return true ;
		}
		return false;
	}
	
	public int getMonsterType(){
		return getMonsterFightConfig().getMonsterType();
	}
	
	public void add2MonsterView(ISpire... spires) {
		if(spires != null && spires.length > 0){
			if(!this.monsterBattle.isDead()) {
				synchronized (this.spireCollection) {
					for(ISpire spire : spires){
						this.spireCollection.add(spire);
					}
				}
			}
		}
	}
	
	/** 怪物的阵营，特殊处理 */
	private Camp monsterCamp = null;
	public int getMonsterCamp() {
		if(monsterCamp == null){
			return getMonsterFightConfig().getMonsterCamp();
		} else {
			return monsterCamp.ordinal();
		}
	}
	
	public void setMonsterCamp(Camp monsterCamp) {
		synchronized (this) {
			this.monsterCamp = monsterCamp;
		}
	}

	public long getTired() {
		return aiActionMonitor.getTiredTime(RESURRECTION_TIME);
	}
	
//
//	public void add2MonsterView(Collection<ISpire> spires) {
//		if(spires != null && spires.size() > 0){
//			if(!this.monsterBattle.isDeath()) {
//				this.spireCollection.addAll( spires );
//			}
//		}
//	}
//
	public void removeFromMonsterView(ISpire... spires) {
//		if(this.monsterBattle.isDead()){
//			this.spireCollection.clear();
//			return;
//		}
		if(spires != null && spires.length > 0){
			synchronized (this.spireCollection) {
				for(ISpire spire : spires){
					this.spireCollection.remove(spire);
				}
			}
		}
	}

	public void removeFromMonsterView(Collection<ISpire> spires) {
//		if(this.monsterBattle.isDeath()){
//			this.spireCollection.clear();
//			return;
//		}
		if(spires != null && spires.size() > 0){
			synchronized (this.spireCollection) {
				for(ISpire spire : spires){
					this.spireCollection.remove(spire);
				}
			}
		}
	}
	
	public void addFightInfo(ISpire spire, int hurt, int skillId) {
		this.monsterBattle.addFightInfo(spire, hurt, skillId);
		if(spire != null){
			GameMap gameMap2 = null;
			if(spire instanceof UserDomain){
				UserDomain userDomain = (UserDomain)spire;
				gameMap2 = userDomain.getGameMap();
				
				//处理怪物在客户端和服务端的位置不一致的问题，修改怪物的坐标玩家附近
				if( !this.spireCollection.contains(spire) && gameMap2 == this.gameMap) {
					int ailevel = this.getMonsterFightConfig().getAilevel();
					AiLevel aiLevel = AiLevel.values()[ ailevel - 1];
					if(aiLevel != null){
						AbstractAiNode[] aiNodes = aiLevel.aiNodes();
						for(AbstractAiNode aiNode : aiNodes){
							if(aiNode instanceof AStartAiNode){
								this.clearPath();
								int direction = DirectionUtil.direction(spire.getX(), spire.getY(), this.x, this.y);
								int[] directionValue = DirectionUtil.getDirectionValue(direction);
								int newX = spire.getX() + directionValue[0];
								int newY = spire.getY() + directionValue[1];
								this.setRouteTarget( new Point(newX, newY) );
								this.walkToTarget(WalkType.ASTAR);
								break;
							}
						}
					}
				}
				
			} else if(spire instanceof PetDomain){
				UserDomain userDomain = PetHelper.getUserDomain(spire.getId());
				if(userDomain != null){
					gameMap2 = userDomain.getGameMap();
				}
				
			}
			
			if(gameMap2 != null && gameMap2 != this.gameMap){
				gameMap2.leaveMap(spire);
			}
			
		}
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		MonsterDomain other = (MonsterDomain) obj;
		return unitId != null && other.unitId != null && unitId.equals(other.unitId);
	}

	/** 怪物运行future */
	private MonsterFutrue monsterFutrue;
	
	public MonsterFutrue getMonsterFutrue() {
		if(monsterFutrue == null){
			synchronized (this) {
				if(monsterFutrue == null){
					monsterFutrue = MonsterFutrue.valueOf(this);
				}
			}
		}
		return monsterFutrue;
	}
	
}
