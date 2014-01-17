package com.yayo.warriors.module.syscfg.manager;

import java.util.Date;
import java.util.List;

import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.syscfg.entity.SystemConfig;
import com.yayo.warriors.module.syscfg.type.ConfigType;


public interface SystemConfigManager {

	SystemConfig getSystemConfig(ConfigType configType);
	
	Date getFirstOpenTime();
	
	boolean updateSystemConfig(ConfigType id, String info);

	int blockIP(List<String> ipList, int op);
	
	boolean isIpBlocked(String ip);
}
