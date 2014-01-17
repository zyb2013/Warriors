package com.yayo.warriors.module.drop.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

/**
 * 战斗奖励封装对象
 * 
 * @author Hyint
 */
public class LootWrapper implements ISpire {

	/** 掉落ID封装类 */
	private UnitId unitId;
	
	/** 掉落类型 :{@link GoodsType} */
	private int goodsType;
	
	/** 掉落信息 */
	private int baseId;
	
	/** 掉落的数量 */
	private int amount;
	
	/** 单位: 毫秒 */
	private long endTime;

	/** 分线号 */
	private int branching;

	/** 公告ID */
	private boolean notice;

	/** 是否正在忙碌中 */
	private boolean busy;

	/** X 坐标点 */
	private int positionX;
	
	/** Y 坐标点 */
	private int positionY;

	/** 物品的绑定状态 */
	private boolean binding;

	/** 当前所在的场景 */
	private GameScreen gameScreen;

	/** 掉落该物品的怪物ID */
	private MonsterFightConfig monsterFight;

	/** 可以拾取的角色ID列表*/
	private Long[] sharePlayers = null;
	
	/** 是否被拾取*/
	private boolean pickup = false;
	
	public long getId() {
		return this.unitId == null ? -1 : this.unitId.getId();
	}

	
	public UnitId getUnitId() {
		return this.unitId;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isNotice() {
		return notice;
	}

	public void setNotice(boolean notice) {
		this.notice = notice;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public MonsterFightConfig getMonsterFight() {
		return monsterFight;
	}

	public void setMonsterFight(MonsterFightConfig monsterFight) {
		this.monsterFight = monsterFight;
	}

	public boolean isTimeOut() {
		return System.currentTimeMillis() >= this.endTime;
	}
	
	public Long[] getSharePlayers() {
		return sharePlayers;
	}

	public void setSharePlayers(Long[] sharePlayers) {
		this.sharePlayers = sharePlayers;
	}

	public int getBranching() {
		return branching;
	}

	public void setBranching(int branching) {
		this.branching = branching;
	}

	
	public int getX() {
		return this.positionX;
	}

	
	public int getY() {
		return this.positionY;
	}
	
	public int getMapId() {
		return this.gameScreen.getMapId();
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	/** 奖励自增ID对象 */
	private static final AtomicLong REWARD_ID_COUNTER = new AtomicLong(1);
	
	/**
	 * 奖励封装对象
	 * 
	 * @return {@link LootWrapper}
	 */
	public static LootWrapper valueOf(Collection<Long> playerIdList) {
		LootWrapper rewardWrapper = new LootWrapper();
		rewardWrapper.unitId = UnitId.valueOf(REWARD_ID_COUNTER.getAndIncrement(), ElementType.DROP_REWARD);
		if(playerIdList == null) {
			rewardWrapper.sharePlayers = new Long[0];
		} else {
			HashSet<Long> hashSet = new HashSet<Long>(playerIdList);
			rewardWrapper.sharePlayers = hashSet.toArray(new Long[hashSet.size()]);
		}
		return rewardWrapper;
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
		
		LootWrapper other = (LootWrapper) obj;
		return unitId != null && other.unitId != null && unitId.equals(other.unitId);
	}

	
	/**
	 * 获取当前场景
	 */
	public GameScreen getCurrentScreen() {
		synchronized (this) {
			GameMap gameMap = this.gameScreen.getGameMap();
			if(gameScreen.checkInThisGameScreen(this)){
				return this.gameScreen;
			}
			this.gameScreen = gameMap.getGameScreen(this) ;
		}
		return gameScreen;
	}

	
	public GameMap getGameMap() {
		GameScreen currentScreen = getCurrentScreen();
		return currentScreen != null ? currentScreen.getGameMap() : null;
	}

	
	public void recordScreen(GameScreen gameScreen) {
		this.gameScreen = gameScreen;
	}

	
	public ElementType getType() {
		return ElementType.DROP_REWARD;
	}

	
	public void changeScreen(GameScreen toGameScreen) {
		throw new RuntimeException("该方法禁止使用");
	}

	public void enterScreen(GameScreen screen) {
		synchronized (this) {
			this.gameScreen = screen ;
			screen.enterScreen(this);
		}
	}
	
	public void leaveScreen() {
		synchronized (this) {
			if(gameScreen != null){
				this.gameScreen.leaveScreen(this);
			}
		}
	}
	
	
	public boolean changeMap(GameMap targetGameMap, int x, int y) {
		synchronized (this) {
			this.positionX = x;
			this.positionY = y;
			GameScreen screen = targetGameMap.getGameScreen(x, y);
			if(screen != null){
				this.leaveScreen();
				this.enterScreen(screen);
			}
		}
		return true ;
	}
	
	public String getMonsterName() {
		return this.monsterFight == null ? "" : monsterFight.getName();
	}

	public boolean isPickup() {
		return pickup;
	}

	public void setPickup(boolean pickup) {
		this.pickup = pickup;
	}
	
}
