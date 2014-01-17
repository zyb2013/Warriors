package com.yayo.warriors.module.syscfg.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.syscfg.type.ConfigType;

@Entity
@Table(name="systemConfig")
public class SystemConfig extends BaseModel<ConfigType> implements Serializable {
	private static final long serialVersionUID = 3097605100505878132L;
	
	@Id
	@Enumerated
	private ConfigType id;
	
	@Lob
	private String info = "";

	@Override
	public ConfigType getId() {
		return this.id;
	}

	@Override
	public void setId(ConfigType id) {
		this.id = id;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public static SystemConfig valueOf(ConfigType id) {
		SystemConfig systemConfig = new SystemConfig();
		systemConfig.id = id;
		systemConfig.info = id.getInfo();
		return systemConfig;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((info == null) ? 0 : info.hashCode());
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
		
		SystemConfig other = (SystemConfig) obj;
		return this.id.ordinal() == other.id.ordinal();
	}

	@Override
	public String toString() {
		return "SystemConfig [id=" + id + ", info=" + info + "]";
	}
	
	
	
}
