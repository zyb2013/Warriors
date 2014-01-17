package com.yayo.warriors.basedb.model;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.monster.type.Classification;
import com.yayo.warriors.module.monster.type.MonsterType;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 怪物战斗属性
 * @author liuyuhua
 */
@Resource
public class MonsterFightConfig {
	/** 怪物的下标名*/
	public static final String MONSTER_LEVEL_IDX_NAME = "MONSTER_FIGHT_CONFIG_LEVEL";
	
	@Id
	private int id;
	
	//属性定义
	private int strength;
	private int dexerity;
	private int intellect;
	private int constitution;
	private int spirituality;
	private int hit;
	private int dodge;
	private int move_speed;
	private int theurgy_attack;
	private int theurgy_defense;
	private int theurgy_critical;
	/** 物理攻击 */
	private int physical_attack;

	/** 物理防御 */
	private int physical_defense;

	/** 物理暴击 */
	private int physical_critical;
	private int pierce;
	private int block;
	private int rapidly;
	private int ductility;
	private int hp;
	private int mp;
	private int gas;
	private int hp_max;
	private int mp_max;
	private int gas_max;
	
	/** 定身效果值*/
	private int immobilizeDefense;

	/** 怪物等级*/
	@Index(name=MONSTER_LEVEL_IDX_NAME, order=0)
	private int level ;
	
	/** 掉落  {掉落ID_掉落ID_....}*/
	private String drop;
	
	/** 任务掉落. { 道具ID_掉落概率|....}*/
	private String taskDrops;
	
	/** 技能属性  {技能ID_技能等级|技能ID_技能等级}*/
	private String skills;
	
	/** 基础技能*/
	private int baseSkill;
	
	/** 基础技能等级*/
	private int baseSkillLevel;
	
	/** 施法方式*/
	private int fightCaseing;
	
	/** 怪物经验*/
	private int exp ;
	
	/** 类型ID 怪物是什么类型 */
	private int baseId;

	/** 怪物名字 */
	private String name;

	/** 怪物皮肤ID */
	private int model;
	
	/** 追击范围(单位:格子 A*格 */
	private int pursueRange;

	/** 巡逻范围(单位:格子 A*格 */
	private int patrolRange = 4;

	/** 警戒范围(单位:格子 A*格 */
	private int warnRange;
	
	/** 散步延时(毫秒) */
	private int walkDelay;
	
	/** 复活后活动延时 */
	private int actionDelay;

	/** Ai的等级 */
	private int ailevel;
	
	/** 头像icon */
	private int icon;
	
	/** 复活时间(单位:秒) */
	private int reviveTime;
	
	/** 怪物类型 : {@link MonsterType} */
	@Index(name=MONSTER_LEVEL_IDX_NAME, order=1)
	private int monsterType ;
	
	/** Boss额外配置    暂时只有复活时间   格式：1_4_5_16   复活时间1, 4, 5, 16点*/
	private String bossConfigs ;
	
	/** 怪物分类 1 普通怪 2 副本怪*/
	@Index(name=MONSTER_LEVEL_IDX_NAME, order=2)
	private int classification = Classification.NONE;
	
	/** 攻击时间间隔单位: 毫秒*/
	private long attackInterval ;
	
	/** 怪物阵营 {@link Camp} */
	private int monsterCamp;
	
	/** 是否可以经验附加 */
	private boolean expAddition = true;
	
	/** 抗捉取 */
	private boolean garbbingDefense = false;
	
	/** 抗击飞 */
	private boolean knockFlyDefense = false;

	/** 抗击退 */
	private boolean knockBackDefense = false;
	
	/** 掉落编号集合 */
	@JsonIgnore
	private Map<Integer, Integer> dropMap = null;
	
	/** 任务掉落集合. { 任务ID, 概率 } */
	@JsonIgnore
	private Map<Integer, Integer> taskDropMap = null;
	
	/** 技能信息集合 { 技能ID, 技能等级 }*/
	@JsonIgnore
	private Map<Integer, Integer> skillInfoMap = null;
	
	//Getter and Setter
	public int getId() {
		return id;
	}

	public long getAttackInterval() {
		return attackInterval;
	}

	public void setAttackInterval(long attackInterval) {
		this.attackInterval = attackInterval;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getDexerity() {
		return dexerity;
	}

	public void setDexerity(int dexerity) {
		this.dexerity = dexerity;
	}

	public int getIntellect() {
		return intellect;
	}

	public void setIntellect(int intellect) {
		this.intellect = intellect;
	}

	public int getConstitution() {
		return constitution;
	}

	public void setConstitution(int constitution) {
		this.constitution = constitution;
	}

	public int getSpirituality() {
		return spirituality;
	}

	public void setSpirituality(int spirituality) {
		this.spirituality = spirituality;
	}

	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
	}

	public int getDodge() {
		return dodge;
	}

	public void setDodge(int dodge) {
		this.dodge = dodge;
	}

	public int getMove_speed() {
		return move_speed;
	}

	public int getImmobilizeDefense() {
		return immobilizeDefense;
	}

	public void setImmobilizeDefense(int immobilizeDefense) {
		this.immobilizeDefense = immobilizeDefense;
	}

	public boolean isExpAddition() {
		return expAddition;
	}

	public void setExpAddition(boolean expAddition) {
		this.expAddition = expAddition;
	}

	/**
	 * 获得怪物的技能信息. (K-技能ID, V-技能等级)
	 * 
	 * @return {@link Map}
	 */
	public Map<Integer, Integer> getSkillInfoMap() {
		if(this.skillInfoMap != null) {
			return skillInfoMap;
		}
		synchronized (this) {
			if(this.skillInfoMap != null) {
				return skillInfoMap;
			}
			
			this.skillInfoMap = new HashMap<Integer, Integer>(1);
			List<String[]> array = Tools.delimiterString2Array(this.getSkills());
			if(array == null || array.isEmpty()) {
				return this.skillInfoMap;
			}
			
			for (String[] element : array) {
				if(element != null && element.length >= 2) {
					this.skillInfoMap.put(Integer.parseInt(element[0]), Integer.parseInt(element[1]));
				}
			}
		}
		return this.skillInfoMap;
	}
	
	public double getMoveSpeedServerData() {
		return Tools.divideAndRoundDown(this.getMove_speed(), AttributeKeys.RATE_BASE, 3);
	}
	
	public void setMove_speed(int move_speed) {
		this.move_speed = move_speed;
	}

	public int getTheurgy_attack() {
		return theurgy_attack;
	}

	public void setTheurgy_attack(int theurgy_attack) {
		this.theurgy_attack = theurgy_attack;
	}

	public int getTheurgy_defense() {
		return theurgy_defense;
	}

	public void setTheurgy_defense(int theurgy_defense) {
		this.theurgy_defense = theurgy_defense;
	}

	public int getTheurgy_critical() {
		return theurgy_critical;
	}

	public void setTheurgy_critical(int theurgy_critical) {
		this.theurgy_critical = theurgy_critical;
	}

	public int getPierce() {
		return pierce;
	}

	public void setPierce(int pierce) {
		this.pierce = pierce;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public int getRapidly() {
		return rapidly;
	}

	public void setRapidly(int rapidly) {
		this.rapidly = rapidly;
	}

	public int getDuctility() {
		return ductility;
	}

	public void setDuctility(int ductility) {
		this.ductility = ductility;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getMp() {
		return mp;
	}

	public void setMp(int mp) {
		this.mp = mp;
	}

	public int getGas() {
		return gas;
	}

	public void setGas(int gas) {
		this.gas = gas;
	}

	public int getHp_max() {
		return hp_max;
	}

	public void setHp_max(int hp_max) {
		this.hp_max = hp_max;
	}

	public int getMp_max() {
		return mp_max;
	}

	public void setMp_max(int mp_max) {
		this.mp_max = mp_max;
	}

	public int getGas_max() {
		return gas_max;
	}

	public void setGas_max(int gas_max) {
		this.gas_max = gas_max;
	}

	public void setDrop(String drop) {
		this.drop = drop;
	}

	public int getBaseSkill() {
		return baseSkill;
	}

	public void setBaseSkill(int baseSkill) {
		this.baseSkill = baseSkill;
	}

	public int getBaseSkillLevel() {
		return baseSkillLevel;
	}

	public void setBaseSkillLevel(int baseSkillLevel) {
		this.baseSkillLevel = baseSkillLevel;
	}

	public int getFightCaseing() {
		return fightCaseing;
	}

	public void setFightCaseing(int fightCaseing) {
		this.fightCaseing = fightCaseing;
	}
	
	public String getTaskDrops() {
		return taskDrops;
	}

	public void setTaskDrops(String taskDrops) {
		this.taskDrops = taskDrops;
	}

	public String getDrop() {
		return drop;
	}

	
	public Map<Integer, Integer> getTaskDropMap() {
		if(this.taskDropMap != null) {
			return taskDropMap;
		}
		
		synchronized (this) {
			if(this.taskDropMap != null) {
				return this.taskDropMap;
			}
			
			this.taskDropMap = new HashMap<Integer, Integer>(1);
			List<String[]> arrays = Tools.delimiterString2Array(this.taskDrops);
			if(arrays == null || arrays.isEmpty()) {
				return this.taskDropMap;
			}
			
			for (String[] element : arrays) {
				Integer taskId = Integer.valueOf(element[0]);
				Integer random = Integer.valueOf(element[1]);
				this.taskDropMap.put(taskId, random);
			}
		}
		return this.taskDropMap;
	}
	
	
	public Map<Integer, Integer> getDropMap() {
		if(this.dropMap != null) {
			return dropMap;
		}
		
		synchronized (this) {
			if(this.dropMap != null) {
				return this.dropMap;
			}
			
			this.dropMap = new HashMap<Integer, Integer>(1);
			if(StringUtils.isBlank(this.drop)) {
				return this.dropMap;
			}
			
			String[] arrays = this.drop.split(Splitable.ELEMENT_SPLIT);
			for (String element : arrays) {
				Integer dropId = Integer.valueOf(element.trim());
				Integer cacheCount = this.dropMap.get(dropId);
				cacheCount = cacheCount == null ? 0 : cacheCount;
				this.dropMap.put(dropId, cacheCount + 1);
			}
		}
		return this.dropMap;
	}
	
	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	@Override
	public String toString() {
		return "MonsterFightConfig [id=" + id + ", strength=" + strength
				+ ", dexerity=" + dexerity + ", intellect=" + intellect
				+ ", constitution=" + constitution + ", spirituality="
				+ spirituality + ", hit=" + hit + ", dodge=" + dodge
				+ ", move_speed=" + move_speed + ", theurgy_attack="
				+ theurgy_attack + ", theurgy_defense=" + theurgy_defense
				+ ", theurgy_critical=" + theurgy_critical + ", pierce="
				+ pierce + ", block=" + block + ", rapidly=" + rapidly
				+ ", ductility=" + ductility + ", hp=" + hp + ", mp=" + mp
				+ ", gas=" + gas + ", hp_max=" + hp_max + ", mp_max=" + mp_max
				+ ", gas_max=" + gas_max + ", drop=" + drop + ", skills="
				+ skills + ", baseSkill=" + baseSkill + ", baseSkillLevel="
				+ baseSkillLevel + "]";
	}

	public int getPhysical_attack() {
		return physical_attack;
	}

	public void setPhysical_attack(int physical_attack) {
		this.physical_attack = physical_attack;
	}

	public int getPhysical_defense() {
		return physical_defense;
	}

	public void setPhysical_defense(int physical_defense) {
		this.physical_defense = physical_defense;
	}

	public int getPhysical_critical() {
		return physical_critical;
	}

	public void setPhysical_critical(int physical_critical) {
		this.physical_critical = physical_critical;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public int getPursueRange() {
		return pursueRange;
	}

	public void setPursueRange(int pursueRange) {
//		if(this.id / 1000 != 16){
//			this.pursueRange = isBoss() ? pursueRange : Math.min(pursueRange, 19);
//		} else {
			this.pursueRange  = pursueRange;
//		}
	}

	public int getPatrolRange() {
		return patrolRange;
	}

	public void setPatrolRange(int patrolRange) {
		this.patrolRange = patrolRange;
	}

	public int getWarnRange() {
		return warnRange;
	}

	public void setWarnRange(int warnRange) {
		this.warnRange = warnRange;
	}

	public int getAilevel() {
		return ailevel;
	}

	public void setAilevel(int ailevel) {
		this.ailevel = ailevel;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	/**
	 * 获取复活时间秒数
	 * @return
	 */
	public int getReviveTime() {
		if(reviveTime > 0){
			return reviveTime;
		}
		
		if(StringUtils.isNotBlank(bossConfigs)){
			String [] bossConfigArrays = bossConfigs.split(Splitable.ATTRIBUTE_SPLIT);
			int [] refreshTimes = convertToIntArray(bossConfigArrays);
			Arrays.sort(refreshTimes);
			int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			for(int refreshHour : refreshTimes){
				if(refreshHour - currentHour > 0){
					calendar.set(Calendar.HOUR_OF_DAY,refreshHour);
					return (int)((calendar.getTime().getTime() - System.currentTimeMillis())/1000);
				}
			}
			
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, refreshTimes[0]);
			return (int)((calendar.getTime().getTime() - System.currentTimeMillis())/1000);
			
		}
		return reviveTime;
	}
	
	/**
	 * 是否可以复活
	 * @return
	 */
	public boolean canRevive(){
		return this.reviveTime > -1 ;
	}
	
	private int[] convertToIntArray(String[] refreshTimes){
		int [] refreshTimesSwap = new int[refreshTimes.length] ;
		for(int i = 0 ; i < refreshTimes.length ; i++){
			refreshTimesSwap[i] = Integer.parseInt(refreshTimes[i]);
		}
		return refreshTimesSwap ;
	}

	public void setReviveTime(int reviveTime) {
		this.reviveTime = reviveTime;
	}

	public String getBossConfigs() {
		return bossConfigs;
	}

	public void setBossConfigs(String bossConfigs) {
		this.bossConfigs = bossConfigs;
	}
	
	public int getMonsterType() {
		return monsterType;
	}

	public void setMonsterType(int monsterType) {
		this.monsterType = monsterType;
	}
	
	public int getClassification() {
		return classification;
	}

	public void setClassification(int classification) {
		this.classification = classification;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean isBoss(){
		return this.monsterType == MonsterType.BOSS.getValue() ;
	}

	public int getWalkDelay() {
		if(this.walkDelay <= 0){
			this.walkDelay = 2000;
		}
		return walkDelay;
	}

	public void setWalkDelay(int walkDelay) {
		this.walkDelay = walkDelay;
	}

	public int getActionDelay() {
		return actionDelay;
	}

	public void setActionDelay(int actionDelay) {
		if(this.actionDelay <= 0){
			this.actionDelay = 1000;
		}
		this.actionDelay = actionDelay;
	}

	public int getMonsterCamp() {
		return monsterCamp;
	}

	public void setMonsterCamp(int monsterCamp) {
		this.monsterCamp = monsterCamp;
	}

	public boolean isKnockFlyDefense() {
		return knockFlyDefense;
	}

	public void setKnockFlyDefense(boolean knockFlyDefense) {
		this.knockFlyDefense = knockFlyDefense;
	}

	public boolean isKnockBackDefense() {
		return knockBackDefense;
	}

	public void setKnockBackDefense(boolean knockBackDefense) {
		this.knockBackDefense = knockBackDefense;
	}

	public boolean isGarbbingDefense() {
		return garbbingDefense;
	}

	public void setGarbbingDefense(boolean garbbingDefense) {
		this.garbbingDefense = garbbingDefense;
	}

}
