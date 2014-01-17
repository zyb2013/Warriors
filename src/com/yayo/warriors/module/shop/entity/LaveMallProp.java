package com.yayo.warriors.module.shop.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;

@Entity
@Table(name="laveMallProp")
public class LaveMallProp extends BaseModel<Integer> {
	
	private static final long serialVersionUID = -2621619162690993468L;
	
	@Id
	@Column(name="mallId")
	private Integer id;
	
	private int count;

	public static LaveMallProp valueOf(int mallId, int count){
		LaveMallProp mallPropsEntity = new LaveMallProp();
		mallPropsEntity.id = mallId;
		mallPropsEntity.count = count;
		return mallPropsEntity;
	}
	
	public Integer getId() {
		return id;
	}
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void addCount(int count) {
		this.count += count;
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
		if (this == obj) {
			return true;
		} else if (!super.equals(obj)) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		LaveMallProp other = (LaveMallProp) obj;
		return id != null && other.id != null && !id.equals(other.id);
	}
}
