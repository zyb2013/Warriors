package com.yayo.warriors.module.dungeon.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.dungeon.model.DungeonInfo;
import com.yayo.warriors.module.dungeon.model.StoryInfo;
import com.yayo.warriors.module.dungeon.types.StoryState;

/**
 * 角色副本
 * @author liuyuhua
 */
@Entity
@Table(name="userDungeon")
public class PlayerDungeon extends BaseModel<Long>{
	private static final long serialVersionUID = 5069914690114722156L;

	@Id
	@Column(name="playerId")
	private Long id;
	
	/** (普通)副本的记录 (格式:基础副本ID_次数_进入时间)*/
	@Lob
	private String data = "";
	
	/** 剧情副本记录(格式:基础副本ID_完成状态({@link StoryState}))*/
	@Lob
	private String story = "";
	
	/** (普通)副本的总进入次数记录 (格式:基础副本ID_次数_进入时间)*/
	@Lob
	private String hisData = "";
	
	/** 当前所在副本的增量ID*/
	private long dungeonId;
	
	/** 当前所在副本的原型ID*/
	private int dungeonBaseId;
	
	/** 创建该类获取出来的时间(单位:毫秒)*/
	private transient volatile long flushableTime = 0;
	
	/** 本次进入副本的时间*/
	@Transient
	private transient volatile long enterDate;
	
	/** 记录副本的时间,通过{@link PlayerDungeon#data} 字段来转换*/
	@Transient
	private transient volatile HashMap<Integer,DungeonInfo> dungeonRecord = null;
	
	/** 记录副本的时间,通过{@link PlayerDungeon#hisData} 字段来转换*/
	@Transient
	private transient volatile HashMap<Integer,DungeonInfo> dungeonHisRecord = null;
	
	/** 记录剧情副本的状态,通过{@ link PlayerDungeon#story} 字段来转换*/
	@Transient
	private transient volatile HashMap<Integer,StoryInfo> storyRecord = null;
	
	/** 
	 * 第一次进入副本的时间
	 * <per>在千层塔这种类型的副本里面,容易导致玩家在00:00之前几分钟进入副本,
	 *      从而扰乱副本进入时间,所有副本进入的时间都以这个变量作为标记而
	 *      {@link PlayerDungeon#enterDate}时间将是主要用于返回客户端进行倒数的时间
	 * </per>
	 * */
	@Transient
	private transient volatile long layerDate = 0;
	
	/** 副本进入次数,刷新时间*/
	@Transient
	private transient volatile Date flushTime = null;
	
	
	/**
	 * 构造方法
	 * @param playerId   玩家的ID
	 * @return {@link PlayerDungeon} 副本对象
	 */
	public static PlayerDungeon valueOf(long playerId){
		PlayerDungeon playerDungeon = new PlayerDungeon();
		playerDungeon.id = playerId;
		return playerDungeon;
	}
	
	/**
	 * 增历史进入记录
	 * @param dungeonBaseId 副本基础ID
	 */
	private void addDungeonHisRecord(int dungeonBaseId){
		Map<Integer,DungeonInfo> hisMap = this.getDungeonHisRecord();
		if(hisMap == null){
			return;
		}
		
		DungeonInfo info = hisMap.get(dungeonBaseId);
		if(info == null){
			info = DungeonInfo.valueOf(dungeonBaseId, 1 , System.currentTimeMillis());
		}else{
			info.addHisTimes();
		}
		
		hisMap.put(dungeonBaseId, info);
		StringBuffer buffer = new StringBuffer();
		for(Entry<Integer,DungeonInfo> entry : this.getDungeonHisRecord().entrySet()){
			DungeonInfo tmp = entry.getValue();
			buffer.append(tmp.toString() + Splitable.ELEMENT_DELIMITER);
		}
		
		this.hisData = buffer.toString();
	}
	
	/**
	 * 获得历史进入记录
	 * @return {@link Map} 历史进入记录集合
	 */
	public Map<Integer,DungeonInfo> getDungeonHisRecord() {
		if(dungeonHisRecord != null){
			return dungeonHisRecord;
		}
		
		synchronized (this) {
			if(dungeonHisRecord != null){
				return dungeonHisRecord;
			}
			
			dungeonHisRecord = new HashMap<Integer, DungeonInfo>(2);
			if(this.hisData == null || this.hisData.isEmpty()){
				return dungeonHisRecord;
			}
			
			List<String[]> records = Tools.delimiterString2Array(this.hisData);
			if(records == null || records.isEmpty()){
				return dungeonHisRecord;
			}
			
			for(String[] record : records){
				if(record.length >= 3){
					int dungeonId = Integer.parseInt(record[0]);
					int times     = Integer.parseInt(record[1]);
					long date     = Long.parseLong(record[2]);
					dungeonHisRecord.put(dungeonId ,  DungeonInfo.valueOf(dungeonId,times,date));
				}
			}
			
			return dungeonHisRecord;
		}
	}
	
	/**
	 * 是否副本状态
	 * @return true 副本状态  false 非副本状态
	 */
	public boolean isDungeonStatus(){
		return this.dungeonId > 0;
	}
	
	/**
	 * 进入副本
	 * @param dungeonId      副本的增量ID
	 * @param dungeonBaseId  副本的基础ID
	 */
	public void enterDungeon(long dungeonId,int dungeonBaseId){
		this.dungeonId = dungeonId;
		this.dungeonBaseId = dungeonBaseId;
		this.enterDate = System.currentTimeMillis();
		if(this.layerDate == 0){
			this.layerDate = enterDate;
		}
		this.addDungeonTimes(dungeonBaseId,this.layerDate);//增加副本当天进入记录
		this.addDungeonHisRecord(dungeonBaseId);//增加进入副本历史记录
	}
	
	/**
	 * 离开副本
	 */
	public void leaveDungeon(){
		this.dungeonId = 0;
		this.dungeonBaseId = 0;
		this.enterDate = 0;
		this.layerDate = 0;
	}
	
	/**
	 * 是否能进入剧情副本
	 * @param dungeonBaseId  基础数据
	 * @return true 可以进入 false 不可以进入
	 */
	public boolean canEnterStory(int dungeonBaseId){
		StoryInfo info = this.getStoryRecord().get(dungeonBaseId);
		if(info == null){
			return true;
		}
		return info.getState() == StoryState.NONE;
	}
	
	/**
	 * 是否可以领取剧情副本奖励
	 * @param dungeonBaseId   基础副本ID
	 * @return true 可以领取 false 反之
	 */
	public boolean isRewardStory(int dungeonBaseId){
		StoryInfo storyInfo = this.getStoryRecord().get(dungeonBaseId);
		if(storyInfo == null){
			return false;
		}
		if(storyInfo.getState() == StoryState.COMPLETE){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 剧情副本成功
	 * @param dungeonBaseId  副本基础ID
	 */
	public synchronized void complete4Story(int dungeonBaseId){
		StoryInfo info = this.getStoryRecord().get(dungeonBaseId);
		if(info == null){
			info = StoryInfo.valueOf(dungeonBaseId, StoryState.COMPLETE);
			this.getStoryRecord().put(dungeonBaseId, info);
			this.serialStory();
		}else{
			info.setState(StoryState.COMPLETE);
			this.getStoryRecord().put(dungeonBaseId, info);
			this.serialStory();
		}
		

	}
	
	/**
	 * 是否已经完成剧情副本
	 * @param dungeonBaseId    基础副本ID
	 * @return true 已经完成 false 反之
	 */
	public boolean isCompleteOrFinishStory(int dungeonBaseId){
		StoryInfo info = this.getStoryRecord().get(dungeonBaseId);
		if(info == null){
			return false;
		}
		
		if(info.getState() == StoryState.COMPLETE || info.getState() == StoryState.FINISH){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 剧情副本完成
	 * @param dungeonBaseId  副本基础ID
	 */
	public synchronized void finish4Story(int dungeonBaseId){
		StoryInfo info = this.getStoryRecord().get(dungeonBaseId);
		if(info == null){
			info = StoryInfo.valueOf(dungeonBaseId, StoryState.FINISH);
			this.getStoryRecord().put(dungeonBaseId, info);
			this.serialStory();
		}else{
			info.setState(StoryState.FINISH);
			this.getStoryRecord().put(dungeonBaseId, info);
			this.serialStory();
		}

	}
	
	/**
	 * 记录剧情副本状态,通过 {@link PlayerDungeon#story} 字段来转换
	 * @return
	 */
	private HashMap<Integer,StoryInfo> getStoryRecord(){
		if(storyRecord != null){
			return storyRecord;
		}
		
		synchronized (this) {
			if(storyRecord != null){
				return storyRecord;
			}
			
			storyRecord = new HashMap<Integer, StoryInfo>();
			if(story == null){
				story = "";
				return storyRecord;
			}
			
			List<String[]> records = Tools.delimiterString2Array(story);
			if(records == null || records.isEmpty()){
				return storyRecord;
			}
			
			for(String[] record : records){
				if(record.length < 2){
					continue;
				}
				int dungeonBaseId = Integer.parseInt(record[0]);
				int state     =     Integer.parseInt(record[1]);
				storyRecord.put(dungeonBaseId, StoryInfo.valueOf(dungeonBaseId, state));
			}
			
			return storyRecord;
		}
	}
	
	/**
	 * 记录副本的时间,通过{@link PlayerDungeon#data} 字段来转换
	 * @return {@link ConcurrentHashMap}
	 */
	private Map<Integer,DungeonInfo> getDungeonRecord(){
		if(dungeonRecord != null){
			return dungeonRecord;
		}
		
		synchronized (this) {
			if(dungeonRecord != null){
				return dungeonRecord;
			}
			
			dungeonRecord = new HashMap<Integer, DungeonInfo>(1);
			
			if(data == null){
				data = "";
				return dungeonRecord;
			}
			
			List<String[]> records = Tools.delimiterString2Array(data);
			if(records == null || records.isEmpty()){
				return dungeonRecord;
			}
			
			for(String[] record : records){
				if(record.length >= 3){
					int dungeonId = Integer.parseInt(record[0]);
					int times     = Integer.parseInt(record[1]);
					long date     = Long.parseLong(record[2]);
					dungeonRecord.put(dungeonId ,  DungeonInfo.valueOf(dungeonId,times,date));
				}
			}
			
			return dungeonRecord;
		}
	}
	
	
	
	/**
	 * 增加进入副本次数
	 * @param dungeonId   副本的基础ID
	 * @param time        进入副本的时间
	 */
	private void addDungeonTimes(int dungeonId,long time){
		DungeonInfo info = this.getDungeonRecord().get(dungeonId);
		if(info == null){
			info = DungeonInfo.valueOf(dungeonId, 1, time); //自动增加一次
			this.dungeonRecord.put(dungeonId, info);
			info = this.dungeonRecord.get(dungeonId);
		}else{
			int times = info.getTimes();
			info.setTimes(times + 1);
			info.setDate(time);
		}
		
		this.serialData(); //序列化存数数据
	}
	

	/**
	 * 刷新副本进入次数 
	 * @return {@link Boolean} true 成功刷新 false 无所刷新 
	 */
	private boolean flushDungeonTimes(){
		boolean falg = false;
		if(this.flushTime == null){
			this.flushTime = new Date();
			falg = true;
		}
		
		if(this.isDungeonStatus() || DateUtil.isToday(flushTime)){
			if(falg){
				for(Iterator<Entry<Integer,DungeonInfo>> it = this.getDungeonRecord().entrySet().iterator();it.hasNext();){
					DungeonInfo info = it.next().getValue();
					if(info == null){
						continue;
					}
					if(!DateUtil.isToday(new Date(info.getDate()))){
						it.remove();
					}
				}
				this.serialData();
				return true;
			}
			
			return false;
		}else{
			for(Iterator<Entry<Integer,DungeonInfo>> it = this.getDungeonRecord().entrySet().iterator();it.hasNext();){
				DungeonInfo info = it.next().getValue();
				if(info == null){
					continue;
				}
				if(!DateUtil.isToday(new Date(info.getDate()))){
					it.remove();
				}
			}
			
			this.serialData();
			this.flushTime = new Date();//重置时间
			return true;
		}
	}
	
	
	/**
	 * 获取最后一次进入副本时间
	 * @param dungeonId  副本的基础ID
	 * @return 
	 *  <per> 0     从来没有进入过</per>
	 *  <per>>0    最后一次进入的时间(单位:秒)</per>
	 */
	public long getEnterDungeonDate(int dungeonId){
		DungeonInfo info = this.getDungeonRecord().get(dungeonId);
		if(info == null){
			return 0;
		}
		return info.getDate();
	}
	
	/**
	 * 进入副本的次数
	 * @param dungeonId   副本的基础ID
	 * @return
	 *  <per> 0     从来没有进入过</per>
	 */
	public int getEnterDungeonTimes(int dungeonId){
		DungeonInfo info = this.getDungeonRecord().get(dungeonId);
		if(info == null){
			return 0;
		}
		return info.getTimes();
	}
	
	
	/**
	 * 把{@link PlayerDungeon#data}的数据序列化
	 */
	private void serialData(){
		StringBuffer buffer = new StringBuffer();
		for(Entry<Integer,DungeonInfo> entry : this.getDungeonRecord().entrySet()){
			DungeonInfo info = entry.getValue();
			buffer.append(info.toString() + Splitable.ELEMENT_DELIMITER);
		}
		this.data = buffer.toString();
	}
	
	/**
	 * 把{@link PlayerDungeon#story} 的数据序列化
	 */
	private void serialStory(){
		StringBuffer buffer = new StringBuffer();
		for(Entry<Integer,StoryInfo> entry : this.getStoryRecord().entrySet()){
			StoryInfo info = entry.getValue();
			buffer.append(info.toString() + Splitable.ELEMENT_DELIMITER);
		}
		this.story = buffer.toString();
	}
	
	//Getter and Setter...
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getData() {
		this.flushDungeonTimes();
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(long dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getDungeonBaseId() {
		return dungeonBaseId;
	}

	public void setDungeonBaseId(int dungeonBaseId) {
		this.dungeonBaseId = dungeonBaseId;
	}

	public long getEnterDate() {
		return enterDate;
	}

	public void setEnterDate(long enterDate) {
		this.enterDate = enterDate;
	}

	public long getFlushableTime() {
		return flushableTime;
	}

	public void setFlushableTime(long flushableTime) {
		this.flushableTime = flushableTime;
	}

	public String getStory() {
		return story;
	}

	public void setStory(String story) {
		this.story = story;
	}

	public String getHisData() {
		return hisData;
	}

	public void setHisData(String hisData) {
		this.hisData = hisData;
	}

	@Override
	public String toString() {
		return "PlayerDungeon [id=" + id + ", data=" + data + ", story="
				+ story + ", dungeonId=" + dungeonId + ", dungeonBaseId="
				+ dungeonBaseId + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerDungeon other = (PlayerDungeon) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
