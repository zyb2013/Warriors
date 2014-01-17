package com.yayo.warriors.module.props.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.type.GoodsType;

/**
 * 用户道具信息
 * 
 * @author Hyint
 */
@Entity
@Table(name="userProps")
public class UserProps extends BackpackEntry {
	private static final long serialVersionUID = 7066420537654833709L;
	
	/** 所属玩家 */
	private long playerId;
	
	/** 丢弃或者出售的时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date discardTime;

	/** 失效时间, 为null则为永久不失效 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiration = null;

	@Transient
	private transient volatile PropsConfig propsConfig;
	
	@Override
	public int getGoodsType() {
		return GoodsType.PROPS;
	}

	public Date getDiscardTime() {
		return discardTime;
	}

	public void setDiscardTime(Date discardTime) {
		this.discardTime = discardTime;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	
	public PropsConfig getPropsConfig() {
		if(this.propsConfig == null) {
			this.propsConfig = EquipHelper.getPropsConfig(this.getBaseId());
		}
		return this.propsConfig;
	}

	public void updatePropsConfig(PropsConfig propsConfig) {
		this.propsConfig = propsConfig;
	}

	@Override
	public void setBaseId(int baseId) {
		this.propsConfig = null;
		super.setBaseId(baseId);
	}

	/**
	 * 验证有效时间
	 * 
	 * @return {@link Boolean}	true-超过时效不可使用, false-可以使用
	 */
	public boolean isOutOfExpiration() {
		if(this.expiration != null) {
			return expiration.getTime() <= System.currentTimeMillis();
		}
		return false;
	}
	
	public boolean isSameExpiration(Date targetExpiration) {
		if((expiration == null && targetExpiration != null) 
		|| (expiration != null && targetExpiration == null)) {
			return false;
		}
				
		if(expiration != null && targetExpiration != null) {
			long addExpirateSecond = DateUtil.toSecond(expiration.getTime());
			long costExpirateSecond = DateUtil.toSecond(targetExpiration.getTime());
			return addExpirateSecond != costExpirateSecond;
		}
		return true;
	}
	
	public void increaseItemCount(int itemCount) {
		setCount(getCount() + itemCount);
	}
	
	public void decreaseItemCount(int itemCount) {
		setCount(getCount() - itemCount);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	
	/**
	 * 计算时效性时间
	 * 
	 * @param  expirateTime	 		效果时间
	 * @return {@link Boolean}		是否效果时间相同
	 */
	public boolean isExpirationEquals(Date expirateTime) {
		boolean result = expirateTime == null && this.expiration == null;
		if(expirateTime != null && this.expiration != null) {
			long expirateTimeSecond = DateUtil.toSecond(expirateTime.getTime());
			long currentExpirationSecond = DateUtil.toSecond(this.expiration.getTime());
			result = expirateTimeSecond == currentExpirationSecond;
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "UserProps [getBackpack()=" + getBackpack() + ", getDiscardTime()="
				+ getDiscardTime() + ", getPlayerId()=" + getPlayerId() + ", isBinding()="
				+ isBinding() + ", getExpiration()=" + getExpiration() + ", id=" + getId() 
				+ ", baseId=" + getBaseId() + ", count=" + getCount() + ", index=" + getIndex() + "]";
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
		
		UserProps other = (UserProps) obj;
		Long thisId = this.getId();
		Long otherId = other.getId();
		int thisBaseId = this.getBaseId();
		int otherBaseId = other.getBaseId();
		return thisId != null && otherId != null 
			&& thisId.equals(otherId) && thisBaseId == otherBaseId 
			&& this.getGoodsType() == other.getGoodsType();
	}
	
	/**
	 * 构建用户道具对象
	 * 
	 * @param  playerId				用户ID
	 * @param  baseId				基础ID
	 * @param  count				道具数量
	 * @param  backpack				背包号
	 * @param  validTime			过期时间
	 * @param  bindingType			绑定类型
	 * @return {@link UserProps}	用户道具对象
	 */
	public static UserProps valueOf(long playerId, int baseId, int count, 
				  int backpack,	Date validTime, boolean bindingType) {
		UserProps userProps = new UserProps();
		userProps.setCount(count);
		userProps.setBaseId(baseId);
		userProps.playerId = playerId;
		userProps.setBackpack(backpack);
		userProps.setBinding(bindingType);
		userProps.setExpiration(validTime);
		return userProps;
	}
	
	/**
	 * 构建用户道具对象
	 * 
	 * @param  playerId				用户ID
	 * @param  baseId				基础ID
	 * @param  count				道具数量
	 * @param  backpack				背包号
	 * @param  validTime			过期时间
	 * @param  bindingType			绑定类型
	 * @return {@link UserProps}	用户道具对象
	 */
	public static UserProps valueOf(long playerId, int backpack, int count, PropsConfig propsConfig, boolean bindingType) {
		UserProps userProps = new UserProps();
		userProps.setCount(count);
		userProps.playerId = playerId;
		userProps.setBackpack(backpack);
		userProps.setBinding(bindingType);
		userProps.setBaseId(propsConfig.getId());
		userProps.setExpiration(propsConfig.getExpirateDate(true));
		return userProps;
	}
	
	
}
