package com.yayo.warriors.basedb.model;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 任务怪物配置列表
 * 
 * @author Hyint
 */
@Resource
public class TaskMonsterConfig {
	
	/**
	 * 怪物战斗属性表的ID.
	 * 
	 * {@link MonsterFightConfig#getId()} 
	 */
	@Id
	private int id;
	
	/** 怪物列表 */
	@JsonIgnore
	private transient List<MonsterConfig> monsters = new LinkedList<MonsterConfig>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<MonsterConfig> getMonsters() {
		return monsters;
	}

	public MonsterConfig getRandomMonster() {
		return monsters != null && !monsters.isEmpty() ? monsters.get(Tools.getRandomInteger(monsters.size())) : null;
	}

	@Override
	public String toString() {
		return "TaskMonsterConfig [id=" + id + "]";
	}
}
