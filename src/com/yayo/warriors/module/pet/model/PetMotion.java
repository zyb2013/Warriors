package com.yayo.warriors.module.pet.model;


public class PetMotion{
	
	private long petId ;
	
	private int x;
	
	private int y;
	

	public static PetMotion valueOf(long petId){
		PetMotion petMotion = new PetMotion();
		petMotion.petId = petId;
		return petMotion;
	}
	

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		PetMotion other = (PetMotion) obj;
		if (petId != other.petId)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PetMotion [petId=" + petId + ", x=" + x + ", y=" + y + "]";
	}
	
}
