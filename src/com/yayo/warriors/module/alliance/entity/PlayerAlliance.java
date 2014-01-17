package com.yayo.warriors.module.alliance.entity;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.alliance.types.Title;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;

@Entity
@Table(name = "userAlliance")
public class PlayerAlliance extends BaseModel<Long>{
	private static final long serialVersionUID = -6679597285916611131L;
	
	@Id
	@Column(name = "playerId")
	private long id;
	
	/** 帮派的ID*/
	private long allianceId;
	
	/** 当前贡献值(可消费值)*/
	private int donate;
	
	/** 历史贡献值*/
	private int hisdonate;
	
	/** 加入帮派的时间*/
	private long jiontime;
	
	/** 离开帮派的时间*/
	private long leavetime;
	
	/** 职位*/
	@Enumerated
	private Title title;
	
	/** 帮派名字*/
	private String allianceName = "";
	
	/*** 帮派技能*/
	private String skills = "";
	
	/** 玩法刷新时间(占卦(抽奖)、捐献银币、捐献道具)单位:毫秒*/
	private long refreshTime = 0;
	/** 每日最大捐献令牌数量*/
	private int donatePropsCount = 0;
	/** 每日最大捐献铜币数量*/
	private int donateSilverCount = 0;
	/** 每日占卦(次数) 在占卦的时间单位里*/
	private int divineCount = 0;
	
    /** 帮派技能,解析:{@link Alliance#skills},格式:{技能ID_技能等级}*/
    @Transient
    private transient Map<Integer,Integer> skillMap = null; 
	
	/** 用于判断是否今天时间 {@link PlayerAlliance#} 时间*/
	@Transient
	private transient Date today = null;
	
	@Transient
	private transient volatile int flushable = Flushable.FLUSHABLE_NORMAL;
	
	/** 帮派技能计算出来的属性*/
	@Transient
	private transient Fightable attributes = new Fightable();

	/**
	 * 是否可以加入帮派
	 * <per>离开帮派12小时候之内无法加入别的帮派</per>
	 * @return {@link Boolean} true 可以 false 不可以
	 */
	public boolean canJoinAlliance(){
		long currentTime = System.currentTimeMillis();
		if((currentTime - this.leavetime) >= 43200000){
			return true;
		}
		return false;
	}
	
	/**
	 * 设置属性值
	 * @param attributeKey {@link AttributeKeys}属性键值
	 * @param value 属性值
	 */
	public void setAttribute(int attributeKey,int value){
		this.attributes.put(attributeKey, value);
	}
	
	/**
	 * 获取属性
	 * @param attributeKey {@link AttributeKeys}属性键值
	 * @return {@link Integer} 属性值
	 */
	public int getAttribute(int attributeKey) {
		return this.attributes.getAttribute(attributeKey);
	}
	
	
	/**
	 * 是否要重算属性
	 * @return true 需要刷新 , false 不需刷新
	 */
	public boolean isFlushable(){
		return this.flushable != Flushable.FLUSHABLE_NOT;
	}
	
    /**
     * 学习技能
     * @param skillId   技能的ID
     * @param level     技能等级
     */ 
    public void studySkill(int skillId,int level){
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
     * 刷新玩法时间
     * @return true 有刷新 false 无刷新
     */
    public boolean refreshTime(){
    	if(today != null){
			if(DateUtil.isToday(today)){
				return false;
			}
    	}
    	
    	synchronized (this) {
        	if(today != null){
    			if(DateUtil.isToday(today)){
    				return false;
    			}
        	}
    		
        	refreshTime = System.currentTimeMillis();
        	today = new Date(refreshTime);
        	donatePropsCount = 0;
        	donateSilverCount = 0;
        	divineCount = 0;
        	return true;
		}
    }

	
	/**
	 * 加入帮派
	 * @param allianceId     帮派的ID
	 * @param allianceName   帮派的名字
	 * @param title          职位
	 */
	public void joinAlliance(long allianceId,String allianceName,Title title){
		this.allianceId = allianceId;
		this.allianceName = allianceName;
		this.title = title;
		this.jiontime = System.currentTimeMillis();
	}
	
	/**
	 * 离开帮派
	 */
	public void leaveAlliance(){
		this.donate = 0;
		this.hisdonate = 0;
		this.allianceName = "";
		this.title = Title.NOMAL;
		this.allianceId = 0;
		this.skills = "";
		this.skillMap = null;
		this.divineCount = 0;
		this.donatePropsCount = 0;
		this.donateSilverCount = 0;
		this.refreshTime = 0;
		this.flushable = Flushable.FLUSHABLE_NORMAL;
		this.attributes.clear();
		this.leavetime = System.currentTimeMillis();
	}
	
    /**
     * 增加贡献值
     * @param donate   贡献值
     */
	public void increaseDonate(int donate) {
		this.donate += donate;
	}
	
    /**
     * 增加今日铜币捐献数
     * @param silver   捐献的铜币
     */
	public void increaseSilverDaily(int silver) {
		this.donateSilverCount += silver;
	}
	
	/**
	 * 增加今日令牌捐献数  
	 * @param propsCount  道具数量
	 */
	public void increasePropsDaily(int propsCount){
		this.donatePropsCount += propsCount;
	}
	
	/**
	 * 减少贡献值
	 * @param donate   贡献值
	 */
	public void decreaseDonate(long donate) {
		this.donate -= donate;
		this.donate = this.donate < 0 ? 0 : this.donate;
	}
	
	/**
	 * 增加历史贡献值
	 * @param donate   贡献值
	 */
	public void increaseHisDonate(int donate){
		this.hisdonate += donate;
	}
	
	/**
	 * 是否存在帮派
	 * @return  true 存在 false 不存在
	 */
	public boolean isExistAlliance(){
		return this.allianceId > 0;
	}
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getAllianceId() {
		return allianceId;
	}

	public void setAllianceId(long allianceId) {
		this.allianceId = allianceId;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}

	public int getHisdonate() {
		return hisdonate;
	}

	public void setHisdonate(int hisdonate) {
		this.hisdonate = hisdonate;
	}

	public long getJiontime() {
		return jiontime;
	}

	public void setJiontime(long jiontime) {
		this.jiontime = jiontime;
	}

	public String getAllianceName() {
		return allianceName;
	}

	public void setAllianceName(String allianceName) {
		this.allianceName = allianceName;
	}

	public Title getTitle() {
		return title;
	}

	public void setTitle(Title title) {
		this.title = title;
	}

	public int getDivineCount() {
		return divineCount;
	}

	public void setDivineCount(int divineCount) {
		this.divineCount = divineCount;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public int getFlushable() {
		return flushable;
	}

	public void setFlushable(int flushable) {
		this.flushable = flushable;
	}
	
	public Fightable getAttributes() {
		return attributes;
	}

	public void setAttributes(Fightable attributes) {
		this.attributes = attributes;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	public int getDonatePropsCount() {
		return donatePropsCount;
	}

	public void setDonatePropsCount(int donatePropsCount) {
		this.donatePropsCount = donatePropsCount;
	}

	public int getDonateSilverCount() {
		return donateSilverCount;
	}

	public void setDonateSilverCount(int donateSilverCount) {
		this.donateSilverCount = donateSilverCount;
	}

	public long getLeavetime() {
		return leavetime;
	}

	public void setLeavetime(long leavetime) {
		this.leavetime = leavetime;
	}

	@Override
	public String toString() {
		return "PlayerAlliance [id=" + id + ", allianceId=" + allianceId
				+ ", donate=" + donate + ", hisdonate=" + hisdonate
				+ ", jiontime=" + jiontime + ", leavetime=" + leavetime
				+ ", title=" + title + ", allianceName=" + allianceName
				+ ", skills=" + skills + ", refreshTime=" + refreshTime
				+ ", donatePropsCount=" + donatePropsCount
				+ ", donateSilverCount=" + donateSilverCount + ", divineCount="
				+ divineCount + "]";
	}
}
