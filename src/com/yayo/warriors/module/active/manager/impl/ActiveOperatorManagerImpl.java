package com.yayo.warriors.module.active.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceListener;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.warriors.basedb.model.ActiveOperatorConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorExChangeConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorLevelConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorRankConfig;
import com.yayo.warriors.module.active.dao.ActiveOperatorDao;
import com.yayo.warriors.module.active.entity.OperatorActive;
import com.yayo.warriors.module.active.entity.PlayerActive;
import com.yayo.warriors.module.active.manager.ActiveOperatorManager;
import com.yayo.warriors.module.active.model.ActiveDrop;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 运营活动管理类 
 * @author liuyuhua
 */
@Component
public class ActiveOperatorManagerImpl extends CachedServiceAdpter implements ActiveOperatorManager ,ResourceListener {
	
	@Autowired
	private ActiveOperatorDao dao;
	@Autowired
	private ResourceService resourceService;

	/** 运营活动集合*/
	private final List<ActiveDrop> ACTIVE_DROPS = Collections.synchronizedList(new ArrayList<ActiveDrop>(5));         //活动掉落
	private final List<OperatorActive> ACTIVE_LIST = Collections.synchronizedList(new ArrayList<OperatorActive>(5));  //缓存对象(用于排序返回客户端)
	
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	public PlayerActive getPlayerActive(PlayerBattle battle) {
		if(battle == null){
			return null;
		}
		
		long playerId = battle.getId();
		return this.get(playerId, PlayerActive.class);
	}
	
	
	public boolean createOrUpdateActive(Collection<OperatorActive> actives) {
		if(actives == null || actives.isEmpty()){
			return false;
		}
		
		for(OperatorActive active : actives){
			try {
				if(ACTIVE_LIST.contains(active)){
					dao.update(active);
					ACTIVE_LIST.remove(active);
					ACTIVE_LIST.add(active);
					
				}else{
					dao.save(active);
					ACTIVE_LIST.add(active);
				}
				
			} catch (Exception e) {
				logger.error("更新或保存运营活动错误e:{}",e);
			}
		}
		
		Collections.sort(ACTIVE_LIST);//排序
		resetDroplist();//充值活动掉落列表
		return true;
	}

	
	public List<OperatorActive> getClientShowActives() {
		List<OperatorActive> result = new ArrayList<OperatorActive>(3);
		if(ACTIVE_LIST == null || ACTIVE_LIST.isEmpty()){
			return result;
		}
		
		long currentTime = System.currentTimeMillis();
		for(OperatorActive active : ACTIVE_LIST){
			if(!active.isOpened()){
				continue;
			}
			
			if(currentTime >= active.getStartTime() && currentTime <= active.getLostTime()){
				result.add(active);
			}
			
		}
		
		return result;
	}

	
	public List<Long> deleteActive(List<Long> activeIds) {
		List<Long> result = new ArrayList<Long>();
		if(activeIds == null || activeIds.isEmpty()){
			return result;
		}
		
		for(long id : activeIds){
			if(!isExist(id)){
				continue;
			}
			
			boolean falg = dao.deleteActive(id);
			if(falg == false){
				continue;
			}
			result.add(id);
		}
		
		Iterator<OperatorActive> it = ACTIVE_LIST.iterator();
		while(it.hasNext()){
			OperatorActive active = it.next();
			if(active != null && result.contains(active.getId())){
				it.remove();
			}
		}
		
		Collections.sort(ACTIVE_LIST);
		resetDroplist();
		return result;
	}
	
	
	/**
	 * 是否存在该活动
	 * @param id                活动ID
	 * @return {@link Boolean}  true 存在 false 不存在
	 */
	private boolean isExist(long id){
		for(OperatorActive active : ACTIVE_LIST){
			if(active.getId() == id){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 运营活动掉落列表
	 */
	private void resetDroplist() {
		Iterator<OperatorActive> it = ACTIVE_LIST.iterator();
		List<ActiveDrop> activeDrops = new ArrayList<ActiveDrop>(2);
		while(it.hasNext()){
			OperatorActive active = it.next();
			ActiveOperatorConfig config = active.getOperatorConfig();
			if(config != null && !config.getDorpList().isEmpty()){
				if(!active.isOpened()){
					continue;
				}
				activeDrops.add(ActiveDrop.valueOf(active.getStartTime(), active.getEndTime(), config.getDorpList()));
			}
		}
		
		ACTIVE_DROPS.clear();
		ACTIVE_DROPS.addAll(activeDrops);
	}
	
	
	public ActiveOperatorConfig getActiveOperatorConfig(int activeId) {
		return resourceService.get(activeId, ActiveOperatorConfig.class);
	}
	
	
	public ActiveOperatorRankConfig getActiveOperatorRankConfig(int activeId) {
		return resourceService.get(activeId, ActiveOperatorRankConfig.class);
	}

	
	public ActiveOperatorLevelConfig getActiveOperatorLevelConfig(int activeId) {
		return resourceService.get(activeId, ActiveOperatorLevelConfig.class);
	}
	
	
	public ActiveOperatorExChangeConfig getActiveOperatorExChangeConfig(int activeId) {
		return resourceService.get(activeId, ActiveOperatorExChangeConfig.class);
	}
	
	
	public OperatorActive getOperatorActive(long id){
		for(OperatorActive active : ACTIVE_LIST){
			if(active.getId() == id){
				return active;
			}
		}
		return null;
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == PlayerActive.class) {
			PlayerActive playerActive = dao.get(id, PlayerActive.class);
			if(playerActive == null){
				try {
					playerActive = PlayerActive.valueOf((Long)id);
					dao.save(playerActive);
				} catch (Exception e) {
					playerActive = null;
					logger.error("角色:[{}] 创建角色活动信息异常:{}", id, e);
				}
			}
			return (T) playerActive;
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public List<OperatorActive> getAllActives() {
		return ACTIVE_LIST;
	}

	
	public List<Integer> getDropList() {
		List<Integer> dorpList = new ArrayList<Integer>(1);
		Iterator<ActiveDrop> it = ACTIVE_DROPS.iterator();
		while(it.hasNext()){
			ActiveDrop activeDrop = it.next();
			if(activeDrop.canDrop()){
				dorpList.addAll(activeDrop.getDorplist());
			}
		}
		return dorpList;
	}

	
	public void onBasedbReload() {
		List<OperatorActive> actives = dao.getActives();
		if(actives != null && !actives.isEmpty()){
			for(OperatorActive active : actives){
				ActiveOperatorConfig config = this.getActiveOperatorConfig(active.getActiveBaseId());
				active.setOperatorConfig(config);
			}
			
			ACTIVE_LIST.addAll(actives);
			Collections.sort(ACTIVE_LIST);//排序
			resetDroplist();//重置活动掉落列表
		}
	}

}
