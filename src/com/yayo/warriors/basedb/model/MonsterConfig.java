package com.yayo.warriors.basedb.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 怪物配置
 * 
 * @author liuyuhua
 */
@Resource
public class MonsterConfig implements IMonsterConfig {
	public static final String IDX_NAME = "MONSTER_FIGHT_ID";
	/** 怪物原型ID */
	@Id
	private int id;

	/** 怪物所在地图 */
	@Index(name = IndexName.MONSTER_MAPID)
	private int mapId;

	/** 怪物出生Y坐标 */
	private int bornX;

	/** 怪物出生Y坐标 */
	private int bornY;

	/** 副本id*/
	private int dungeon ;
	
	/** 怪物基本数据编号*/
	@Index(name=IDX_NAME, order=0)
	private int monsterFightId ;

	@JsonIgnore
	private MonsterFightConfig monsterFight;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getBornX() {
		return bornX;
	}

	public void setBornX(int bornX) {
		this.bornX = bornX;
	}

	public int getBornY() {
		return bornY;
	}

	public void setBornY(int bornY) {
		this.bornY = bornY;
	}

	public int getDungeon() {
		return dungeon;
	}

	public void setDungeon(int dungeon) {
		this.dungeon = dungeon;
	}

	public int getMonsterFightId() {
		return monsterFightId;
	}

	public void setMonsterFightId(int monsterFightId) {
		this.monsterFightId = monsterFightId;
	}

	public MonsterFightConfig getMonsterFight() {
		return monsterFight;
	}

	public void setMonsterFight(MonsterFightConfig monsterFight) {
		this.monsterFight = monsterFight;
	}

	@Override
	public String toString() {
		return "MonsterConfig [id=" + id + ", mapId=" + mapId + ", bornX=" + bornX + ", bornY="
				+ bornY + ", dungeon=" + dungeon + ", monsterFightId=" + monsterFightId
				+ ", monsterFight=" + monsterFight + "]";
	}
}
