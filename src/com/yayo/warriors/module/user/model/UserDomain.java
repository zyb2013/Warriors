package com.yayo.warriors.module.user.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.types.ItemLimitTypes;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.Player.PackLock;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.helper.UserHelper;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.ElementType;

/**
 * 用户域模型对象
 * 
 * @author Hyint
 */
public class UserDomain implements ISpire {

	/** 角色ID */
	private UnitId unitId;
	
	/** 角色对象 */
	private Player player;
	
	/** 角色战斗对象 */
	private PlayerBattle battle;
	
	/** 角色移动对象 */
	private PlayerMotion motion;
	
	/** 用户BUFFER信息 */
	private UserBuffer userBuffer;
	
	/** 用户技能对象 */
	private UserSkill userSkill;
	
	/** 角色所在的场景信息 */
	private GameScreen gameScreen;
	
	/** 需要下发给玩家的精灵队列  协议号102=0加入场景 103=1离开场景*/
	@SuppressWarnings("unchecked")
	private Queue<ISpire> [] spireQueue = new ConcurrentLinkedQueue [3];
	
	public Player getPlayer() {
		return player;
	}

	public PackLock getPackLock(){
		return this.player.getPackLock();
	}
	
	public PlayerBattle getBattle() {
		return UserHelper.refreshBattleAttribute(this.battle, this);
	}

	public UserSkill getUserSkill() {
		return userSkill;
	}

	public PlayerMotion getMotion() {
		return motion;
	}
	
	public void updateFlushable(boolean isLock, int flushable) {
		if(isLock) {
			ChainLock lock = LockUtils.getLock(battle);
			try {
				lock.lock();
				battle.setFlushable(flushable);
			} finally {
				lock.unlock();
			}
		} else {
			battle.setFlushable(flushable);
		}
	}
	
	public long getPlayerId() {
		return unitId.getId();
	}

	/**
	 * 构建与模型对象
	 * 
	 * @param  player				角色对象
	 * @param  battle				角色战斗对象
	 * @param  motion				角色移动对象
	 * @param  userSkill			角色的技能对象
	 * @return {@link UserDomain}	用户域模型对象
	 */
	public static UserDomain valueOf(Player player, PlayerBattle battle, 
		PlayerMotion motion, UserBuffer userBuffer, UserSkill userSkill) {
		UserDomain userDomain = new UserDomain();
		userDomain.battle = battle;
		userDomain.player = player;
		userDomain.motion = motion;
		userDomain.userSkill = userSkill;
		userDomain.userBuffer = userBuffer;
		userDomain.unitId = UnitId.valueOf(player.getId(), ElementType.PLAYER);
		return userDomain;
	}
	
	public static UserDomain valueOf(InitCreateInfo initCreateInfo) {
		UserDomain userDomain = new UserDomain();
		userDomain.battle = initCreateInfo.getBattle();
		userDomain.player = initCreateInfo.getPlayer();
		userDomain.motion = initCreateInfo.getPlayerMotion();
		userDomain.userSkill = initCreateInfo.getUserSkill();
		userDomain.userBuffer = initCreateInfo.getUserBuffer();
		userDomain.unitId = UnitId.valueOf(initCreateInfo.getPlayerId(), ElementType.PLAYER);
		return userDomain;
	}
	
	
	public void recordScreen(GameScreen gameScreen) {
		this.gameScreen = gameScreen ;
	}
	
	
	public UnitId getUnitId() {
		return this.unitId;
	}
	
	/**
	 * 在该地图中是否可以使用,该类型物品
	 * @param type 类型,参见{@link ItemLimitTypes}
	 * @return true 可以 false 不可以
	 */
	public boolean canUsePropsInMap(int type){
		if(this.gameScreen == null){
			return true;
		}
		
		GameMap gameMap = this.gameScreen.getGameMap();
		if(gameMap == null){
			return true;
		}
		
		BigMapConfig config = gameMap.getBigMapConfig();
		if(config == null){
			return true;
		}
		
		return config.canUsePropsInMap(type);
	}

	/**
	 * 场景是否转换了
	 * @param gameScreen
	 * @return
	 */
	public boolean isChangeScreen(GameScreen gameScreen) {
		if(this.gameScreen == null || !gameScreen.equals(this.gameScreen )){
			return true ;
		}
		return false;
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

	public UserBuffer getUserBuffer() {
		if(userBuffer != null) {
			checkBufferTimeOut(userBuffer);
		}
		return userBuffer;
	}
	
	public GameMap getGameMap() {
		return this.gameScreen == null ? null : this.gameScreen.getGameMap();
	}
	
	/**
	 * 检测用户BUFF
	 * 
	 * @param userBuffer
	 */
	private void checkBufferTimeOut(UserBuffer userBuffer) {
		Map<Integer, Buffer> bufferInfos = userBuffer.getBufferInfos();
		Map<Integer, Buffer> itemBuffers = userBuffer.getItemBufferInfos();
		if(bufferInfos.isEmpty() && itemBuffers.isEmpty()) {
			return;
		}
		
		boolean refreshable = false;
		ChainLock lock = LockUtils.getLock(userBuffer);
		try {
			lock.lock();
			bufferInfos = userBuffer.getBufferInfos();
			itemBuffers = userBuffer.getItemBufferInfos();
			if(bufferInfos.isEmpty() && itemBuffers.isEmpty()) {
				return;
			}
			
			if(userBuffer.hasTimeOut(bufferInfos)) {
				userBuffer.updateBufferInfos(false);
				refreshable = true;
			} 
			
			if(userBuffer.hasTimeOut(itemBuffers)) {
				userBuffer.updateItemBufferInfos(false);
				refreshable = true;
			}
		} finally {
			lock.unlock();
		}
		
		if(refreshable) {
			updateFlushable(false, Flushable.FLUSHABLE_NORMAL);
			UserHelper.pushBufferAttributeChange2Area(this);
		}
	}
	
	
	public void changeScreen(GameScreen toGameScreen) {
		synchronized (this) {
			this.leaveScreen();
			enterScreen(toGameScreen);
		}
	}

	
	public boolean changeMap(GameMap targetGameMap, int x, int y) {
		GameScreen screen = targetGameMap.getGameScreen(x, y);
		synchronized (this) {
			this.leaveScreen();
			if(screen != null){
				this.enterScreen(screen);
			}
			this.motion.changeMap(targetGameMap.getMapId() , x,y);
		}
		return true ;
	}

	public void enterScreen(GameScreen targetScreen) {
		GameScreen screen = this.gameScreen;
		targetScreen.enterScreen(this);
		this.gameScreen = targetScreen ;
		if(screen != null && screen.getMapId() != this.getMapId() ){
			screen.leaveScreen(this);
		}
	}

	public void leaveScreen(){
		if(gameScreen != null){
			this.gameScreen.leaveScreen(this);
		}
	}

	
	public int getX() {
		return this.motion.getX();
	}

	public int getMapId() {
		return this.motion.getMapId();
	}
	
	
	public int getY() {
		return this.motion.getY();
	}

	
	public long getId() {
		return this.unitId.getId();
	}

	public int getBranching(){
		return this.player.getBranching() ;
	}

	
	public ElementType getType() {
		return ElementType.PLAYER;
	}

	public Set<ISpire> pollAllSpireQueue(SpireQueueType type) {
		Queue<ISpire> hideSpire = getSpireQueue(type);
		Set<ISpire> spireSet = null;
		if(hideSpire != null){
			spireSet = new HashSet<ISpire>();
			int currentSize = hideSpire.size();
			for(int i = 0 ; i < currentSize ; i++){
				ISpire spire = hideSpire.poll();
				if(spire == null){
					continue ;
				}
				spireSet.add(spire);
			}
		}
		return spireSet;
	}

	public void putCanViewSpire(ISpire spire){
		putSpire(spire, SpireQueueType.VIEW);
	}
	
	public void putCanViewSpire(Collection<ISpire> spireList){
		putSpireList(spireList, SpireQueueType.VIEW);
	}
	
	public void putHideSpire(ISpire spire){
		putSpire(spire, SpireQueueType.HIDE);
	}
	
	public void putHideSpire(Collection<ISpire> spireList){
		putSpireList(spireList, SpireQueueType.HIDE);
	}
	
	public void putMotionSpire(ISpire spire){
		putSpire(spire, SpireQueueType.MOTION);
	}
	
	public void putMotionSpire(Collection<ISpire> spireList){
		putSpireList(spireList,  SpireQueueType.MOTION);
	}
	
	public void putSpire(ISpire spire , SpireQueueType type){
		Queue<ISpire> spireQ = getSpireQueue(type);
//		if(!spireQ.contains(spire)){
			spireQ.add(spire);
//		}
	}
	
	public void putSpireList(Collection<ISpire> spires , SpireQueueType type){
		Queue<ISpire> spireQ = getSpireQueue(type);
		for(ISpire spire : spires){
//			if(!spireQ.contains(spire)){
				spireQ.add(spire);
//			}
		}
	}
	
	/**
	 * 取得队列
	 * @param type
	 * @return
	 */
	private Queue<ISpire> getSpireQueue(SpireQueueType type){
		int index = type.getValue();
		Queue<ISpire> spireQ = this.spireQueue[index] ;
		if(spireQ == null){
			synchronized (spireQueue) {
				spireQ = this.spireQueue[index];
				if(spireQ == null){
					spireQ = new ConcurrentLinkedQueue<ISpire>();
					this.spireQueue[index] = spireQ ;
				}
			}
		}
		return spireQ;
	}
	
	/**
	 *	玩家的精灵队列类型
	 */
	public enum SpireQueueType{
		/** 0-看到的 */
		VIEW(0),
		/** 1-隐藏的 */
		HIDE(1),
		/** 2-移动的 */
		MOTION(2);
		
		private int value;
		SpireQueueType(int value){
			this.value = value;
		}
		public int getValue() {
			return value;
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
		
		UserDomain other = (UserDomain) obj;
		return unitId != null && other.unitId != null && unitId.equals(other.unitId);
	}
}
