package com.yayo.warriors.module.monster.model;

import java.util.concurrent.atomic.AtomicLong;

import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.MonsterFightConfig;

/**
 * 怪物模型
 * @author liuyuhua
 */
public class Monster {
	
	/**创建怪物时候的自增ID*/
	private Long id;
	
	/** 怪物所在的线 */
	private int branching; 
	
	/** 怪物副本ID*/
	private long dungeonId;
	
	/** 怪物创建时间**/
	private long createDate;
	
	/** 怪物战斗基础数据对象 */
	private MonsterFightConfig monsterFightConfig ;
	
	/** 怪物自增长Id*/
	private static final AtomicLong AUTO_MONSTER_ID = new AtomicLong(10000L);
	
	public static synchronized long getAutoId() {
		return AUTO_MONSTER_ID.getAndIncrement();
	}
	
	/**
	 * 构造函数
	 * @param autoId    	系统赋值的自增长ID
	 * @param branching  	线ID
	 * @param config    	配置
	 * @return
	 */
	public static Monster valueOf(int branching, MonsterFightConfig monsterFightConfig){
		Monster monster = new Monster();
		monster.id       = getAutoId();
		monster.branching = branching;
		monster.monsterFightConfig = monsterFightConfig ;
		monster.createDate = DateUtil.getCurrentSecond();
		return monster;
	}
	
	/**
	 * 构造函数
	 * @param autoId       系统赋值的自增长ID
	 * @param branching    线ID
	 * @param dungeonId    副本ID
	 * @param config       配置
	 * @return
	 */
	public static Monster valueOf(int branching,long dungeonId, MonsterFightConfig monsterFightConfig){
		Monster monster = new Monster();
		monster.id       = getAutoId();
		monster.branching = branching;
		monster.dungeonId = dungeonId;
		monster.monsterFightConfig = monsterFightConfig ;
		monster.createDate = DateUtil.getCurrentSecond();
		return monster;
	}

	/**
	 * 是否副本怪
	 * @return true 副本怪,false不是副本怪
	 */
	public boolean isDungeon(){
		return dungeonId != 0;
	}
	
    //Setter and Getter...
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getBaseId() {
		return monsterFightConfig.getBaseId();
	}

	public String getName() {
		return monsterFightConfig.getName();
	}

	public Integer getModel() {
		return monsterFightConfig.getModel();
	}

	public int getBranching() {
		return branching;
	}

	public void setBranching(int branching) {
		this.branching = branching;
	}

	public int getIcon() {
		return monsterFightConfig.getIcon();
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public long getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(long dungeonId) {
		this.dungeonId = dungeonId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Monster other = (Monster) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public MonsterFightConfig getMonsterFightConfig() {
		return this.monsterFightConfig;
	}
	
}
