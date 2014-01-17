package com.yayo.warriors.module.dungeon.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.module.dungeon.rule.DungeonRule;
import com.yayo.warriors.module.dungeon.types.DungeonState;
import com.yayo.warriors.module.dungeon.types.DungeonType;
import com.yayo.warriors.socket.ResponseKey;

/**
 * 副本模型
 * @author liuyuhua
 */
public class Dungeon {
	
	/** 副本自增长ID*/
	private long dungeonId;
	/** 创建该副本时,玩家所在的分线*/
	private int branching;
	/** 副本的创建时间(单位:秒)*/
	private long createDate;
	
	/** 进入副本的玩家ID*/
	private List<Long> entrant = new ArrayList<Long>();
	/** 已领取奖励的玩家ID*/
	private List<Long> rewardIds = new ArrayList<Long>();
	/** 离开副本的玩家ID*/
	private List<Long> leaveIds = new ArrayList<Long>();
	
	/** 怪物回合数对应的怪物 {当前回合数,怪物ID}*/
	private Map<Integer,List<Long>> round4Monsters = new HashMap<Integer, List<Long>>(5);
	/** {怪物ID,对应回合数} 用于标记怪对应于拿一波怪物*/
	private Map<Long,Integer> monsters4Round = new HashMap<Long, Integer>(5);
	/**{回合数字,经验值} - 每一回合击杀怪物后获得的经验*/
	private Map<Integer,Integer> round4Exp = new HashMap<Integer, Integer>(5);
	/**{回合数,本回合总共有多少只怪}*/
	private Map<Integer,Integer> round4MaxMonster = new HashMap<Integer,Integer>(5);
	
	/** 本次执行的时间*/
	private long actionDate;
	/** 当前回合数  用于{@link DungeonType#TOWER_DEFENSE} {@link DungeonType#ROUND} 两种类型
	 *  注意 {@link Dungeon#roundCount}从 1 开始*/
	private int roundCount;
	/** 下一回合开始时间 单位:秒
	 *  {@link Dungeon#roundCount}}下一回合启动的时间间隔})*/
	private long nextRoundDate;
	/** 副本状态 {@link DungeonType#TOWER_DEFENSE} {@link DungeonType#ROUND} 
	 *  两种副本会有{@link DungeonState#FAIL}}(失败)状态*/
	private DungeonState state;
	/** 副本完成时间，对应{@link DungeonState#SUCCESS} 状态的时间*/
	private long completeDate;
	/** 副本基础配置*/
	private DungeonConfig dungeonConfig = null;
	
	/**
	 * 构造方法
	 * @param branching    所在分线
	 * @param dungeonId    副本的自增长ID
	 * @param config       副本配置
	 * @return
	 */
	public static Dungeon valueOf(int branching,long dungeonId,DungeonConfig config){
		Dungeon dungeon = new Dungeon();
		dungeon.roundCount = DungeonRule.DEFAUL_DUNGEON_ROUND;
		dungeon.branching  = branching;
		dungeon.dungeonId  = dungeonId;
		dungeon.state      = DungeonState.PROGRESS;
		dungeon.createDate = DateUtil.getCurrentSecond();
		dungeon.dungeonConfig = config;
		return dungeon;
	}
	
	/**
	 * 构造方法
	 * @param branching     所在分线
	 * @param config        副本配置
	 * @return {@link Dungeon} 副本对象
	 */
	public static Dungeon valueOf(int branching,DungeonConfig config, long tempId){
		Dungeon dungeon = new Dungeon();
		dungeon.roundCount = DungeonRule.DEFAUL_DUNGEON_ROUND;
		dungeon.branching  = branching;
		dungeon.dungeonId  = tempId;
		dungeon.state      = DungeonState.PROGRESS;
		dungeon.createDate = DateUtil.getCurrentSecond();
		dungeon.dungeonConfig = config;
		return dungeon;
	}
	
	/**
	 * 获取进入副本玩家人数
	 * @return
	 */
	public int getEnterPlayerSize(){
		return this.entrant.size();
	}
	
	/**
	 * 添加进入副本的玩家
	 * @param playerId  玩家的ID
	 */
	public void addEntrant(long playerId){
		if(!this.entrant.contains(playerId)){
			this.entrant.add(playerId);
		}
	}
	
	/**
	 * 添加已经领取了奖励的玩家
	 * @param playerId  玩家的ID
	 */
	public void addReward(long playerId){
		if(!this.rewardIds.contains(playerId)){
			this.rewardIds.add(playerId);
		}
	}
	
	/**
	 * 添加已经离开了副本的玩家
	 * @param playerId   玩家的ID
	 */
	public void addLeave(long playerId){
		if(!this.leaveIds.contains(playerId)){
			this.leaveIds.add(playerId);
		}
	}
	
	/**
	 * 添加用于计算的怪物到副本中
	 * @param round           当前回合数
	 * @param monsterIds      怪物集合
	 */
	public synchronized Map<String,Object> addDungeonMonster(int round,List<Long> monsterIds){
		if(monsterIds != null && !monsterIds.isEmpty()){
			for(long monsterId : monsterIds){
				monsters4Round.put(monsterId, round);
			}
			round4Monsters.put(round, monsterIds);
			round4MaxMonster.put(round, monsterIds.size());
			
			Map<String,Object> result = new HashMap<String, Object>(4);
			result.put(ResponseKey.ROUND, round);
			result.put(ResponseKey.NUMBER, monsterIds.size());
			result.put(ResponseKey.EXP, 0);
			result.put(ResponseKey.TOTAL, monsterIds.size());
			return result;
		}
		
		return null;
	}
	
	/**
	 * 删除怪物
	 * <per>该方法都是单一线程调用的,所以只需要增加 synchronized</per>
	 * @param monsterId     怪物的ID
	 * @result {@link Map<String,Object>} 返回需要发送给客户端的信息
	 */
	public synchronized Map<String,Object> removeMonsters(long monsterId,int exp){
		Map<String,Object> result = new HashMap<String, Object>(4);
		if(!monsters4Round.containsKey(monsterId)){
			return result;
		}
		int round = monsters4Round.remove(monsterId);  //获取怪物所在的回合数
		List<Long> monsters = round4Monsters.get(round);//获取回合所对应的怪物
		if(monsters != null && !monsters.isEmpty()){
			monsters.remove(monsterId);
			
			//计算本回合累计获得的经验值
		    int totleExp = 0;
		    if(this.round4Exp.get(round) == null){
				this.round4Exp.put(round, exp);
				totleExp += exp;
		    }else{
			   totleExp = this.round4Exp.get(round);
			   totleExp += exp;
			   this.round4Exp.put(round, totleExp);
		    }
			
		    int totle = round4MaxMonster.get(round); //本回合总共有多少只怪
		    result.put(ResponseKey.ROUND, round);
		    result.put(ResponseKey.NUMBER, monsters.size());
		    result.put(ResponseKey.EXP, totleExp);
		    result.put(ResponseKey.TOTAL, totle);
		}
		return result;
	}
	
	/**
	 * 副本中的怪物是否已经被清空(击杀)
	 * @return {@link Boolean} true 怪物已经清空 false 反之
	 */
	public boolean isEmpty4Monster(){
		return monsters4Round.isEmpty();
	}
	
	/**
	 * 获取副本中当前怪物数量
	 * @return {@link Integer} 当前副本怪物数量
	 */
	public int size4Monster(){
		return monsters4Round.size();
	}
	
	/**
	 * 副本是否完成
	 * @return true 完成 false 没有完成
	 */
	public boolean isComplete(){
		return this.state == DungeonState.SUCCESS;
	}
	
	/**
	 * 过滤没有离开副本的玩家ID
	 * @param enterPlayers      进入副本的玩家ID
	 * @param leavePlayers      离开副本的玩家ID
	 * @return {@link Collection<Long>} 返回没有离开副本的玩家ID
	 */
    public Collection<Long> filterPlayers(){
    	Collection<Long> result = new ArrayList<Long>();
    	if(this.entrant != null && leaveIds != null){
        	for(long playerId : entrant){
        		if(!leaveIds.contains(playerId)){
        			result.add(playerId);
        		}
        	}
    	}
    	return result;
    }
    
	
	//Getter and Setter...

	public long getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(long dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getBaseId() {
		return dungeonConfig.getId();
	}
	
	public int getBranching() {
		return branching;
	}

	public void setBranching(int branching) {
		this.branching = branching;
	}

	public int getMapId() {
		return dungeonConfig.getMapId();
	}

	public int getType() {
		return dungeonConfig.getType();
	}

	public long getActionDate() {
		return actionDate;
	}

	public void setActionDate(long actionDate) {
		this.actionDate = actionDate;
	}

	public List<Long> getEntrant() {
		return entrant;
	}

	public void setEntrant(List<Long> playerIds) {
		this.entrant = playerIds;
	}

	public List<Long> getRewardIds() {
		return rewardIds;
	}

	public void setRewardIds(List<Long> rewardIds) {
		this.rewardIds = rewardIds;
	}

	public int getRoundCount() {
		return roundCount;
	}

	public void setRoundCount(int roundCount) {
		this.roundCount = roundCount;
	}

	public long getNextRoundDate() {
		return nextRoundDate;
	}

	public void setNextRoundDate(long nextRoundDate) {
		this.nextRoundDate = nextRoundDate;
	}

	public DungeonState getState() {
		return state;
	}

	public void setState(DungeonState state) {
		this.state = state;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public List<Long> getLeaveIds() {
		return leaveIds;
	}

	public void setLeaveIds(List<Long> leaveIds) {
		this.leaveIds = leaveIds;
	}

	public long getCompleteDate() {
		return completeDate;
	}

	public void setCompleteDate(long completeDate) {
		this.completeDate = completeDate;
	}

	public DungeonConfig getDungeonConfig() {
		return dungeonConfig;
	}

	public void setDungeonConfig(DungeonConfig dungeonConfig) {
		this.dungeonConfig = dungeonConfig;
	}

	public Map<Integer, List<Long>> getRound4Monsters() {
		return round4Monsters;
	}

	public void setRound4Monsters(Map<Integer, List<Long>> round4Monsters) {
		this.round4Monsters = round4Monsters;
	}

	public Map<Long, Integer> getMonsters4Round() {
		return monsters4Round;
	}

	public void setMonsters4Round(Map<Long, Integer> monsters4Round) {
		this.monsters4Round = monsters4Round;
	}

	public Map<Integer, Integer> getRound4Exp() {
		return round4Exp;
	}

	public void setRound4Exp(Map<Integer, Integer> round4Exp) {
		this.round4Exp = round4Exp;
	}

	public Map<Integer, Integer> getRound4MaxMonster() {
		return round4MaxMonster;
	}

	public void setRound4MaxMonster(Map<Integer, Integer> round4MaxMonster) {
		this.round4MaxMonster = round4MaxMonster;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (dungeonId ^ (dungeonId >>> 32));
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
		Dungeon other = (Dungeon) obj;
		if (dungeonId != other.dungeonId)
			return false;
		return true;
	}

}
