package com.yayo.warriors.module.alliance.model;

/**
 * 帮派排序
 * @author liuyuhua
 */
public class AllianceRanking implements Comparable<AllianceRanking>{
	
	/** 帮派ID*/
	private long allianceId;
	
	/** 帮派等级*/
	private Integer level;

	public long getAllianceId() {
		return allianceId;
	}

	public void setAllianceId(long allianceId) {
		this.allianceId = allianceId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	/**
	 * 构造方法
	 * @param allianceId    帮派的ID
	 * @param level         帮派等级
	 * @return {@link AllianceRanking} 帮派排序对象
	 */
	public static AllianceRanking valueOf(long allianceId,int level){
		AllianceRanking ranking = new AllianceRanking();
		ranking.allianceId = allianceId;
		ranking.level = level;
		return ranking;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (allianceId ^ (allianceId >>> 32));
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AllianceRanking other = (AllianceRanking) obj;
		if (allianceId != other.allianceId)
			return false;
		return true;
	}

	
	public String toString() {
		return "AllianceRanking [allianceId=" + allianceId + ", level=" + level
				+ "]";
	}

	
	public int compareTo(AllianceRanking o) {
		if(o == null) {
			return -1;
		}
		Integer targetLevel = o.level;
		return targetLevel.compareTo(this.level);
	}

}
