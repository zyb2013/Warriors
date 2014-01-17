package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.type.IndexName;

/**
 * 押镖任务基础配置对象 
 * @author liuyuhua
 */
@Resource
public class EscortTaskConfig {
	
	/** 普通 类型任务*/
	@JsonIgnore
	public static final int SIMPLE = 0;
	
	/** 押镖任务ID*/
	@Id
	private int id;
	
	/** 护送任务名 */
	private String name;
	
	/** 阵营*/
	@Index(name = IndexName.ESCORT_TASK_CAMP_TYPE)
	private int camp;
	
	/** 0 普通 , 1 国运*/
	private int type;
	
	/** 接受任务的NpcId*/
	private int acceptNpc;
	
	/** 完成任务的NpcId*/
	private int completeNpc;
	
	/** 任务最小可接等级*/
	private int minLevel;
	
	/** 任务最打可接等级*/
	private int maxLevel;
	
	/** 道具奖励 {道具Id_数量_绑定状态|道具Id_数量_绑定状态}*/
	private String itemRewards;
	
	/** 奖励的经验系数*/
	private String exp;
	
	/** 限制时间(单位:秒)*/
	private int limitTime;
	
	/** 道具奖励Map*/
	@JsonIgnore
	private transient volatile List<RewardVO> rewardsInfo = null;

	/**
	 * 获取任务道具奖励
	 * @return {@link Map<Integer,Integer>} 奖励物品的集合
	 */
	public List<RewardVO> getItemRewardMap(){
		if(rewardsInfo != null){
			return rewardsInfo;
		}
		synchronized (this) {
			if(rewardsInfo != null){
				return rewardsInfo;
			}
			
			this.rewardsInfo = new ArrayList<RewardVO>(0);
			if(itemRewards == null || itemRewards.isEmpty()){
				return rewardsInfo;
			}
			
			Map<Integer, int[]> itemMaps = new HashMap<Integer, int[]>();
			List<String[]> items = Tools.delimiterString2Array(itemRewards);
			for(String[] item : items) {
				if(item.length < 3){
					continue;
				}
				
				int propsId = Integer.parseInt(item[0]); 
				int number  = Integer.parseInt(item[1]);
				int[] array = itemMaps.get(propsId);
				if(array == null) {
					array = new int[2];
				}
				
				if(Integer.parseInt(item[2]) < 1) { 
					array[0] = array[0] + number;
				} else {
					array[1] = array[1] + number;
				}
				itemMaps.put(propsId, array);
			}
			
			for (Entry<Integer, int[]> entry : itemMaps.entrySet()) {
				int propsId = entry.getKey();
				int[] value = entry.getValue();
				if(value[0] > 0) {
					this.rewardsInfo.add(RewardVO.props(propsId, value[0], false));
				}
				if(value[1] > 0) {
					this.rewardsInfo.add(RewardVO.props(propsId, value[1], true));
				}
			}
			return this.rewardsInfo;
		}
	}
	
	/**
	 * 获取任务经验奖励
	 * @param level      玩家的等级
	 * @param quality    任务的品级
	 * @return {@link Integer} 经验值
	 */
	public int getExpValue(int level, int quality) {
		return FormulaHelper.invoke(this.exp, quality, level).intValue();
	}
	
	//Getter and Setter....
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAcceptNpc() {
		return acceptNpc;
	}

	public void setAcceptNpc(int acceptNpc) {
		this.acceptNpc = acceptNpc;
	}

	public int getCompleteNpc() {
		return completeNpc;
	}

	public void setCompleteNpc(int completeNpc) {
		this.completeNpc = completeNpc;
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

	public String getItemRewards() {
		return itemRewards;
	}

	public void setItemRewards(String itemRewards) {
		this.itemRewards = itemRewards;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public int getLimitTime() {
		return limitTime;
	}

	public void setLimitTime(int limitTime) {
		this.limitTime = limitTime;
	}

	@Override
	public String toString() {
		return "EscortTaskConfig [id=" + id + ", camp=" + camp + ", type="
				+ type + ", acceptNpc=" + acceptNpc + ", completeNpc="
				+ completeNpc + ", minLevel=" + minLevel + ", maxLevel="
				+ maxLevel + ", itemRewards=" + itemRewards + ", exp=" + exp
				+ ", limitTime=" + limitTime + "]";
	}
}
