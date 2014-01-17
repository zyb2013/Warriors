package com.yayo.warriors.module.vip.entity;

import static com.yayo.warriors.module.vip.model.VipFunction.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.TimeConstant;

@Entity
@Table(name = "playerVip")
public class PlayerVip extends BaseModel<Long> {
	private static final long serialVersionUID = -7290417914274055168L;
	
	@Id
	@Column(name = "playerId")
	private Long id;
	
	/** VIP等级*/
	private int vipLevel = 0;
	
	/** VIP结束时间*/
	private long vipEndTime = 0;
	
	/** 其他参数*/
	private String param = "";
	
	/** 最后数据清0日期*/
	private int lastCleanDate = 0;
	
	/** 其他参数 缓存成MAP*/
	@Transient
	private transient volatile Map<String, Integer> paramMap = null; 
	
	
	public PlayerVip() {}
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public int getVipLevel() {
		return vipLevel;
	}
	
	public void setVipLevel(int vipLevel) {
		this.paramMap = null;
		this.vipLevel = vipLevel;
	}
	
	public long getVipEndTime() {
		return vipEndTime;
	}
	
	public void setVipEndTime(long vipEndTime) {
		this.vipEndTime = vipEndTime;
	}
	
	public String getParam() {
		return param;
	}
	
	public void setParam(String param) {
		this.param = param;
	}
	
	public int getLastCleanDate() {
		return lastCleanDate;
	}
	
	public void setLastCleanDate(int lastCleanDate) {
		this.lastCleanDate = lastCleanDate;
	}
	
	
	public void alterNum(String key , int num){
		if(paramMap == null){
			initParameters();
		}
		if(this.needToCleanRecord()){
			this.cleanRecord();
		}
		
		Integer value = paramMap.get(key);        // 这里初始化的时候为0
		if (value == null) {
			value = Integer.valueOf(0);
		}
		paramMap.put(key, num + value);
		this.paramToString();
	}
	
	/**
	 * 清除所有数据
	 */
	private void cleanRecord() {
		this.lastCleanDate = Calendar.getInstance().get(Calendar.DATE);
		if(this.paramMap == null){
			initParameters();
		}
		Set<String> keySet = this.paramMap.keySet();
		for(String key : keySet){
			paramMap.put(key, 0);
		}
	}

	/**
	 * 是否需要清除数据
	 * 
	 * @return
	 */
	private boolean needToCleanRecord() {
		return lastCleanDate != Calendar.getInstance().get(Calendar.DATE);
	}

	
	public void paramToString(){
		if(paramMap == null) return ;
		StringBuilder builder = new StringBuilder() ;
		Set<String> keySet = paramMap.keySet();
		for(String key : keySet){
			builder.append(key).append(Splitable.ATTRIBUTE_SPLIT).append(paramMap.get(key)).append(Splitable.ELEMENT_DELIMITER);
		}
		this.param = builder.toString() ;
	}
	
	
	private void initParameters(){
		paramMap = new HashMap<String, Integer>();
		if(StringUtils.isBlank(param)) return ;
		String [] paramsArrays = param.split(Splitable.ELEMENT_SPLIT);
		for(String params : paramsArrays){
			String [] keyValue = params.split(Splitable.ATTRIBUTE_SPLIT);
			paramMap.put(keyValue[0], Integer.parseInt(keyValue[1]));
		}
	}
	
	
	public Integer intValue(String name) {
		if(this.paramMap == null){
			initParameters() ;
		}
		Integer usedNum = paramMap.get(name);
		if(usedNum == null) {
			usedNum = Integer.valueOf(0) ;
		}
		return usedNum;
	}
	
	
	/**
	 * 初始化VIP的福利
	 * 
	 * @param
	 */
	public void initVipParams() {
//		alterNum(RecieveBlessExp.name(), 0);
		alterNum(FlyingShoes.name(), 0);
//		alterNum(ReceiveTaskReward.name(), 0);
//		alterNum(ReceiveHorseReward.name(), 0);
		alterNum(ReceiveVipGift.name(), 0);
	}
	
	
	/**
	 * 修改VIP到期时间
	 * 
	 * @param vipLevel      VIP等级
	 * @param millis        到期时间毫秒
	 */
	public boolean alterEndTime(int vipLevel, long millis) {
		if(this.vipLevel == vipLevel) {
			this.vipEndTime += millis;
			return true;
		}
		if(this.vipLevel < vipLevel) {
			long curSec = System.currentTimeMillis();
			this.vipLevel = vipLevel;
			this.vipEndTime = curSec + millis;
			return true;
		}
		return false;
	}
	
	
	/**
	 * (判断VIP是否到期)VIP到期, 清除玩家VIP数据
	 * 
	 * @return
	 */
	public boolean clearVipData() {
		long curSec = System.currentTimeMillis();               
		long sec = TimeConstant.ONE_MINUTE_MILLISECOND;         // 允许误差1分钟
		
		if (vipEndTime <= curSec + sec && vipLevel > 0) {
			this.param = "";
			this.paramMap = null;
			this.vipLevel = 0;
			return true;
		} else if (this.needToCleanRecord()) {
			this.cleanRecord();
		}
		
		return false;
	}
	
	
	public static PlayerVip valueOf(long id) {
		PlayerVip vip = new PlayerVip();
		vip.id = id ;
		return vip;
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
		PlayerVip other = (PlayerVip) obj;
		return id != null && other.id != null && id.equals(other.id);
	}
	
}
