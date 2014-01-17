package com.yayo.warriors.module.npc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.type.ElementType;

/**
 * NPC 类
 * 
 * 注:type属性,对NPC这个类来说 元素包含了很多,出了玩家和怪物以外, 所有在场景上会动的元素都是NPC,这个是服务端的定义
 * 
 * @author liuyuhua
 */
public class Npc {

	/** NPC基础数据ID和类型 */
	private UnitId unitId;

	/** 类型ID */
	private int baseId;

	/** 头像icon */
	private int icon;

	/** 地图 ID */
	private int mapId;

	/** 名字 */
	private String name;

	/** 模型名字 */
	private int model;

	/** X 坐标 */
	private int bornX;

	/** Y 坐标 */
	private int bornY;

	/** 等级 */
	private int level;

	/** 场景类型 */
	private int screenType;

	/** npc配置对象 */
	private NpcConfig npcConfig;
	
	/** 游戏地图场景中npc监视 */
	private Map<GameMap, Long> monitor = Collections.synchronizedMap( new HashMap<GameMap, Long>(1) );
	
	/** 再生时间 */
	private int rebirthTime;

	/**
	 * 构造函数
	 * 
	 * @param config
	 *            NPC基础数据
	 * @return
	 */
	public static Npc valueOf(NpcConfig config) {
		Npc npc = new Npc();
		npc.npcConfig = config;
		npc.baseId = config.getBaseId();
		npc.level = config.getLevel();
		npc.mapId = config.getMapId();
		npc.model = config.getModel();
		npc.name = config.getName();
		npc.bornX = config.getBornX();
		npc.bornY = config.getBornY();
		npc.screenType = config.getScreenType();
		npc.rebirthTime = config.getRebirthTime();
		// npc.props = config.getProps();

		if (config.getIcon() != null) {
			npc.icon = config.getIcon();
		}
		npc.unitId = UnitId.valueOf(config.getId(), EnumUtils.getEnum(ElementType.class, config.getElementType()) );
		return npc;
	}
	
	/**
	 * 是否可见
	 * @param gameMap
	 * @return	true:可见		false: 不可见
	 */
	public boolean isCanView(GameMap gameMap){
		if(gameMap != null){
			synchronized (monitor) {
				Long time = monitor.get(gameMap);
				if(time == null){
					return true;
				} else if(time < System.currentTimeMillis()){
					monitor.remove(gameMap);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 取得超时游戏地图
	 * @return
	 */
	public Collection<GameMap> getTimeOutGameMap(Set<Npc> handleNpcSet){
		List<GameMap> gameMaps = new ArrayList<GameMap>(5);
		synchronized (monitor) {
			for(Iterator<GameMap> iterator = monitor.keySet().iterator(); iterator.hasNext(); ){
				GameMap gameMap = iterator.next();
				Long time = monitor.get(gameMap);
				if(time == null || time < System.currentTimeMillis()){
					iterator.remove();
					gameMaps.add(gameMap);
					synchronized (handleNpcSet) {
						handleNpcSet.remove(this);
					}
				}
			}
		}
		return gameMaps;
	}
	
	public void hide(GameMap gameMap){
		hide(gameMap, this.rebirthTime);
	}
	
	public void hide(GameMap gameMap, int hidTime){
		if(gameMap != null){
			synchronized (monitor) {
				if(!monitor.containsKey(gameMap)){
					monitor.put(gameMap, System.currentTimeMillis() + hidTime);
				}
			}
		}
	}
	
	public void view(GameMap gameMap){
		if(gameMap != null){
			synchronized (monitor) {
				monitor.remove(gameMap);
			}
		}
	}
	
	public int getRebirthTime() {
		return rebirthTime;
	}

	public void setRebirthTime(int rebirthTime) {
		this.rebirthTime = rebirthTime;
	}

	public UnitId getUnitId() {
		return unitId;
	}

	public ElementType getElementType() {
		return unitId.getType();
	}

	public void setElementType(ElementType elementType) {
		this.unitId.setType(elementType);
	}

	public Integer getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public Integer getId() {
		return (int)this.unitId.getId();
	}

	public void setId(int id) {
		this.unitId.setId(id);
	}

	public int getIcon() {
		return icon;
	}

	public NpcConfig getNpcConfig() {
		return npcConfig;
	}

	public void setNpcConfig(NpcConfig npcConfig) {
		this.npcConfig = npcConfig;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public Integer getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public int getBornX() {
		return bornX;
	}

	public void setBornX(int x) {
		this.bornX = x;
	}

	public int getBornY() {
		return bornY;
	}

	public void setBornY(int y) {
		this.bornY = y;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getScreenType() {
		return screenType;
	}

	public void setScreenType(int screenType) {
		this.screenType = screenType;
	}
}
