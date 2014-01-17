package com.yayo.warriors.basedb.model;



import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/** 
 * 商城活动配置(限购)
 * 
 * @author huachaoping
 */
@Resource
public class MallActiveConfig implements Comparable<MallActiveConfig> {
	
	/** 主键 */
	@Id
	private int id;
	
	/** 活动周期,　从该活动开始的持续天数 */
	private int activeRound;
	
	/** 活动串 */
	private int serial;
	
	/** 活动顺序 */
	private int sequence;
	
	/** 是否唯一 (0-不唯一, 1-唯一)*/
	private int isOnly;
	
	/** 是否开启 (0-未开启, 1-开启)*/
	@Index(name=IndexName.MALL_ACTIVE_OPEN, order=0)
	private int isOpen;
	
	/** 持续天数:活动的持续天数, 以开服时间算...  */
	@JsonIgnore
	private int lastDay;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActiveRound() {
		return activeRound;
	}

	public void setActiveRound(int activeRound) {
		this.activeRound = activeRound;
	}

	public int getSerial() {
		return serial;
	}

	public void setSerial(int serial) {
		this.serial = serial;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getIsOnly() {
		return isOnly;
	}

	public void setIsOnly(int isOnly) {
		this.isOnly = isOnly;
	}

	public int getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}

	public int getLastDay() {
		return lastDay;
	}

	public void setLastDay(int lastDay) {
		this.lastDay = lastDay;
	}

	
	
	public int compareTo(MallActiveConfig o) {
		if (this.sequence > o.sequence) {
			return 1;
		} else if (this.sequence < o.sequence) {
			return -1;
		}
		return 0;
	}

	
}
