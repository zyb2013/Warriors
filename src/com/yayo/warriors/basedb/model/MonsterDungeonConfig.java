package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.type.IndexName;

@Resource
public class MonsterDungeonConfig implements IMonsterConfig{
	
	@Id
	private int id;
	
	/** 怪物战斗属性id */
	private int monsterFightId;
	
	/** 副本id */
	@Index(name = IndexName.MONSTERDUNGEON_DUNGEONID_ROUND, order = 0)
	private int dungeonId;
	
	/** 第几轮 */
	@Index(name = IndexName.MONSTERDUNGEON_DUNGEONID_ROUND, order = 1)
	private int round;
	
	/** x坐标 */
	private int bornX;
	
	/** y坐标 */
	private int bornY;
	
	/** 几率*/
	private int rate;
	
	/**
	 * 该怪物是否要出现
	 * @return {@link Boolean} true 出现 falsh 反之
	 */
	public boolean ranRate(){
		if(rate == 100){
			return true;
		}else{
			int rand = Tools.getRandomInteger(100);
			if(rand <= rate){
				return true;
			}else{
				return false; 
			}
		}
	}
	
	
	//Getter and Setter....
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMonsterFightId() {
		return monsterFightId;
	}

	public void setMonsterFightId(int monsterFightId) {
		this.monsterFightId = monsterFightId;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(int dungeonId) {
		this.dungeonId = dungeonId;
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

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		MonsterDungeonConfig other = (MonsterDungeonConfig) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MonsterDungeonConfig [id=" + id + ", monsterFightId="
				+ monsterFightId + ", round=" + round + ", dungeonId="
				+ dungeonId + ", bornX=" + bornX + ", bornY=" + bornY + "]";
	}

}
