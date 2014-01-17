package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.task.model.RewardVO;

/**
 * 试炼任务奖励
 * 
 * @author Hyint
 */
@Resource
public class PracticeRewardConfig {

	/** 当前任务的次数 */
	@Id
	private int id;

	/** 游戏币奖励 */
	private String expExpr;

	/** 游戏币奖励 */
	private String gasExpr;

	/** 游戏币奖励 */
	private String silverExpr;
	
	/** 道具ID. 格式: 道具ID_道具数量_绑定状态(0-未绑定, 1-绑定)| */
	private String propsId ;

	/** 道具对象 */
	@JsonIgnore
	private List<RewardVO> rewards = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getExpExpr() {
		return expExpr;
	}

	public void setExpExpr(String expExpr) {
		this.expExpr = expExpr;
	}
	
	public String getPropsId() {
		return propsId;
	}
	
	public void setPropsId(String propsId) {
		this.propsId = propsId;
	}

	public String getGasExpr() {
		return gasExpr;
	}

	public void setGasExpr(String gasExpr) {
		this.gasExpr = gasExpr;
	}

	public String getSilverExpr() {
		return silverExpr;
	}

	public void setSilverExpr(String silverExpr) {
		this.silverExpr = silverExpr;
	}

	public int getExpValue(int level, int quality) {
		return FormulaHelper.invoke(this.expExpr, quality, level).intValue();
	}

	public int getSilverValue(int level, int quality) {
		return FormulaHelper.invoke(this.silverExpr, quality, level).intValue();
	}

	public int getGasValue(int level, int quality) {
		return FormulaHelper.invoke(this.gasExpr, quality, level).intValue();
	}
	
	public List<RewardVO> getRewardList() {
		if(this.rewards != null) {
			return this.rewards;
		}
		
		synchronized (this) {
			if(this.rewards != null) {
				return this.rewards;
			}
			
			this.rewards = new ArrayList<RewardVO>();
			List<String[]> arrays = Tools.delimiterString2Array(this.propsId);
			if(arrays == null || arrays.isEmpty()) {
				return this.rewards;
			}
			
			Map<Integer, int[]> maps = new HashMap<Integer, int[]>();
			for (String[] element : arrays) {
				Integer itemId = Integer.valueOf(element[0]);
				Integer count = Integer.valueOf(element[1]);
				int[] array = maps.get(itemId);
				if(array == null) {
					array = new int[2];
				}
				
				if(element.length < 2 || Integer.valueOf(element[2]) < 1) {
					array[0] = array[0] + count;
				} else {
					array[1] = array[1] + count;
				}
				maps.put(itemId, array);
			}
			
			for (Entry<Integer, int[]> entry : maps.entrySet()) {
				int itemId = entry.getKey();
				int[] value = entry.getValue();
				if(value[0] > 0) {
					this.rewards.add(RewardVO.props(itemId, value[0], false));
				}
				if(value[1] > 0) {
					this.rewards.add(RewardVO.props(itemId, value[1], true));
				}
			}
		}
		return this.rewards;
	}
	
	public boolean canReward() {
		return !this.getRewardList().isEmpty();
	}
	
	@Override
	public String toString() {
		return "PracticeRewardConfig [id=" + id + ", expExpr=" + expExpr + ", gasExpr=" 
				+ gasExpr + ", silverExpr=" + silverExpr + ", propsId=" + propsId + "]";
	}
}
