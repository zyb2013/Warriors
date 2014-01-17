package com.yayo.warriors.module.alliance.entity;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.alliance.model.DonateRecord;
import com.yayo.warriors.module.alliance.model.Record;
import com.yayo.warriors.module.alliance.types.AllianceState;
import com.yayo.warriors.module.alliance.types.VilidaState;

@Entity
@Table(name = "alliance")
public class Alliance extends BaseModel<Long>{
	private static final long serialVersionUID = -25125784933195236L;

	@Id
	@Column(name = "allianceId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	/** 帮会名字*/
	private String name = "";
	
	/** 创建者(帮主)*/
	private long playerId;
	
	/** 状态*/
	private int state = AllianceState.ACTIVE;
	
	/** 阵营*/
	private int camp;
	
    /** 公告*/
    private String notice = "";
    
    /** 等级*/
    private int level;
    
    /** 威望值*/
    private int prestige;
    
    /** 资金*/
    private long silver;
    
    /** 令牌(玩家捐献给帮派的)*/
    private int tokenPropsCount;
    
    /** 护法人数*/
    private int prolawNum;
    
    /** 长老人数*/
    private int elderNum;
    
    /** 副帮主*/
    private int deputymasterNum;
    
    /** 设置验证状态*/
    private int vilidaState = VilidaState.NORMAL;
    
    /** 帮主的名字*/
    private String masterName;
    
	/** 藏经阁(技能)*/
    private int booksLevel;
    
	/** 帮派商店 */
	private int shopLevel;
	
	/** 祭台*/
	private int daisLevel;
	
	/** 演舞台*/
	private int arenaLevel;
	
	/***
	 * 帮派研究获得的技能
	 * {技能ID_等级|技能ID_等级|技能ID_等级}
	 */
	private String skills = "";
	
	
	/** 帮派等级升级记录(配合运营活动) 格式:{等级_时间|等级_时间}*/
	@Lob
	private String levelupRecord;
	
	/** 冲级活动(主要记录帮主是否有领取过)
	 * <per>格式:活动ID_要求等级|活动ID_要求等级</per>
	 * */
	@Lob
	private String levelActive = "";
	
    /** 记录*/
    @Transient
    private transient List<Record> recordLog = new ArrayList<Record>();
    
    /** 帮派技能,解析:{@link Alliance#skills},格式:{技能ID_技能等级}*/
    @Transient
    private transient Map<Integer,Integer> skillMap = null; 

    /** 今天的捐献值记录*/
    @Transient
    private transient Date today = null;
    /** 今天是否有人捐献过*/
    @Transient
    private transient boolean haveDonate = false;
    /** 今日贡献值记录排行*/
    @Transient
    private transient List<DonateRecord> todayDonateLog = new ArrayList<DonateRecord>();
    
    /** 升级记录*/
    @Transient
    private transient Map<Integer,Long> levelupRecordMap = null;
    
	/** 冲级活动 解析{@link Alliance#levelActive}*/
	@Transient
	private transient List<String> levelActiveList = null;
	
	/**
	 * 获取等级活动集合
	 * @return {@link List} 等级活动集合
	 */
	public List<String> getLevelActives(){
		if(levelActiveList != null){
			return levelActiveList;
		}
		
		synchronized (this) {
			if(levelActiveList != null){
				return levelActiveList;
			}
			
			levelActiveList = new ArrayList<String>();
			if(levelActive == null || levelActive.isEmpty()){
				return levelActiveList;
			}
			
			String[] splits = levelActive.split(Splitable.ELEMENT_SPLIT);
			for(String split : splits){
				levelActiveList.add(split);
			}
			
			return levelActiveList;
		}
	}
	
	/**
	 * 添加已领取的等级活动
	 * @param levelId     等级活动ID 格式:{活动ID_要求等级}
	 */
	public void addLevelActives(String levelId){
		List<String> levelList = this.getLevelActives();
		levelList.add(levelId);
		
		//格式化...
		StringBuilder builder = new StringBuilder();
		for(String level : levelList){
			builder.append(level).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		
		this.levelActive = builder.toString();
	}
	
    
    /**
     * 添加帮派升级记录
     * @param level   等级
     */
    public void addLevelupRecords(int level){
    	Map<Integer, Long> levelupRecords = this.getLevelupRecords();
    	levelupRecords.put(level, System.currentTimeMillis());
    	
    	StringBuilder builder = new StringBuilder();
    	for(Entry<Integer, Long> entry : levelupRecords.entrySet()){
    		int key = entry.getKey();
    		long value = entry.getValue();
    		builder.append(key).append(Splitable.ATTRIBUTE_SPLIT)
    		       .append(value).append(Splitable.ELEMENT_DELIMITER);
    	}
    	
    	if(builder.length() > 0) {
    		builder.deleteCharAt(builder.length() - 1);
    	}
    	
    	levelupRecord = builder.toString();
    }
    
    
    /**
     * 获取升级记录
     * @return {@link Map}
     */
    public Map<Integer, Long> getLevelupRecords(){
    	if(levelupRecordMap != null){
    		return levelupRecordMap;
    	}
    	
    	synchronized (this) {
        	if(levelupRecordMap != null){
        		return levelupRecordMap;
        	}
        	
        	levelupRecordMap = new HashMap<Integer, Long>(2);
        	if(levelupRecord == null || levelupRecord.isEmpty()){
        		return levelupRecordMap;
        	}
        	
        	String[] levelRecordSplit = levelupRecord.split(Splitable.ELEMENT_SPLIT);
        	for(String strRecord : levelRecordSplit){
        		String[] tmpRecord = strRecord.split(Splitable.ATTRIBUTE_SPLIT);
        		if(tmpRecord.length >= 2){
        			int level = Integer.parseInt(tmpRecord[0]);
        			long time = Long.parseLong(tmpRecord[1]);
        			levelupRecordMap.put(level, time);
        		}
        	}
        	
        	return levelupRecordMap;
		}
    }

	/**
     * 是否有捐献
     * @return true 有捐献 false 没有捐献
     * */
    public boolean isHaveDonate() {
		return haveDonate;
	}
    
	/**
	 * 设置捐献状态 
	 * @param haveDonate true 有捐献过
	 */
    public void setHaveDonate(boolean haveDonate) {
		this.haveDonate = haveDonate;
	}

    /**
     * 获取今日捐献值记录
     * @param playerId              玩家的ID
     * @return {@link DonateRecord} 捐献值得记录
     */
    public DonateRecord getDonateRecord4PlayerId(long playerId){
    	List<DonateRecord> donateList = this.getDonateRecoreds();
    	if(donateList == null || donateList.isEmpty()){
    		return null;
    	}
    	for(DonateRecord record : donateList){
    		if(record.getPlayerId() == playerId){
    			return record;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * 排序今日捐献排行
     */
    public void sortDonateRecored(){
    	if(todayDonateLog != null && !todayDonateLog.isEmpty()){
    		Collections.sort(todayDonateLog);
    	}
    }

	/**
     * 获取今日贡献值记录排行
     * @return {@link List} 捐献值排行
     */
    public List<DonateRecord> getDonateRecoreds(){
    	if(today != null){
    		if(!DateUtil.isToday(today)){
    			synchronized (this) {
    				if(DateUtil.isToday(today)){
    					return todayDonateLog;
    				}
        			today = new Date();
        			todayDonateLog.clear();
        			return todayDonateLog;
				}
    		}
    		
    		return todayDonateLog;
    		
    	}else{
    		synchronized (this) {
				if(today != null){
					return todayDonateLog;
				}
    			
				today = new Date();
				todayDonateLog.clear();
				return todayDonateLog;
			}
    	}
    	
    }

    
    /**
     * 研究技能
     * @param skillId   技能的ID
     * @param level     技能等级
     */ 
    public void researchSkill(int skillId,int level){
    	Map<Integer,Integer> skillMap = this.getSkillMap();
    	skillMap.put(skillId, level);//直接设置
    	this.skills = this.build2Skills();//序列化
    }
    
    
    /**
     * 序列化构建帮派技能
     * @return {@link String} 序列化后的帮派技能 
     */
    private String build2Skills(){
    	if(skillMap == null || skillMap.isEmpty()){
    		return "";
    	}
    	
    	StringBuilder builder = new StringBuilder();
    	for(Entry<Integer, Integer> entry : skillMap.entrySet()){
    		int skillId = entry.getKey();
    		int skillLevel = entry.getValue();
    		builder.append(skillId).append(Splitable.ATTRIBUTE_SPLIT)
    		       .append(skillLevel).append(Splitable.ELEMENT_DELIMITER);
    	}
    	
    	if(builder.length() > 0) {
    		builder.deleteCharAt(builder.length() - 1);
    	}
    	
    	return builder.toString();
    }
    
    
    /**
     * 获取帮派技能
     * @return {@link Map} 帮派技能集合
     */
    public Map<Integer,Integer> getSkillMap(){
    	if(skillMap != null){
    		return skillMap;
    	}
    	
    	synchronized (this) {
        	if(skillMap != null){
        		return skillMap;
        	}
        	
        	skillMap = new HashMap<Integer, Integer>(3);
        	if(skills == null || skills.isEmpty()){
        		return skillMap;
        	}
        	
        	List<String[]> tmpSkills = Tools.delimiterString2Array(skills);
    		for(String[] skill : tmpSkills){
    			int skillId = Integer.valueOf(skill[0]);
    			int skillLevel = Integer.valueOf(skill[1]);
    			skillMap.put(skillId, skillLevel);
    		}
    	
        	return skillMap;
		}
    }
    
    
    /**
     * 增加货币
     * @param silver   游戏货币
     */
	public void increaseSilver(long silver) {
		this.silver += silver;
	}
	
	/**
	 * 减少货币
	 * @param silver   游戏货币
	 */
	public void decreaseSilver(long silver) {
		this.silver -= silver;
		this.silver = this.silver < 0 ? 0 : this.silver;
	}
	
	/**
	 * 减少令牌
	 * @param silver   游戏货币
	 */
	public void decreaseTokenProps(int tokenProps) {
		this.tokenPropsCount -= tokenProps;
		this.tokenPropsCount = this.tokenPropsCount < 0 ? 0 : this.tokenPropsCount;
	}
    
    /**
     * 是否是解散状态
     * @return true 解散状态 false不是解散状态
     */
    public boolean isDrop(){
    	return this.state == AllianceState.DROP;
    }
    
    /**
     * 解散帮派
     * @return true 解散帮派成功
     */
    public boolean disband(){
    	this.state = AllianceState.DROP;
    	return this.isDrop();
    }
    
    /**
     * 增加副帮主人数
     * @param number   数量
     */
	public void increaseDeputymaster(int number) {
		this.deputymasterNum += number;
	}
	
	/**
	 * 减少副帮主人数
	 * @param number   数量 
	 */
	public void decreaseDeputymaster(int number) {
		this.deputymasterNum -= number;
		this.deputymasterNum = this.deputymasterNum < 0 ? 0 : this.deputymasterNum;
	}
	
    /**
     * 增加长老人数
     * @param number   数量
     */
	public void increaseElderNum(int number) {
		this.elderNum += number;
	}
	
	/**
	 * 减少护法人数
	 * @param number   数量 
	 */
	public void decreaseElderNum(int number) {
		this.elderNum -= number;
		this.elderNum = this.elderNum < 0 ? 0 : this.elderNum;
	}
	
    /**
     * 增加护法人数
     * @param number   数量
     */
	public void increaseProlawNum(int number) {
		this.prolawNum += number;
	}
	
	/**
	 * 减少副长老人数
	 * @param number   数量 
	 */
	public void decreaseProlawNum(int number) {
		this.prolawNum -= number;
		this.prolawNum = this.prolawNum < 0 ? 0 : this.prolawNum;
	}
	
	/**
	 * 增加帮派事件记录
	 * @param log  记录
	 */
	public synchronized void addRecordLog(Record log){
		if(this.recordLog.size() >= 50){
			recordLog.remove(0);
		}
		this.recordLog.add(log);
	}
	
	/**
	 * 获取帮派事件记录
	 * @return {@link List} 捐献集合
	 */
	public List<Record> getRecordLog() {
		return recordLog;
	}
 
	///Getter and Setter....

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getPrestige() {
		return prestige;
	}

	public void setPrestige(int prestige) {
		this.prestige = prestige;
	}

	public long getSilver() {
		return silver;
	}

	public void setSilver(long silver) {
		this.silver = silver;
	}

	public int getTokenPropsCount() {
		return tokenPropsCount;
	}

	public void setTokenPropsCount(int tokenPropsCount) {
		this.tokenPropsCount = tokenPropsCount;
	}

	public int getProlawNum() {
		return prolawNum;
	}

	public void setProlawNum(int prolawNum) {
		this.prolawNum = prolawNum;
	}

	public int getElderNum() {
		return elderNum;
	}

	public void setElderNum(int elderNum) {
		this.elderNum = elderNum;
	}

	public int getDeputymasterNum() {
		return deputymasterNum;
	}

	public void setDeputymasterNum(int deputymasterNum) {
		this.deputymasterNum = deputymasterNum;
	}

	public int getVilidaState() {
		return vilidaState;
	}

	public void setVilidaState(int vilidaState) {
		this.vilidaState = vilidaState;
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public int getBooksLevel() {
		return booksLevel;
	}

	public void setBooksLevel(int booksLevel) {
		this.booksLevel = booksLevel;
	}

	public int getShopLevel() {
		return shopLevel;
	}

	public void setShopLevel(int shopLevel) {
		this.shopLevel = shopLevel;
	}

	public int getDaisLevel() {
		return daisLevel;
	}

	public void setDaisLevel(int daisLevel) {
		this.daisLevel = daisLevel;
	}

	public int getArenaLevel() {
		return arenaLevel;
	}

	public void setArenaLevel(int arenaLevel) {
		this.arenaLevel = arenaLevel;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLevelupRecord() {
		return levelupRecord;
	}

	public void setLevelupRecord(String levelupRecord) {
		this.levelupRecord = levelupRecord;
	}

	public String getLevelActive() {
		return levelActive;
	}

	public void setLevelActive(String levelActive) {
		this.levelActive = levelActive;
	}

}
