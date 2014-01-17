package com.yayo.warriors.module.notice.vo;

import java.io.Serializable;
import java.util.Map;

import com.yayo.warriors.module.notice.type.NoticeType;

public class NoticeVo implements Serializable, Comparable<NoticeVo>{

	private static final long serialVersionUID = -4559359879091587695L;
	
	private long playerId ;
	private int playerLevel ;
	private String playerName ;
	private String title ;
	private String contents ;
	private int contentIndex ;
	
	private long startTime ;
	private long endTime ;
	
	private int interval ;
	private int noticeType ;
	private int speed ;
	private int priority = 1;
	
	private Map<String,Object> params ;
	

	public static NoticeVo valueOf(int noticeID, NoticeType noticeType, Map<String, Object> params, int priority) {
		NoticeVo vo = new NoticeVo();
		vo.contentIndex = noticeID;
		vo.noticeType = noticeType.ordinal();
		vo.params = params;
		vo.priority = priority;
		return vo;
	}
	

	public static NoticeVo valueOf(int noticeID, int priority, Map<String, Object> params) {
		NoticeVo vo = new NoticeVo();
		vo.contentIndex = noticeID;
		vo.priority = priority;
		vo.params = params;
		vo.noticeType = NoticeType.HONOR.ordinal();
		return vo;
	}


	public boolean isSimpleNotice() {
		return playerId != 0;
	}

	
	public long getPlayerId() {
		return this.playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public int getPlayerLevel() {
		return playerLevel;
	}

	public String getTitle() {
		return title;
	}

	public String getContents() {
		return contents;
	}

	public int getContentIndex() {
		return contentIndex;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public int getInterval() {
		return interval;
	}

	public int getNoticeType() {
		return noticeType;
	}

	public int getSpeed() {
		return speed;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void setPlayerLevel(int playerLevel) {
		this.playerLevel = playerLevel;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public void setContentIndex(int contentIndex) {
		this.contentIndex = contentIndex;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setNoticeType(int noticeType) {
		this.noticeType = noticeType;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	
	
	public int compareTo(NoticeVo o) {
		if (this.priority < o.priority) {
			return -1;
		} else {
			return 1;
		}
	}

}
