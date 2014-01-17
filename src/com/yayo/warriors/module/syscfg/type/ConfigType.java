package com.yayo.warriors.module.syscfg.type;

public enum ConfigType {
	
	BRANCHING("1"), 
	ALLOW_ACCESS_IP("127.0.0.1,192.168.*.*,*.*.*.*"),
	RANK_OPEN("0"),
	BLACKLIST_IP(""),
	INDULGE_OPEN(""),
	LIMIT_PLAYER_MAP(""),
	MAX_BRANCHING_MEMBERS(""),
	MAX_BRANCHING("");
	private String info;
	
	ConfigType(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
}
