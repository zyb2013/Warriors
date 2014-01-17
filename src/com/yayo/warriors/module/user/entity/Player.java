package com.yayo.warriors.module.user.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.lock.IEntity;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.type.BlinkType;
import com.yayo.warriors.module.team.rule.TeamRule;
import com.yayo.warriors.module.user.model.ForbidVO;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Capacity;
import com.yayo.warriors.module.user.type.PlayerStateKey;
import com.yayo.warriors.module.user.type.Sex;
import com.yayo.warriors.type.AdultState;

/**
 * 角色对象
 * 
 * @author Hyint
 */
@Entity
@Table(name="player")
public class Player extends BaseModel<Long> {
	private static final long serialVersionUID = 8373636802915134160L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="playerId")
	private Long id;
	
	/** 角色名字 */
	private String name;
	
	/** 帐号名 */
	private String userName;
	
	/** 用户密码, 这个标识目前是平台传过来的一些校验, 不作为逻辑验证 */
	private String password;
	
	/** 角色的称号 */
	private int title;

	/** 头像 */
	private int icon;
	
	/** 性别 */
	@Enumerated
	private Sex sex;
	
	/** 所在的服务器ID */
	private int serverId;
	
	/** 角色的阵营. 默认无阵营. */
	@Enumerated
	private Camp camp = Camp.NONE;
	
	/** 
	 * 新手指导完成数据
	 * 
	 *  格式: 任务步骤_任务步骤_...
	 */
	@Lob
	private String guide = "";
	
	//---------------- 货币类/背包类 -----------------
	
	/** 金币 */
	private long golden = 0L;
	
	/** 银币 */
	private long silver = 0L;
	
	/** 礼金 */
	private long coupon = 0L;
	
	/** 当前最大背包格子数 */
	private int maxBackSize = 0;

	/** 最大的仓库格子数 */
	private int maxStoreSize = 0;
	
	/** 角色领取记录. 领取类型1_领取类型2_领取类型3... */
	@Lob
	private String receiveInfo = "";
	
	/** 当前宠物栏最大值,初始化值为:4*/
	private int maxPetSlotSize =  0;
	
	/** 玩家的级别. */
	@Enumerated
	private Capacity capacity = Capacity.NONE;
	//-------------------- 角色时间参量 ---------------------------
	
	/** 是否删除 */
	private boolean deletable;
	
	/** 删除角色的时间 */
	private Date deleteTime;

	/** 角色创建时间 */
	private Date createTime = new Date();
	
	/** 登陆时间 */
	private Date loginTime = new Date();

	/** 登出时间 */
	private Date logoutTime = new Date();
	
	/** 禁止登录. 封禁状态_封禁结束时间(单位:毫秒) */
	private String forbidLogin = "";
	
	/** 禁言标志 . 封禁状态_封禁结束时间(单位:毫秒) */
	private String forbidChat = "";
	
	/** 当前在线时间(单位: 秒, 当天在线时间累积, 过12点则清空) */
	private long onlineTimes;

	/** 登录天数(总共在线的天数) */
	private int loginDays = 0;
	
	/** 当前连续登录天数.(如果本次登录与上次登录超过一天, 则该值变为0) */
	private int continueDays = 0;

	/** 最大连续天数.(当当前连续登陆天数每超过最大连续天数, 则更新为最大的天数) */
	private int continueMaxDays = 0;
	
	/** 登录次数(总共登录的次数) */
	private int loginCount = 0;
	
	/** 是否显示时装*/
	private boolean fashionShow = false;
	
	/** 家将历练值*/
	private int petexperience = 0;
	
	// ------------------ 防沉迷字段 ----------------------
	
	/** 
	 * 成年状态, 初始化是未填写状态
	 * (0是未验证防沉迷, 1是未成年, 2是成年)
	 */
	@Enumerated
	private AdultState adult = AdultState.NONE;
	
	/*** 装备闪光类型*/
	@Transient
	private transient volatile BlinkType blinkType = BlinkType.NONE;
	
	/** 是否保护模式
	 *  <pre>玩家在押镖状态下,如果被劫镖将来会变成保护状态</pre>
	 *  <pre>保护状态规则:</pre>
	 *  <li>玩家不受任何攻击</li>
	 *  <li>玩家不可以攻击任何对象</li>
	 * */
	@Transient
	private transient volatile boolean protection = false;
	
	/**
	 * 复活保护时间(单位:秒)
	 * 使用复活酒复活之后(原地复活)，角色3秒的无敌时间，发出攻击则取消无敌状态
	 * 无敌期间，其他玩家和怪物无法攻击玩家
	 */
	@Transient
	private transient volatile long reviveProteTime = 0;
	
	
	public String getReceiveInfo() {
		return receiveInfo;
	}

	public void setReceiveInfo(String receiveInfo) {
		this.receiveSet = null;
		this.receiveInfo = receiveInfo;
	}

	public Capacity getCapacity() {
		return capacity;
	}

	public void setCapacity(Capacity capacity) {
		this.capacity = capacity;
	}

	/**
	 * 计算总共在线时间
	 * 
	 * @return {@link Long}		总共在线时间的秒数
	 */
	public long getCurrentTotalOnlineTimes() {
		Date nowDate = new Date();
		int mis = TimeConstant.ONE_SECOND_MILLISECOND;
		if(DateUtil.isToday(this.loginTime)) { //登录时间是今天, 则把记录的在线时间和当前在线时间累加
			long loginTimeMillis = loginTime.getTime();
			long currentMillisecond = nowDate.getTime();
			long currentOnlineTimeSecond = (currentMillisecond - loginTimeMillis) / mis;
			return this.onlineTimes + Math.max(0, currentOnlineTimeSecond);
		} else {	//登录时间不是今天, 则计算从0点到当前时间
			long zeroTimeMillis = DateUtil.getDate0AM(nowDate).getTime();
			return Math.max(0, (nowDate.getTime() - zeroTimeMillis) / mis);
		}
	}
	
	
	/**
	 * 防沉迷验证, 是否有物品奖励(未成年和未填写防沉迷信息的玩家在在线5个小时后物品收益为0)
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isGoodsReward() {
		return this.adult.isGoodsReward(this);
	}
	
	public boolean isExpReward() {
		return this.adult.addRatio(this) > 0;
	}

	public boolean isSilverReward() {
		return this.adult.addRatio(this) > 0;
	}
	
	public int getContinueDays() {
		return continueDays;
	}

	public void setContinueDays(int continueDays) {
		this.continueDays = continueDays;
	}

	public int calcIndulgeProfit(int value) {
		return (int) (this.adult.addRatio(this) * value);
	}
	
	public int getMaxBackSize(int backpackType) {
		if(backpackType == BackpackType.DEFAULT_BACKPACK) {
			return this.maxBackSize;
		} else if(backpackType == BackpackType.DEFAULT_BACKPACK) {
			return this.maxStoreSize;
		}
		return 0;
	}
	//------------------------临时参量------------------------
	@Transient
	private transient volatile Set<Integer> guides = null;
	
	/** 领取数组 */
	@Transient
	private transient volatile Set<Integer> receiveSet = null;
	
	/** 放置到内存玩家属性 */
	@Transient
	private transient volatile ConcurrentHashMap<PlayerStateKey, Object> attributes = new ConcurrentHashMap<PlayerStateKey, Object>(5);
	
	/**
	 * 背包锁对象
	 */
	@Transient
	private transient PackLock packLock = new PackLock();
	
	public PackLock getPackLock() {
		return packLock;
	}

	/**
	 * 获得截取的集合列表
	 * 
	 * @return {@link Set}
	 */
	public Set<Integer> getReceiveSet() {
		if(this.receiveSet != null) {
			return receiveSet;
		}
		synchronized (this) {
			if(this.receiveSet != null) {
				return receiveSet;
			}
			this.receiveSet = new HashSet<Integer>();
			if(StringUtils.isBlank(this.receiveInfo)) {
				return this.receiveSet;
			}
			
			for (String element : receiveInfo.split(Splitable.ATTRIBUTE_SPLIT)) {
				if(!StringUtils.isBlank(element)) {
					this.receiveSet.add(Integer.valueOf(element));
				}	
			}
		}
		return this.receiveSet;
	}
	
	public boolean containsReceives(int receiveId) {
		return this.getReceiveSet().contains(receiveId);
	}
	
	public void addReceiveInfo(int receiveId, boolean update) {
		this.getReceiveSet().add(receiveId);
		if(update) this.updateReceiveInfo();
	}
	
	/**
	 * 更新领取信息
	 */
	public void updateReceiveInfo() {
		StringBuffer buffer = new StringBuffer();
		Set<Integer> infos = this.getReceiveSet();
		for (Integer element : infos) {
			buffer.append(element).append(Splitable.ATTRIBUTE_SPLIT);
		}
		this.receiveInfo = buffer.toString();
	}
	
	public Set<Integer> getGuides() {
		if(this.guides != null) {
			return this.guides;
		}
		
		synchronized (this) {
			if(this.guides != null) {
				return this.guides;
			}
		
			this.guides = new HashSet<Integer>();
			if(!StringUtils.isBlank(this.guide)){
				return this.guides;
			}
			
			String[] steps = this.guide.split(Splitable.ATTRIBUTE_SPLIT);
			for(String step : steps){
				if(StringUtils.isNotBlank(step)){
					this.guides.add( Integer.valueOf(step) );
				}
			}
		}
		return this.guides;
	}
	
	public void saveGuides() {
		StringBuilder gd = new StringBuilder();
		Set<Integer> guideSet = this.getGuides();
		for(int guide : guideSet){
			gd.append(Splitable.ATTRIBUTE_SPLIT).append(guide);
		}
		if(gd.length() > 0){
			gd.deleteCharAt(0);
		}
		this.guide = gd.toString();
	}

	
	public Long getId() {
		return id;
	}

	
	public void setId(Long id) {
		this.id = id;
	}
 
	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

	public String getForbidLogin() {
		return forbidLogin;
	}

	public void setForbidLogin(String forbidLogin) {
		this.forbidLogin = forbidLogin;
	}

	public int getBranching() {
		Integer branching = (Integer) attributes.get(PlayerStateKey.BRANCHING);
		return branching == null ? -1 : branching;
	}

	public void setBranching(int branching) {
		attributes.put(PlayerStateKey.BRANCHING, branching);
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}

	public long getSilver() {
		return silver;
	}

	public void setSilver(long silver) {
		this.silver = silver;
	}

	public void increaseSilver(long silver) {
		this.silver += silver;
	}
	
	public void decreaseSilver(long silver) {
		this.silver -= silver;
	}

	public long getGolden() {
		return golden;
	}

	public void setGolden(long golden) {
		this.golden = golden;
	}

	public void increaseGolden(long golden) {
		this.golden += golden;
	}
	
	public void decreaseGolden(long golden) {
		this.golden -= golden;
	}

	public int getMaxBackSize() {
		return maxBackSize;
	}

	public int getMaxPetSlotSize() {
		return maxPetSlotSize;
	}

	public void setMaxPetSlotSize(int maxPetSlotSize) {
		this.maxPetSlotSize = maxPetSlotSize;
	}
	
	public int getMaxStoreSize() {
		return maxStoreSize;
	}

	public void setMaxStoreSize(int maxStoreSize) {
		this.maxStoreSize = maxStoreSize;
	}

	public int getTeamId() {
		Integer teamId = (Integer) attributes.get(PlayerStateKey.TEAM_ID);
		return teamId == null ? TeamRule.NO_TEAM_ID : teamId;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public void addLoginDays(int loginDays) {
		this.loginDays += loginDays;
	}

	public void addContinueDays(int loginDays) {
		this.continueDays += loginDays;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}
	
	public void addLoginCount(int loginCount) {
		this.loginCount += loginCount;
	}

	public void setTeamId(int teamId) {
		attributes.put(PlayerStateKey.TEAM_ID, teamId);
	}

	public void setMaxBackSize(int maxBackSize) {
		this.maxBackSize = maxBackSize;
	}

	public String getForbidChat() {
		return forbidChat;
	}
	
	public void setForbidChat(String forbidChat) {
		this.forbidChat = forbidChat;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public ForbidVO getForbidChatVO() {
		return getForbitVO(PlayerStateKey.FORBIT_CHAT_VO, this.forbidChat);
	}

	public ForbidVO getForbidLoginVO() {
		return getForbitVO(PlayerStateKey.FORBIT_LOGIN_VO, this.forbidLogin);
	}
	
	public void updateForbidChat() {
		forbidChat = getForbidChatVO().toString();
	}
	
	public void updateForbidLogin() {
		forbidChat = getForbidLoginVO().toString();
	}

	public BlinkType getBlinkType() {
		return blinkType;
	}

	public synchronized void setBlinkType(BlinkType blinkType) {
		this.blinkType = blinkType;
	}

	/**
	 * 查看玩家是否被封禁聊天
	 * 
	 * @return	{@link Boolean}	角色被禁言, false-角色没有被禁言
	 */
	public boolean isForbid2Chat() {
		ForbidVO forbid = this.getForbidChatVO();
		if(forbid != null){
			if(forbid.isForbidden()){
				return true;
			} else{
				removeAttribute(PlayerStateKey.FORBIT_CHAT_VO);
			}
		}
		return false;
	}
	
	/**
	 * 查看玩家是否被封禁登陆
	 * 
	 * @return	{@link Boolean}	角色被封禁登陆, false-角色没有被封禁登陆
	 */
	public boolean isForbid2Login() {
		ForbidVO forbid = this.getForbidLoginVO();
		if(forbid != null){
			if(forbid.isForbidden()){
				return true;
			} else{
				removeAttribute(PlayerStateKey.FORBIT_LOGIN_VO);
			}
		}
		return false;
	}
	
	/**
	 * 获得封禁VO对象
	 * 
	 * @param stateKey
	 * @param forbitInfo
	 * @return
	 */
	private ForbidVO getForbitVO(PlayerStateKey stateKey, String forbitInfo) {
		ForbidVO forbidChatVO = (ForbidVO)attributes.get(stateKey);
		if(forbidChatVO != null) {
			return forbidChatVO;
		}
		
		synchronized (this) {
			forbidChatVO = (ForbidVO) attributes.get(stateKey);
			if(forbidChatVO != null) {
				return forbidChatVO;
			}
			
			long endTime = 0;
			int forbid = ForbidVO.NORMAL;
			if(!StringUtils.isBlank(forbitInfo)) {
				String[] array = forbitInfo.split(Splitable.ATTRIBUTE_SPLIT);
				if(array.length > 0) forbid = Integer.valueOf(array[0]);
				if(array.length > 1) endTime = Long.valueOf(array[1]);
			}
			
			attributes.put(stateKey,  ForbidVO.valueOf(forbid, endTime));
			forbidChatVO = (ForbidVO)attributes.get(stateKey);
		}
		return forbidChatVO;
	}
	
	
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + serverId;
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (serverId != other.serverId)
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	
	
	public String toString() {
		return "Player [id=" + id + ", name=" + name + ", userName=" + userName
				+ ", password=" + password + ", title=" + title + ", icon="
				+ icon + ", sex=" + sex + ", serverId=" + serverId + ", camp="
				+ camp + ", guide=" + guide + ", golden=" + golden
				+ ", silver=" + silver + ", maxBackSize=" + maxBackSize
				+ ", maxStoreSize=" + maxStoreSize + ", receiveInfo="
				+ receiveInfo + ", maxPetSlotSize=" + maxPetSlotSize
				+ ", deletable=" + deletable + ", deleteTime=" + deleteTime
				+ ", createTime=" + createTime + ", loginTime=" + loginTime
				+ ", logoutTime=" + logoutTime + ", forbidLogin=" + forbidLogin
				+ ", forbidChat=" + forbidChat + ", onlineTimes=" + onlineTimes
				+ ", loginDays=" + loginDays + ", loginCount=" + loginCount
				+ ", fashionShow=" + fashionShow + ", petexperience="
				+ petexperience + ", adult=" + adult + ", packLock=" + packLock
				+ "]";
	}

	/**
	 * 构造函数 (创建角色的时候使用)
	 * 
	 * @param  userName       	账号ID
	 * @param  playerName   	角色名字
	 * @param  job            	职业
	 * @param  sex       	   	性别
	 * @param  icon		    	头像图标
	 * @return {@link Player}	角色对象
	 */
	public static Player valueOf(String userName, String playerName, Sex sex,int icon){
		Player player = new Player();
		player.sex = sex;
		player.icon = icon;
		player.name = playerName;
		player.userName = userName;
		return player;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(PlayerStateKey key, Class<T> clazz) {
		return (T) attributes.get(key);
	} 
	
	@SuppressWarnings("unchecked")
	public <T> T setAttribute(PlayerStateKey key, Object value) {
		return (T) attributes.put(key, value);
	}

	public void removeAttribute(PlayerStateKey key) {
		attributes.remove(key);
	}

	public long getOnlineTimes() {
		return onlineTimes;
	}

	public void setOnlineTimes(long onlineTimes) {
		this.onlineTimes = onlineTimes;
	}

	public void addOnlineTimes(long onlineTimes) {
		if(onlineTimes > 0) {
			this.onlineTimes += onlineTimes;
		}
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public Date getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public boolean isFashionShow() {
		return fashionShow;
	}

	public void setFashionShow(boolean fashionShow) {
		this.fashionShow = fashionShow;
	}

	/**
	 * 是否可以新增物品到背包中..
	 * 
	 * @param  totalSize		当前需要新增的背包总量+背包当前的物品数量
	 * @param  backpack			背包号
	 * @return {@link Boolean}	true-可以增加, false-不可以增加
	 */
	public boolean canAddNew2Backpack(int totalSize, int backpack) {
		if(backpack == BackpackType.DEFAULT_BACKPACK) {
			return totalSize <= this.maxBackSize;
		} else if(backpack == BackpackType.STORAGE_BACKPACK) {
			return totalSize <= this.maxStoreSize;
		} else {
			return false;
		}
	}

	public String getGuide() {
		return guide;
	}

	public void setGuide(String guide) {
		this.guide = guide;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public AdultState getAdult() {
		return adult;
	}
	
	public void setAdult(AdultState adult) {
		this.adult = adult;
	}
	
	public int getPetexperience() {
		return petexperience;
	}

	public void setPetexperience(int petexperience) {
		this.petexperience = petexperience;
	}

	public boolean isProtection() {
		return protection;
	}

	public void setProtection(boolean protection) {
		this.protection = protection;
	}

	public long getCoupon() {
		return coupon;
	}

	public void setCoupon(long coupon) {
		this.coupon = coupon;
	}
	
	public void increaseCoupon(long coupon) {
		this.coupon += coupon;
	}
	
	public void decreaseCoupon(long coupon) {
		this.coupon -= coupon;
	}

	public int getContinueMaxDays() {
		return continueMaxDays;
	}

	public void setContinueMaxDays(int continueMaxDays) {
		this.continueMaxDays = continueMaxDays;
	}
	
	public void updateContinueMaxDays(int continueMaxDays) {
		this.continueMaxDays = Math.max(continueMaxDays, this.continueMaxDays);
	}

	public long getReviveProteTime() {
		return reviveProteTime;
	}

	public void setReviveProteTime(long reviveProteTime) {
		this.reviveProteTime = reviveProteTime;
	}

	public boolean isReviveProteTime() {
		return this.reviveProteTime >= DateUtil.getCurrentSecond();
	}
	
	/**
	 * 	背包锁对象
	 */
	public class PackLock implements IEntity<Long>{
		
		public Long getIdentity() {
			return id;
		}
	}

}
