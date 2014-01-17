package com.yayo.warriors.module.pack.entity;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.pack.entity.Backpack.PK;

/**
 * 便捷栏位信息
 * 
 * @author Hyint
 */
@Entity
@Table(name="backpack")
public class Backpack extends BaseModel<PK>  {
	private static final long serialVersionUID = 3977067273797115918L;

	/**
	 * 角色背包的主键信息
	 * 
	 * @author Hyint
	 */
	@Embeddable
	public static class PK implements Serializable, Comparable<PK> {
		private static final long serialVersionUID = -2678770172695212292L;
		/** 角色ID */
		private Long playerId = 0L;
		/** 背包的位置类型 */
		private Integer packageType = 0;

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public int getPackageType() {
			return packageType;
		}

		public void setPackageType(int packageType) {
			this.packageType = packageType;
		}

		
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + packageType;
			result = prime * result + (int) (playerId ^ (playerId >>> 32));
			return result;
		}

		/**
		 * 构建PK对象
		 * 
		 * @param playerId			角色ID
		 * @param packageType		背包位置类型
		 * @return
		 */
		public static PK valueOf(long playerId, int packageType) {
			PK pk = new PK();
			pk.playerId = playerId;
			pk.packageType = packageType;
			return pk;
		}
		
		
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj == null) {
				return false;
			} else if (getClass() != obj.getClass()) {
				return false;
			}

			PK other = (PK) obj;
			return this.packageType.intValue() == other.packageType.intValue()
				&& this.playerId.longValue() == other.playerId.longValue();
		}

		
		public String toString() {
			return "PK [playerId=" + playerId + ", packageType=" + packageType + "]";
		}

		
		public int compareTo(PK o) {
			int playerIdComp = playerId.compareTo(o.playerId);
			if(playerIdComp < 0) {
				return playerIdComp;
			} else {
				return packageType.compareTo(o.packageType) > 0 ? 1 : 0;
			}
		}
	}
	
	@Id
	@EmbeddedId
	private PK id;
	
	@Lob
	private byte[] packageInfo;

	
	public PK getId() {
		return this.id;
	}

	
	public void setId(PK id) {
		this.id = id;
	}

	public byte[] getPackageInfo() {
		return packageInfo;
	}

	public void setPackageInfo(byte[] packageInfo) {
		this.packageInfo = packageInfo;
	}

	
	public String toString() {
		return "PackagePosition [id=" + id + ", packageInfo=" + Arrays.toString(packageInfo) + "]";
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + Arrays.hashCode(packageInfo);
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}else if (!super.equals(obj)){
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		Backpack other = (Backpack) obj;
		return id != null && other.id != null && Arrays.equals(packageInfo, other.packageInfo);
	}
	
	public static Backpack valueOf(PK pk, byte[] backpackPosition) {
		Backpack packagePosition = new Backpack();
		packagePosition.id = pk;
		packagePosition.packageInfo = backpackPosition;
		return packagePosition;
				
	}
	
}
