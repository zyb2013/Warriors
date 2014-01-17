package com.yayo.warriors.module.treasure.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.NumberUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.treasure.model.Treasure;

/**
 * 用户宝藏对象
 * 
 * @author jonsai
 */
@Entity
@Table(name = "userTreasure")
public class UserTreasure extends BaseModel<Long> implements Serializable{
	private static final long serialVersionUID = 8398955926215211626L;
	
	/** 用户id */
	@Id
	private long playerId;
	
	/** 宝藏用户道具id */
	private long userPropsId;
	
	/** 地图id */
	private int mapId;
	
	/** x坐标 */
	private int x;
	
	/** y坐标 */
	private int y;
	
	/** 奖励id */
	private int rewardId;
	
	/** 使用的铲子类型 */
	private int propsId;
	
	/** 藏宝图品质 */
	private int quality;
	
	/** 状态, 0-正常, 1-挖宝, */
	private int status;
	
	/** 打开的所有藏宝图, 格式：  userPropsId_mapId_x_y|...*/
	@Lob
	private String openedTreasure = "";

	/** 打开的箱子 */
	private String openBox;
	
	@Transient
	private Map<Long, Treasure> openedTreasureMap = null;
	
	@Transient
	private Set<Integer> openedBoxs = null;
	
	//-----------------------
	/**
	 * 构造
	 * @param userPropsId
	 * @return
	 */
	public static UserTreasure valueOf(long playerId){
		UserTreasure userTreasure = new UserTreasure();
		userTreasure.playerId = playerId;
		return userTreasure;
	}
	
	/**
	 * 取得开启的藏宝图
	 * @param userPropsId
	 * @return
	 */
	public Treasure getOpendTreasure(long userPropsId){
		Map<Long, Treasure> openedTreasureMap = getOpenedTreasureMap();
		synchronized (openedTreasureMap) {
			return openedTreasureMap.get(userPropsId);
		}
	}
	
	private Map<Long, Treasure> getOpenedTreasureMap() {
		if(openedTreasureMap == null){
			synchronized (this) {
				if(openedTreasureMap == null){
					openedTreasureMap = new HashMap<Long, Treasure>(3);
					List<String[]> list = Tools.delimiterString2Array(this.openedTreasure);
					if(list != null && list.size() > 0){
//						userPropsId_mapId_x_y|
						for(String[] values : list){
							Treasure treasure = Treasure.valueOf(Long.valueOf(values[0]), Integer.valueOf(values[1]), Integer.valueOf(values[2]), Integer.valueOf(values[3]));
							openedTreasureMap.put(treasure.getUserPropsId(), treasure);
						}
					}
				}
			}
		}
		return openedTreasureMap;
	}

	public Set<Integer> getOpenedBoxs() {
		if(openedBoxs == null){
			synchronized (this) {
				if(openedBoxs == null){
					openedBoxs = new HashSet<Integer>(1);
				}
				if(StringUtils.isNotBlank(this.openBox)){
					String[] npcIds = this.openBox.split(Splitable.ATTRIBUTE_SPLIT);
					if(npcIds != null){
						for(String npcId : npcIds){
							openedBoxs.add( Integer.valueOf(npcId) );
						}
					}
				}
			}
		}
		return openedBoxs;
	}
	
	public void flushOpenedBoxs(){
		StringBuilder sb = new StringBuilder();
		if(this.openedBoxs != null){
			for(Integer npcId : this.openedBoxs){
				sb.append(Splitable.ATTRIBUTE_SPLIT).append(npcId);
			}
			if(sb.length() > 0){
				sb.deleteCharAt(0);
			}
		}
		this.openBox = sb.toString();
	}

	/**
	 * 移除一个打开的藏宝图
	 * @param userPropsId
	 */
	public Treasure removeOpenedTreasure(long userPropsId){
		Map<Long, Treasure> openedTreasureMap = getOpenedTreasureMap();
		synchronized (openedTreasureMap) {
			Treasure remove = openedTreasureMap.remove(userPropsId);
			flushOpenedMap();
			return remove;
		}
	}
	
	/**
	 * 增加一个打开的藏宝图
	 * @param userPorpsId
	 * @param mapId
	 * @param x
	 * @param y
	 */
	public Treasure addOpendTreasure(long userPropsId, int mapId, int x, int y){
		Treasure treaure = Treasure.valueOf(userPropsId, mapId, x, y);
		Map<Long, Treasure> openedTreasureMap = getOpenedTreasureMap();
		synchronized (openedTreasureMap) {
			openedTreasureMap.put(userPropsId, treaure);
			flushOpenedMap();
		}
		return treaure;
	}
	
	private void flushOpenedMap(){
		if(openedTreasureMap != null){
			StringBuilder sb = new StringBuilder();
			for(Entry<Long, Treasure> entry : openedTreasureMap.entrySet()){
				Treasure treasure = entry.getValue();
				sb.append(Splitable.ELEMENT_DELIMITER).append(treasure.getUserPropsId()).append(Splitable.ATTRIBUTE_SPLIT)
				.append(treasure.getMapId()).append(Splitable.ATTRIBUTE_SPLIT)
				.append(treasure.getX()).append(Splitable.ATTRIBUTE_SPLIT).append(treasure.getY());
			}
			if(sb.length() > 0){
				sb.deleteCharAt(0);
			}
			this.openedTreasure = sb.toString();
		}
	}
	

	/**
	 * 重置
	 */
	public void reset(){
		this.userPropsId = 0;
		this.mapId = 0;
		this.x = 0;
		this.y = 0;
		this.propsId = 0;
		this.rewardId = 0;
		this.quality = 1;
		this.status = 0;
		this.openBox = null;
		if(this.openedBoxs != null){
			this.openedBoxs.clear();
		}
	}

	/**
	 * 是否为空
	 * @return
	 */
	public boolean isEnterTreasureMap(){
		return this.userPropsId == 0 || this.mapId == 0 || x == 0 || y == 0;
	}
	
	public String getOpenBox() {
		return openBox;
	}
	
	public void setOpenBox(String openBox) {
		this.openBox = openBox;
	}

	public String getOpenedTreasure() {
		return openedTreasure;
	}

	public void setOpenedTreasure(String openedTreasure) {
		this.openedTreasure = openedTreasure;
	}

	@Override
	public Long getId() {
		return this.playerId;
	}

	@Override
	public void setId(Long id) {
		this.playerId = id;
	}

	public long getUserPropsId() {
		return userPropsId;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public void setUserPropsId(long userPropsId) {
		this.userPropsId = userPropsId;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	/** 状态, 0-正常, 1-挖宝, 2-npcId */
	public int getStatus() {
		return status;
	}

	/** 状态, 0-正常, 1-挖宝, 2-领奖了 */
	public void setStatus(int status) {
		this.status = status;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
	
}
