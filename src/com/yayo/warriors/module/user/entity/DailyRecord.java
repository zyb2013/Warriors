package com.yayo.warriors.module.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;

@Entity
@Table(name="dailyRecord")
public class DailyRecord extends BaseModel<Long> {
	private static final long serialVersionUID = -8595869426952665352L;
	
	/** 角色id */
	@Id
	@Column(name = "playerId")
	private Long id;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public static DailyRecord valueOf(long playerId){
		DailyRecord dailyRecord = new DailyRecord();
		dailyRecord.id = playerId;
		return dailyRecord; 
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
		DailyRecord other = (DailyRecord) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
}
