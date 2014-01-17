package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.TimeConstant;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.GoodsType;

/**
 * 掉落奖励表
 * 
 * @author Hyint
 */
@Resource
public class DropConfig {
	public static final String REWARD_NO = "rewardNo";
	
	@Id
	private int id;

	/**
	 * 全局奖励刷新周期 <br>
	 * 此行奖励的刷新周期，以天为单位. 
	 */
	private int period;
	
	/** 
	 * 奖励编号(同一组编号组成一次掉落)
	 */
	@Index(name=REWARD_NO, order = 0)
	private int rewardNo;
	
	/** 
	 * 掉落概率. 
	 */
	private int rate = 0;
	
	/**
	 * 物品发送类别<br>
	 * 区分发送的物品类别
	 * 
	 * <pre>
	 * -1. 没有掉落
	 *  0. 道具类型
	 *  1. 装备类型
	 *  2. 游戏币类型
	 *  3. 元宝类型
	 * </pre>
	 */
	private int type = GoodsType.NONE;
	
	/**
	 * 消失时间. 就是从物品掉落到消失的时间间隔. (单位: 秒)
	 */
	private int dieoutTime = 0;
	
	/**
	 * 道具序列号<br>
	 * 
	 * <pre>
	 * 
	 * 装备类型格式: 装备ID_绑定状态(0-不绑定, 1-绑定)|装备ID_绑定状态(0-不绑定, 1-绑定)|....
	 * 
	 * 道具类型格式: 道具ID_绑定状态(0-不绑定, 1-绑定)|道具ID_绑定状态(0-不绑定, 1-绑定)|....
	 * 
	 * 货币类型格式: 掉落的最小数量_最大数量|
	 * 
	 * 货币类型:  0. 银两
	 *           1. 礼券
	 *           2. 元宝
	 * </pre>
	 */
	private String serialNums = null;

	/**
	 * 每次发放奖励数量<br>
	 * 
	 * <pre>
	 * 每次获得该行物品的个数.不可为空或者非正整数.
	 * 
	 * 装备和道具: 都会按照这个数量发放并且记录也是按照此值记录.
	 * 
	 * 货币: 则只会按照1次记录
	 * </pre>
	 */
	private int amount = 0;

	/**
	 * 全局发放总量上限(个数)<br>
	 * <pre>
	 * 在一个刷新周期内，整个服务器所有玩家获取奖励个数的限制。 
	 * 如果在刷新周期内，已经达到全服总量上限，则再次抽到此行奖励，用默认奖励代替
	 * 0代表不限制
	 * </pre>
	 */
	private int maxAmount = 0;

	/** 个人最大上限 */
	private int maxPersonal = 0;

	/** 
	 * 角色的职业限制. 详细见: {@link Job}. </br>
	 * 
	 * 限制的职业. 如果填写了的职业, 则都不能掉. 格式: 职业1|职业2|... </br>
	 * 
	 * 则指定的职业不能掉落
	 */
	private String job;
	
	/** 可以获得物品的最小等级 */
	private int minLevel;

	/** 可以获得物品的最大等级 */
	private int maxLevel;
	
	/**
	 * 系统公告<br>
	 * <pre>
	 * 需要发送公告的奖励行，填写公告ID，公告ID一般都和奖励ID一致。 
	 * 不填则表示不发公告
	 * </pre>
	 */
	private boolean notice = false;

	/**
	 * 是否默认掉落.
	 */
	private boolean acquiesce;
	
	/** 
	 * 满值 程序将根据这个值来生成随机数 [1,fullValue] 
	 */
	private volatile int fullValue = 0;
	
	/**
	 * 奖励的道具ID列表
	 * 根据 serialNums 转换得到
	 */
	@JsonIgnore
	private transient List<DropInfo> dropInfos = null;
	
	/** 被限制的职业列表 */
	private transient List<Integer> jobLimiters = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public int getRewardNo() {
		return rewardNo;
	}

	public void setRewardNo(int rewardNo) {
		this.rewardNo = rewardNo;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getFullValue() {
		return fullValue;
	}

	public void setFullValue(int fullValue) {
		this.fullValue = fullValue;
	}

	public String getSerialNums() {
		return serialNums;
	}

	public void setSerialNums(String serialNums) {
		this.serialNums = serialNums;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(int maxAmount) {
		this.maxAmount = maxAmount;
	}
 
	public boolean isNotice() {
		return notice;
	}

	public void setNotice(boolean notice) {
		this.notice = notice;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<DropInfo> getDropInfoList() {
		if(this.dropInfos != null) {
			return dropInfos;
		}
		synchronized (this) {
			if(this.dropInfos != null) {
				return this.dropInfos;
			}
			
			this.dropInfos = new ArrayList<DropInfo>();
			List<String[]> arrays = Tools.delimiterString2Array(this.serialNums);
			if(arrays == null || arrays.isEmpty()) {
				return this.dropInfos;
			}
			
			boolean isMoney = ArrayUtils.contains(GoodsType.CURRENCYS, this.type); //货币类型
			for (String[] dropInfo : arrays) {
				Integer dropInfoId = Integer.valueOf(dropInfo[0]);
				if(isMoney) {
					Integer dropMoney = Integer.valueOf(dropInfo[1]);
					this.dropInfos.add(DropInfo.valueOf(dropInfoId, false));
					this.dropInfos.add(DropInfo.valueOf(dropMoney, false));
				} else {
					boolean binding = dropInfo.length > 1 && Integer.valueOf(dropInfo[1]) >= 1;
					this.dropInfos.add(DropInfo.valueOf(dropInfoId, binding));
				}
			}
		}
		
		return this.dropInfos;
	}

	public boolean isAcquiesce() {
		return acquiesce;
	}

	public void setAcquiesce(boolean acquiesce) {
		this.acquiesce = acquiesce;
	}

	public int getDieoutTime() {
		return dieoutTime;
	}

	public long getDieoutTimeMillis() {
		return dieoutTime * TimeConstant.ONE_SECOND_MILLISECOND;
	}

	public void setDieoutTime(int dieoutTime) {
		this.dieoutTime = dieoutTime;
	}

	public int getMaxPersonal() {
		return maxPersonal;
	}

	public void setMaxPersonal(int maxPersonal) {
		this.maxPersonal = maxPersonal;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}
	
	public List<Integer> getJobLimiters() {
		if(this.jobLimiters != null) {
			return this.jobLimiters;
		}
		
		synchronized (this) {
			if(this.jobLimiters != null) {
				return this.jobLimiters;
			}
			this.jobLimiters = new LinkedList<Integer>();
			if(StringUtils.isBlank(this.job)) {
				return jobLimiters;
			}
			
			String[] elements = this.job.split(Splitable.ELEMENT_SPLIT);
			for (String element : elements) {
				if(StringUtils.isBlank(element)) {
					continue;
				}
				
				Integer value = Integer.valueOf(element);
				if(!this.jobLimiters.contains(value)) {
					this.jobLimiters.add(value);
				}
			}
		}
		return this.jobLimiters;
	}
	/**
	 * 是否可以掉落
	 * 
	 * @param  playerJob		角色的职业
	 * @param  playerLevel		角色的等级
	 * @return {@link Boolean}	true-可以掉落, false-不可以掉落
	 */
	public boolean isCanDrop(int playerJob, int playerLevel) {
		boolean canDrop = false;
		List<Integer> jobLimit = this.getJobLimiters();
		if(!jobLimit.contains(playerJob)) {
			int min = Math.min(minLevel, maxLevel);
			int max = Math.max(minLevel, maxLevel);
			canDrop = playerLevel >= min && playerLevel <= max;
		}
		return canDrop;
	}
	
	@Override
	public String toString() {
		return "DropConfig [id=" + id + ", period=" + period + ", rewardNo=" + rewardNo + ", rate="
				+ rate + ", type=" + type + ", job=" + job + ", acquiesce=" + acquiesce + "]";
	}
	
	/**
	 * 掉落信息
	 * 
	 * @author Hyint
	 */
	public static class DropInfo {
		private int info;
		private boolean binding;
		public int getInfo() {
			return info;
		}
		public void setInfo(int info) {
			this.info = info;
		}
		public boolean isBinding() {
			return binding;
		}
		public void setBinding(boolean binding) {
			this.binding = binding;
		}
		
		public static DropInfo valueOf(int info, boolean binding) {
			DropInfo dropInfo = new DropInfo();
			dropInfo.info = info;
			dropInfo.binding = binding;
			return dropInfo;
		}
	}
}
