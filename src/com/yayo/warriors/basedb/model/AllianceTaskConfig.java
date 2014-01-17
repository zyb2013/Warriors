package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.common.helper.FormulaHelper;

/**
 * 帮派任务配置对象
 * <per>帮派任务的规则是玩家一旦加入了帮派,将会自动接受所有任务</per>
 * @author liuyuhua
 */
@Resource
public class AllianceTaskConfig {

	/** 任务的ID*/
	@Id
	private int id;
	
	/** 任务名字 */
	private String name;
	
	/** 任务事件 (格式:{任务类型_条件_数量|任务类型_条件_数量})*/
	private String addition1;
	
	/** 允许完成的次数*/
	private int completeCount;
	
	/** 奖励的经验值*/
	private String exp;
	
	/** 奖励的贡献值*/
	private int donate;
	
	/** 奖励帮派资金(非个人)*/
	private int allianceSilver;
	
	/** 解析 {@link AllianceTaskConfig#addition1}*/
	@JsonIgnore
	private transient volatile List<String[]> eventslist = null;
	
	/** 任务中存在的事件类型*/
	@JsonIgnore
	private transient volatile List<Integer> eventTypes = null;
	
	/**
	 * 计算或的的经验值得
	 * @param   level          玩家的等级
	 * @return {@link Integer} 获得的经验值
	 */
	public int caclExp(int level){
		return FormulaHelper.invoke(this.exp, level).intValue();
	}
	
	/**
	 * 获取事件集合
	 * @return {@link List} 事件集合
	 */
	public List<String[]> getEventsList(){
		if(eventslist != null){
			return eventslist;
		}
		synchronized (this) {
			eventslist = new ArrayList<String[]>();
			if(addition1 == null || addition1.isEmpty()){
				return eventslist;
			}
			
			List<String[]> results = Tools.delimiterString2Array(addition1);
			for(String[] result : results){
				if(result.length >= 3){
					eventslist.add(result);
				}
			}
			return eventslist;
		}
	}
	
	/**
	 * 是否存在该类型事件
	 * @param type    事件类型
	 * @return true 存在 false 反之
	 */
	public boolean hasEventType(int type){
		if(eventTypes != null){
			return eventTypes.contains(type);
		}
		synchronized (this) {
			if(eventTypes != null){
				return eventTypes.contains(type);
			}
			eventTypes = new ArrayList<Integer>();
			List<String[]> events = this.getEventsList();
			if(events != null && !events.isEmpty()){
				for(String[] event : events){
					int eventType = Integer.parseInt(event[0]);
					if(!eventTypes.contains(eventType)){
						eventTypes.add(eventType);
					}
				}
			}
			return eventTypes.contains(type);
		}
	}
	
	// Getter and Setter...
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddition1() {
		return addition1;
	}

	public void setAddition1(String addition1) {
		this.addition1 = addition1;
	}
	
	public int getCompleteCount() {
		return completeCount;
	}

	public void setCompleteCount(int completeCount) {
		this.completeCount = completeCount;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}

	public int getAllianceSilver() {
		return allianceSilver;
	}

	public void setAllianceSilver(int silver) {
		this.allianceSilver = silver;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "AllianceTaskConfig [id=" + id + ", addition1=" + addition1
				+ ", completeCount=" + completeCount + ", exp=" + exp
				+ ", donate=" + donate + ", allianceSilver=" + allianceSilver + "]";
	}
	
}
