package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.type.IndexName;

/**
 * 活动公告配置 
 * @author liuyuhua
 */
@Resource
public class ActiveNoticeConfig {
	
	/** 序号*/
	@Id
	private int id;
	
	/** 活动ID 参见,{@link ActiveOperatorConfig#getId()}*/
	@Index(name=IndexName.ACTIVE_OPERATOR_NOTICE, order = 0)
	private int activeId;
	
	/**  
	 * 类型
	 * <per>1 - 提前</per>
	 * <per>2 - 开始</per>
	 * <per>3 - 进行中</per>
	 * <per>4 - 即将结束</per>
	 * <per>5 - 结束</per>
	 * */
	@Index(name=IndexName.ACTIVE_OPERATOR_NOTICE, order = 1)
	private int type;
	
	/**
	 * 公告ID 参加,{@link NoticeID}
	 */
	private int noticeId;
	
	/**
	 * 时间
	 * 对应{@link ActiveNoticeConfig#type}
	 * */
	private int time;

	//Getter and Setter...
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActiveId() {
		return activeId;
	}

	public void setActiveId(int activeId) {
		this.activeId = activeId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(int noticeId) {
		this.noticeId = noticeId;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "ActiveNoticeConfig [id=" + id + ", activeId=" + activeId
				+ ", type=" + type + ", noticeId=" + noticeId + ", time="
				+ time + "]";
	}

}
