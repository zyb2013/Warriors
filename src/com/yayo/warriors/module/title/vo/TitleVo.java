package com.yayo.warriors.module.title.vo;

import java.io.Serializable;

import com.yayo.warriors.basedb.model.TitleDictionary;
import com.yayo.warriors.module.title.type.TitleState;

public class TitleVo implements Serializable {
	private static final long serialVersionUID = 6865841284681313961L;

	/** 称号编号 */
	private int titleId;
	
	/** 称号状态. {@link TitleState} */
	private int state = TitleState.HAVENOT.ordinal();
	
	
	public TitleVo(TitleDictionary titleConfig , int state){
		if(titleConfig != null) {
			this.state = state;
			this.titleId = titleConfig.getId() ;
		} else {
			this.titleId = 0 ;
			this.state = state;
		}
	}

	public int getTitleId() {
		return titleId;
	}

	public int getState() {
		return state;
	}

	public void setTitleId(int titleId) {
		this.titleId = titleId;
	}

	public void setState(int state) {
		this.state = state;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + titleId;
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
		TitleVo other = (TitleVo) obj;
		if (titleId != other.titleId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TitleVo [titleId=" + titleId + ", state=" + state + ", additional=" + "]";
	}
}
