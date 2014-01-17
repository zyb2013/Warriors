package com.yayo.warriors.module.pet.model;

public class PropsEgg {

	private long propsId;
	
	private int number;
	
	public static PropsEgg valueOf(long propsId,int number){
		PropsEgg egg = new PropsEgg();
		egg.propsId = propsId;
		egg.number = number;
		return egg;
	} 

	public long getPropsId() {
		return propsId;
	}

	public void setPropsId(long propsId) {
		this.propsId = propsId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "PropsEgg [propsId=" + propsId + ", number=" + number + "]";
	}
}
