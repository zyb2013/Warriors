package com.yayo.warriors.module.drop.vo;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 够将掉落奖励VO对象
 * 
 * @author Hyint
 */
public class DropRewardVO implements Serializable {
	private static final long serialVersionUID = -5931863305481534606L;

	/**
	 * 奖励的自增ID
	 */
	private long id;
	
	/**
	 * 掉落点的X坐标
	 */
	private int x;
	
	/**
	 * 掉落点的Y坐标
	 */
	private int y;
	
	/** 
	 * 掉落的物品类型
	 * <pre>
	 * -1:不存在
	 *  0:道具
	 *  1:装备
	 *  2:游戏币
	 *  3:游戏币
	 *  4:元宝
	 *  5:礼券
	 * </pre>
	 */
	private int type;
	
	/**
	 * 装备/道具: 掉落的物品ID.
	 * 货币类型  : 掉落货币类型.(0. 银两, 1. 礼券,2. 元宝)
	 */
	private int baseId;
	
	/**
	 * 掉落的数量
	 */
	private int amount;
	
	/**
	 * 消失结束时间. 单位:毫秒
	 */
	private long endTime;
	
	/** 所在的地图ID. 是地图的ID */
	private int mapId;
	
	/**
	 * 可以拾取该物品的角色ID列表
	 */
	private Long[] sharePlayers;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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


	public Long[] getSharePlayers() {
		return sharePlayers;
	}

	public void setSharePlayers(Long[] sharePlayers) {
		this.sharePlayers = sharePlayers;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}
 

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	@Override
	public String toString() {
		return "DropRewardVO [id=" + id + ", mapId=" + mapId + ", x=" + x + ", y=" + y
				+ ", type=" + type + ", baseId=" + baseId + ", amount=" + amount + ", endTime="
				+ endTime + ", sharePlayers=" + Arrays.toString(sharePlayers) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DropRewardVO other = (DropRewardVO) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}
