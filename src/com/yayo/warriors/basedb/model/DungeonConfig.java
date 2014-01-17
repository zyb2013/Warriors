package com.yayo.warriors.basedb.model;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.types.DifficultyType;
import com.yayo.warriors.module.dungeon.types.DungeonType;
import com.yayo.warriors.module.user.model.Fightable;

/**
 * 副本配置
 * @author liuyuhua
 */
@Resource
public class DungeonConfig {
	
	/** */
	@Id
	private int id;
	
	/** 下一链副本ID (没有的时候为0,用于验证千程塔的类型副本)*/
	private int chainId;
	
	/** 副本的玩法类型*/
	private int type;
	
	/** 难度  {@link DifficultyType}*/
	private int difficulty;
	
	/** 副本地图ID*/
	private int mapId;
	
	/** 副本名称*/
	private String name;
	
	/** 等级限制*/
	private int levelLimit;
	
	/** 一天,进入次数限制*/
	private int enterNum;
	
	/** 副本可以存活的时间,(单位:秒)*/
	private int dungeonLiveDate;
	
	/** 进入副本最小人数*/
	private int minNumLimit;
	
	/** 进入副本最大人数*/
	private int maxNumLimit;
	
	/** 每一波的间隔时间(单位:秒)*/
	private int nextRoundSec;
	
	/** 总共波(回合)数*/
	private int totleRoundCount;
	
	/** 副本中断后或者玩家强制退出副本,玩家从新上线后所回到的位置
	 *  格式(地图ID_X坐标_Y坐标)
	 *  即:当且仅当玩家进入副本后,当玩家关闭IE,或者断网后,
	 *     过若干个小时候回到游戏中,副本{@link Dungeon}被回收后
	 *     强制玩家去到的目的地图坐标*/
	private String interrupt;
	
	/** 进入副本的初始化坐标 格式(地图ID_X坐标_Y坐标)*/
	private String enter;
	
	/** 进入副本需要接的任务(格式:{taskId_taskId_taskId_taskId})*/
	private String enterTask;
	
	/** 每回合开始之前需要接受的任务 (格式:{回合数_任务ID_任务ID|回合数_任务ID_任务ID})*/
	private String roundTask;
	
	/** 时间间隔波数,从副本开始创建开始计算,对应{@link DungeonType#ROUND_INTIME}
	 *  (格式:{秒数_波数|秒数_波数|秒数_波数})
	 * */
	private String roundIntime;
	
	/** 剧情副本属性奖励*/
	private String attributeReap;
	
	/** 剧情副本装备或道具奖励{类型_道具_数量|类型_道具_数量}*/
	private String itemReap;
	
	/** 高富帅副本 入口{XY坐标_副本基础ID|XY坐标_副本基础ID}*/
	private String highRich;
	
	/** 开启时间区间 格式: 16:10~18:30|19:10~20:55*/
	private String openRegionTime = "";
	
	/** 开启周期区间 格式:1(星期日)|2(星期一)|3(星期二)|7(星期六)*/
	private String openRegionDay = "";
	
	/** 解析{@link DungeonConfig#interrupt}中断后地图坐标系*/
	@JsonIgnore
	private transient DungeonPoint dungeonPoint;
	
	/** 解析{@link DungeonConfig#enter}进入地图坐标系*/
	@JsonIgnore
	private transient DungeonPoint enterPoint;
	
	/** 解析{@link DungeonConfig#enterTask}进入副本任务ID*/
	@JsonIgnore
	private transient Collection<Integer> enterTaskList = null;
	
	/** 解析{@link DungeonConfig#roundTask}每回合任务ID*/
	@JsonIgnore
	private transient Map<Integer,Collection<Integer>> roundTasks = null;
	
	/** 解析{@link DungeonConfig#roundIntime}字段 格式:{时间,回合数}*/
	@JsonIgnore
	private transient Map<Integer,Integer> roundIntimes = null;
	
	/** 解析{@link DungeonConfig#attributeReap}*/
	@JsonIgnore
	private transient Fightable fightable = null;
	
	/** 解析{@link DungeonConfig#highRich}*/
	@JsonIgnore
	private transient Map<String,Integer> highRichs = null;
	
	/** 解析{@link DungeonConfig#itemReap}*/
	@JsonIgnore
	private transient List<DungeonProps> itemReaps = null; 
	
	/** 解析{@link DungeonConfig#openRegionDay}*/
	@JsonIgnore
	private transient List<Integer> regionDays = null;
	
	/**
	 * 是否开放
	 * @return true 可以进入 false 不可以进入
	 */
	public boolean isOpen(){
		if(openRegionTime == null || openRegionTime.isEmpty()){//当且仅当开启时间没有的时候将直接返回ture
			return true;
		}
		
		Calendar calendar = Calendar.getInstance();
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY); 
		int currentMinute = calendar.get(Calendar.MINUTE); 
		int currentWeek = calendar.get(Calendar.DAY_OF_WEEK);
		
		if(!this.getRegionDays().contains(currentWeek)){ //不存在开发的周期
			return false;
		}
		
		String[] times = openRegionTime.split(Splitable.ELEMENT_SPLIT);
		boolean falg = false;
		for(String time : times){
			String[] split = time.split("-");
			String beginTime = split[0];
			String endTime = split[1];
			String[] splitBeginTime = beginTime.split(Splitable.DELIMITER_ARGS); 
			String[] splitEndTime = endTime.split(Splitable.DELIMITER_ARGS);
			int beginHour = Integer.parseInt(splitBeginTime[0]);
			int beginMinute = Integer.parseInt(splitBeginTime[1]);
			int endHour = Integer.parseInt(splitEndTime[0]);
			int endMinute = Integer.parseInt(splitEndTime[1]);
			
			if(currentHour == beginHour){ //判断是否到达开始的时间
				if(currentMinute < beginMinute){
					falg = false;
				}
			}
			
			if(currentHour == endHour){ //判断是否达到结束的时间
				if(currentMinute > endMinute){
					falg = false;
				}
			}
			
			if(currentHour >= beginHour && currentHour <= endHour){
				falg = true;
			}
			
			if(falg == true){
				return falg;
			}
		}
		
		return false;
	}
	
	/**
	 * 获取开放的具体时间
	 * @return {@link List} 集合
	 */
	private List<Integer> getRegionDays(){
		if(regionDays != null){
			return regionDays;
		}
		
		synchronized (this) {
			if(regionDays != null){
				return regionDays;
			}
			
			regionDays = new ArrayList<Integer>(7);
			if(openRegionDay != null && !openRegionDay.isEmpty()){
				String[] tmpDays = openRegionDay.split(Splitable.ELEMENT_SPLIT);
				for(String day : tmpDays){
					regionDays.add(Integer.parseInt(day));
				}
			}
			
			return regionDays;
		}
	}
	
	/**
	 * 剧情副本奖励
	 * @return {@link List} 道具奖励集合
	 */
	public List<DungeonProps> getItemReaps(){
		if(itemReaps != null){
			return itemReaps;
		}
		
		synchronized (this) {
			if(itemReaps != null){
				return itemReaps;
			}
			
			itemReaps = new ArrayList<DungeonProps>();
			
			if(itemReap == null || itemReap.isEmpty()){
				return itemReaps;
			}
			
			List<String[]> delimiter = Tools.delimiterString2Array(itemReap);
			
			for(String[] props : delimiter){
				if(props.length < 3){
					continue;
				}
				
				int propsId   =  Integer.parseInt(props[1]);
				int goodsType =  Integer.parseInt(props[0]);
				int number    =  Integer.parseInt(props[2]);
				
				itemReaps.add(DungeonProps.valueOf(propsId,goodsType,number));
			}
			
			return itemReaps;
		}
	}
	
	/**
	 * 获取高富帅副本基础ID
	 * @param x   玩家的X轴坐标
	 * @param y   玩家的Y轴坐标
	 * @return {@link Integer}  副本基础ID
	 */
	public Integer getHighRichs(int x,int y){
		String key = new StringBuilder().append(x).append(y).toString();
		if(highRichs != null){
			return this.highRichs.get(key);
		}
		
		synchronized (this) {
			if(highRichs != null){
				return this.highRichs.get(key);
			}
			
			this.highRichs = new HashMap<String, Integer>(3);
			if(!StringUtils.isBlank(highRich)){
				List<String[]> datas = Tools.delimiterString2Array(highRich);
				for(String[] data : datas){
					if(data.length < 2){
						continue;
					}
					this.highRichs.put(data[0], Integer.parseInt(data[1]));
				}
			}
			return this.highRichs.get(key);
		}
	}
	
	
	/**
	 * 获取奖励属性
	 * @return {@link Fightable} 属性对象
	 */
	public Fightable getRewardFightable(){
		if(fightable != null){
			return fightable;
		}
		
		synchronized (this) {
			if(fightable != null){
				return fightable;
			}
			
			fightable = new Fightable();
			if(attributeReap == null){
				return fightable;
			}
			
			List<String[]> attributes = Tools.delimiterString2Array(attributeReap);
			for(String[] attribute : attributes){
				if(attribute.length < 2){
					continue;
				}
				int attKey = Integer.parseInt(attribute[0]);
				int attValue = Integer.parseInt(attribute[1]);
				fightable.add(attKey, attValue);
			}
			return fightable;
		}
		
	}
	
	
	/**
	 * 相隔多少时间开始调用的回合数
	 * @return
	 */
	public Map<Integer,Integer> getRoundIntimes(){
		if(this.roundIntimes != null){
			return this.roundIntimes;
		}
		
		synchronized (this) {
			if(this.roundIntimes != null){
				return this.roundIntimes;
			}
			
			this.roundIntimes = new HashMap<Integer,Integer>(1);
			if(this.roundIntime == null){
				return this.roundIntimes;
			}
			
			List<String[]> list = Tools.delimiterString2Array(this.roundIntime);
			if(list != null){
				for(String[] tmp : list){
					if(tmp != null && tmp.length >= 2){
						int intime = Integer.parseInt(tmp[0]);
						int round = Integer.parseInt(tmp[1]);
						this.roundIntimes.put(intime, round);
					}
				}
			}
			
			return this.roundIntimes;
		}
	}
	
	
	
	
	/**
	 * 获取每回合开始前需要接受的任务
	 * @return {@link Map} 任务集合
	 */
	public Map<Integer,Collection<Integer>> getRoundTasks(){
		if(this.roundTasks != null){
			return this.roundTasks;
		}
		
		synchronized (this) {
			if(this.roundTasks != null){
				return this.roundTasks;
			}
			
			this.roundTasks = new HashMap<Integer, Collection<Integer>>(1);
			if(this.roundTask == null){
				return this.roundTasks;
			}
			
			String[] split_tasks = this.roundTask.split(Splitable.ELEMENT_SPLIT);
			for(String tasks : split_tasks){
				String[] tmp = tasks.split(Splitable.ATTRIBUTE_SPLIT);
				if(tmp.length < 2){
					continue;
				}
				
				int round = Integer.parseInt(tmp[0]);
				Collection<Integer> tasklist = this.roundTasks.get(round);
				if(tasklist == null){
					tasklist = new ArrayList<Integer>();
					this.roundTasks.put(round, tasklist);
					tasklist = this.roundTasks.get(round);
				}
				
				for(int i = 0 ; i < tmp.length ; i++){
					if(i == 0){
						//第一位是回合数,所以不需要处理
						continue;
					}
					int taskId = Integer.parseInt(tmp[i]);
					tasklist.add(taskId);
				}
			}
			
			return this.roundTasks;
		}
	}
	
	/**
	 * 获取进入副本后,需要接受的任务
	 * @return {@link Collection<Integer>} 任务集合
	 */
	public Collection<Integer> getEnterTaskList() {
		if(this.enterTaskList != null){
			return this.enterTaskList;
		}
		
		synchronized (this) {
			if(this.enterTaskList != null){
				return this.enterTaskList;
			}
			
			this.enterTaskList = new ArrayList<Integer>();
			if(this.enterTask == null){
				return this.enterTaskList;
			}
			
			String[] taskIds = this.enterTask.split(Splitable.ATTRIBUTE_SPLIT);
			for(String taskId : taskIds){
				this.enterTaskList.add(Integer.parseInt(taskId));
			}
			
			return enterTaskList;
		}
	}
	
	/**
	 * 副本中断后或者玩家强制退出副本,玩家从新上线后所回到的位置
	 * 解析:
	 * <per>{@link DungeonConfig#interrupt}</per>
	 * @return
	 */
	public DungeonPoint getInterruptPoint(){
		if(this.dungeonPoint != null){
			return this.dungeonPoint;
		}
		
		synchronized (this) {
			if(this.dungeonPoint != null){
				return this.dungeonPoint;
			}
			
			if(this.interrupt == null){
				this.dungeonPoint = new DungeonPoint();
				return this.dungeonPoint;
			}
			
			String[] point = this.interrupt.split(Splitable.ATTRIBUTE_SPLIT);
			this.dungeonPoint = new DungeonPoint();
			if(point.length >= 3){
				int mapId = Integer.parseInt(point[0]);
				int x = Integer.parseInt(point[1]);
				int y = Integer.parseInt(point[2]);
				this.dungeonPoint.setMapId(mapId);
				this.dungeonPoint.setX(x);
				this.dungeonPoint.setY(y);
			}
			
			return this.dungeonPoint;
		}
	}
	
	/**
	 * 进入副本,玩家的位置
	 * 解析:
	 * <per>{@link DungeonConfig#enter}</per>
	 * @return
	 */
	public DungeonPoint getEnterPoint(){
		if(this.enterPoint != null){
			return this.enterPoint;
		}
		
		synchronized (this) {
			if(this.enterPoint != null){
				return this.enterPoint;
			}
			
			if(this.enter == null){
				this.enterPoint = new DungeonPoint();
				return this.enterPoint;
			}
			
			String[] point = this.enter.split(Splitable.ATTRIBUTE_SPLIT);
			this.enterPoint = new DungeonPoint();
			if(point.length >= 3){
				int mapId = Integer.parseInt(point[0]);
				int x = Integer.parseInt(point[1]);
				int y = Integer.parseInt(point[2]);
				this.enterPoint.setMapId(mapId);
				this.enterPoint.setX(x);
				this.enterPoint.setY(y);
			}
			
			return this.enterPoint;
		}
	}
	
	/**
	 * 是否剧情副本
	 * @return true 剧情副本  false 反之
	 */
	public boolean isStoryDungeon(){
		return this.difficulty == DifficultyType.STORY;
	}
	
	/**
	 * 是否藏宝图类型
	 * @return true 藏宝图 false 反之
	 */
	public boolean isTreasure(){
		return this.type == DungeonType.TREASURE;
	}
	
	/**
	 * 是否存在下一个节点
	 * @return true 存在下一个节点;false不存在下一个节点
	 */
	public boolean hasChainId(){
		return this.chainId > 0;
	}
	
	//Getter and Setter...

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getChainId() {
		return chainId;
	}

	public void setChainId(int chainId) {
		this.chainId = chainId;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}


	public int getNextRoundSec() {
		return nextRoundSec;
	}

	public void setNextRoundSec(int nextRoundSec) {
		this.nextRoundSec = nextRoundSec;
	}

	public int getTotleRoundCount() {
		return totleRoundCount;
	}

	public void setTotleRoundCount(int totleRoundCount) {
		this.totleRoundCount = totleRoundCount;
	}


	public String getInterrupt() {
		return interrupt;
	}

	public void setInterrupt(String interrupt) {
		this.interrupt = interrupt;
	}

	public int getLevelLimit() {
		return levelLimit;
	}

	public void setLevelLimit(int levelLimit) {
		this.levelLimit = levelLimit;
	}

	public int getEnterNum() {
		return enterNum;
	}

	public void setEnterNum(int enterNum) {
		this.enterNum = enterNum;
	}

	public String getEnter() {
		return enter;
	}

	public void setEnter(String enter) {
		this.enter = enter;
	}

	public int getDungeonLiveDate() {
		return dungeonLiveDate;
	}

	public void setDungeonLiveDate(int dungeonLiveDate) {
		this.dungeonLiveDate = dungeonLiveDate;
	}

	public String getEnterTask() {
		return enterTask;
	}

	public void setEnterTask(String enterTask) {
		this.enterTask = enterTask;
	}

	public String getRoundTask() {
		return roundTask;
	}

	public void setRoundTask(String roundTask) {
		this.roundTask = roundTask;
	}

	public String getRoundIntime() {
		return roundIntime;
	}

	public void setRoundIntime(String roundIntime) {
		this.roundIntime = roundIntime;
	}

	public String getAttributeReap() {
		return attributeReap;
	}

	public void setAttributeReap(String attributeReap) {
		this.attributeReap = attributeReap;
	}

	public String getHighRich() {
		return highRich;
	}
	
	public void setHighRich(String highRich) {
		this.highRich = highRich;
	}

	public String getItemReap() {
		return itemReap;
	}


	public void setItemReap(String itemReap) {
		this.itemReap = itemReap;
	}

	public String getOpenRegionTime() {
		return openRegionTime;
	}

	public void setOpenRegionTime(String openRegionTime) {
		this.openRegionTime = openRegionTime;
	}

	public String getOpenRegionDay() {
		return openRegionDay;
	}

	public void setOpenRegionDay(String openRegionDay) {
		this.openRegionDay = openRegionDay;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinNumLimit() {
		return minNumLimit;
	}

	public void setMixNumLimit(int mixNumLimit) {
		this.minNumLimit = mixNumLimit;
	}

	public int getMaxNumLimit() {
		return maxNumLimit;
	}

	public void setMaxNumLimit(int maxNumLimit) {
		this.maxNumLimit = maxNumLimit;
	}

	@Override
	public String toString() {
		return "DungeonConfig [id=" + id + ", chainId=" + chainId + ", type="
				+ type + ", difficulty=" + difficulty + ", mapId=" + mapId
				+ ", name=" + name + ", levelLimit=" + levelLimit
				+ ", enterNum=" + enterNum + ", dungeonLiveDate="
				+ dungeonLiveDate + ", minNumLimit=" + minNumLimit
				+ ", maxNumLimit=" + maxNumLimit + ", nextRoundSec="
				+ nextRoundSec + ", totleRoundCount=" + totleRoundCount
				+ ", interrupt=" + interrupt + ", enter=" + enter
				+ ", enterTask=" + enterTask + ", roundTask=" + roundTask
				+ ", roundIntime=" + roundIntime + ", attributeReap="
				+ attributeReap + ", itemReap=" + itemReap + ", highRich="
				+ highRich + ", openRegionTime=" + openRegionTime
				+ ", openRegionDay=" + openRegionDay + "]";
	}
}
