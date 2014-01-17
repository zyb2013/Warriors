package com.yayo.warriors.module.alliance.model;

/**
 * 转移帮派
 * <per>该对象用于玩家确认的时候用来确定的回执</per>
 * @author liuyuhua
 */
public class Devolve {
	/** 申请者的ID*/
	private long playerId;
	
	/**
	 * 构造函数
	 * @param playerId 玩家的ID
	 * @return {@link Apply} 申请对象
	 */
	public static Devolve valueOf(long playerId){
		Devolve devolve = new Devolve();
		devolve.playerId = playerId;
		return devolve;
	}
	
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Devolve other = (Devolve) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Devolve [playerId=" + playerId + "]";
	}
	
	
	
}
