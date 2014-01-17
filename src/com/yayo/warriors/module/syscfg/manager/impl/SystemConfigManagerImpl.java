package com.yayo.warriors.module.syscfg.manager.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.syscfg.entity.SystemConfig;
import com.yayo.warriors.module.syscfg.manager.SystemConfigManager;
import com.yayo.warriors.module.syscfg.type.ConfigType;


@Service
public class SystemConfigManagerImpl extends CachedServiceAdpter implements SystemConfigManager {
	@Autowired
	private DbService dbService;
	
	@Autowired
	@Qualifier("GAME_SERVER_FIRST_OPEN")
	private String GAME_SERVER_FIRST_OPEN;		
	
	
	private Set<String> blockIps = null;
	
	private Date firstOpenTime = null;
	
	
	public SystemConfig getSystemConfig(ConfigType id) {
		return id == null ? null : this.get(id, SystemConfig.class);
	}
	
	
	public Date getFirstOpenTime() {
		if(firstOpenTime == null){
			if( StringUtils.isNotBlank(this.GAME_SERVER_FIRST_OPEN) ){
				firstOpenTime = DateUtil.string2Date(this.GAME_SERVER_FIRST_OPEN, DatePattern.PATTERN_YYYY_MM_DD);
			}
		}
		return firstOpenTime;
	}

	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == SystemConfig.class) {
			SystemConfig systemConfig = commonDao.get((ConfigType)id, SystemConfig.class);
			if(systemConfig != null) {
				return (T) systemConfig;
			}
			
			try {
				systemConfig = SystemConfig.valueOf((ConfigType) id);
				commonDao.save(systemConfig);
				return (T) systemConfig;
			} catch (Exception e) {
				return null;
			}
		}
		
		return super.getEntityFromDB(id, clazz);
	}

	
	
	public boolean updateSystemConfig(ConfigType configType, String info) {
		SystemConfig systemConfig = this.getSystemConfig(configType);
		if(systemConfig == null) {
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(systemConfig);
		try {
			lock.lock();
			systemConfig.setInfo(StringUtils.defaultIfBlank(info, ""));
		} finally {
			lock.unlock();
		}

		dbService.submitUpdate2Queue(systemConfig);
		return true;
	}

	
	public int blockIP(List<String> ipList, int op) {
		if(op < 1 || op > 2 || ipList == null || ipList.isEmpty()){
			return CommonConstant.INPUT_VALUE_INVALID;
		}
		
		SystemConfig systemConfig = getSystemConfig(ConfigType.BLACKLIST_IP);
		ChainLock lock = LockUtils.getLock(systemConfig);
		try {
			lock.lock();
			if(op == 1) {		
				this.blockIps.addAll( ipList );
			} else if (op == 2){	
				this.blockIps.removeAll( ipList );
			}
			
			StringBuilder sb = new StringBuilder();
			for(Iterator<String> iterator= this.blockIps.iterator(); iterator.hasNext(); ){
				sb.append(Splitable.BETWEEN_ITEMS).append(iterator.next());
			}
			if(sb.length() > 0){
				sb.deleteCharAt(0);
			}
			systemConfig.setInfo( sb.toString() );
		} finally {
			lock.unlock();
		}
		updateSystemConfig(ConfigType.BLACKLIST_IP, systemConfig.getInfo() );
		
		return CommonConstant.SUCCESS;
	}

	
	public boolean isIpBlocked(String ip) {
		this.checkBlockIpSet();
		return StringUtils.isNotBlank(ip) && this.blockIps.contains( ip );
	}

	private void checkBlockIpSet() {
		if(this.blockIps != null) {
			return;
		}
		
		this.blockIps = new ConcurrentHashSet<String>();
		SystemConfig systemConfig = getSystemConfig(ConfigType.BLACKLIST_IP);
		if(systemConfig == null || StringUtils.isBlank(systemConfig.getInfo())) {
			return;
		}
	
		ChainLock lock = LockUtils.getLock(systemConfig);
		try {
			lock.lock();
			for(String str : systemConfig.getInfo().split(Splitable.BETWEEN_ITEMS)){
				if(StringUtils.isNotBlank(str)){
					this.blockIps.add(str.trim());
				}
			}
		} finally {
			lock.unlock();
		}
	}
}
