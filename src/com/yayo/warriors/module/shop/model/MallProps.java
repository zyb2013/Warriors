package com.yayo.warriors.module.shop.model;

import java.io.Serializable;

public class MallProps implements Serializable {
	
	private static final long serialVersionUID = -321563714110919365L;

	private int id;
	
	private long endTime;
	
	private int buyCount;
	
	private int laveCount;
	
	
	public static MallProps valueOf(int id, long endTime, int buyCount, int laveCount) {
		MallProps mallProps = new MallProps();
		mallProps.id = id;
		mallProps.endTime = endTime;
		mallProps.buyCount = buyCount;
		mallProps.laveCount = laveCount;
		return mallProps;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}

	public int getLaveCount() {
		return laveCount;
	}

	public void setLaveCount(int laveCount) {
		this.laveCount = laveCount;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		MallProps other = (MallProps) obj;
		return id == other.id;
	}

}
