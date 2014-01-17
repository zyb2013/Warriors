package com.yayo.warriors.module.active.verify;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.warriors.basedb.model.ActiveNoticeConfig;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.module.active.entity.OperatorActive;
import com.yayo.warriors.module.active.manager.ActiveOperatorManager;
import com.yayo.warriors.module.active.rule.ActiveNoticeType;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.type.IndexName;


/**
 * 运营活动公告 
 * @author liuyuhua
 */
@Component
public class ActiveNoticeService {
	
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private ActiveOperatorManager activeOperatorManager;
	
	/**
	 * 活动公告
	 */
	public void actionActiveNotice() {
		List<OperatorActive> actives = activeOperatorManager.getAllActives();
		Iterator<OperatorActive> it = actives.iterator();
		while(it.hasNext()){
			OperatorActive active = it.next();
			this.beforNotice(active);
			this.startNotice(active);
			this.loopNotice(active);
			this.willCloseNotice(active);
			this.closedNotice(active);
		}
	}
	
	/**
	 * 循环公告
	 * @param active
	 */
	private void loopNotice(OperatorActive active){
		if(active == null){
			return;
		}
		
		if(!active.isOpened()){
			return;
		}
		
		long currentTime = System.currentTimeMillis();//当前时间
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return;
		}
		
		int activeId = active.getActiveBaseId(); //活动基础ID
		List<ActiveNoticeConfig> configs = this.getNoticeConfig(activeId, ActiveNoticeType.CLOSED_ACTIVE);
		for(ActiveNoticeConfig config : configs){
			int broadTime = config.getTime() * 1000; //转换成毫秒
			long startTime = active.getStartTime();//活动 开始时间
			long surplus = currentTime - startTime;//剩余时间
			if(surplus < 0){
				continue;
			}
			
			long time = surplus % broadTime;
			if(time == 0 || (time > 0 && time <= 3000)){
				int noticeId = config.getNoticeId();
				Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
				resultMap.put(NoticeRule.name, active.getTitle());
				NoticePushHelper.pushNotice(noticeId, NoticeType.HONOR, resultMap, 1);// 推送世界荣誉公告
			}
		}
	}
	
	/**
	 * 已经结束
	 * @param active
	 */
	private void closedNotice(OperatorActive active){
		if(active == null){
			return;
		}
		
		if(active.isOpened()){
			return;
		}
		
		long currentTime = System.currentTimeMillis();//当前时间
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return;
		}
		
		int activeId = active.getActiveBaseId(); //活动基础ID
		List<ActiveNoticeConfig> configs = this.getNoticeConfig(activeId, ActiveNoticeType.CLOSED_ACTIVE);
		for(ActiveNoticeConfig config : configs){
			long endTime = active.getEndTime();//活动 结束时间
			long surplus = currentTime - endTime;//剩余时间
			if(surplus < 0){
				continue;
			}
			
			surplus = surplus / 1000L;
			if(surplus > 0 && surplus <= 3000){
				int noticeId = config.getNoticeId();
				Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
				resultMap.put(NoticeRule.name, active.getTitle());
				NoticePushHelper.pushNotice(noticeId, NoticeType.HONOR, resultMap, 1);// 推送世界荣誉公告
			}
		}
	}
	
	/**
	 * 即将结束
	 * @param active   运营活动
	 */
	private void willCloseNotice(OperatorActive active){
		if(active == null){
			return;
		}
		
		if(!active.isOpened()){ //已经开启的活动无需再迭代
			return;
		}
		
		long currentTime = System.currentTimeMillis();//当前时间
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return;
		}

		int activeId = active.getActiveBaseId(); //活动基础ID
		List<ActiveNoticeConfig> configs = this.getNoticeConfig(activeId, ActiveNoticeType.WILL_CLOSE_ACTIVE);
		for(ActiveNoticeConfig config : configs){
			int broadTime = config.getTime() * 1000; //转换成毫秒
			long endTime = active.getEndTime();      //活动 结束时间
			long surplus = currentTime - endTime;    //剩余时间
			if(surplus < 0){
				continue;
			}
			
			long time = surplus - broadTime;//广播时间
			if(time > 0 && time <= 2500){
				int noticeId = config.getNoticeId();
				Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
				resultMap.put(NoticeRule.name, active.getTitle());
				NoticePushHelper.pushNotice(noticeId, NoticeType.HONOR, resultMap, 1);// 推送世界荣誉公告
			}
			
		}
	}
	
	
	/**
	 * 活动开启公告
	 * @param active   运营活动
	 */
	private void startNotice(OperatorActive active){
		if(active == null){
			return;
		}
		
		if(!active.isOpened()){ //已经开启的活动无需再迭代
			return;
		}
		
		long currentTime = System.currentTimeMillis();//当前时间
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return;
		}
		
		int activeId = active.getActiveBaseId(); //活动基础ID
		List<ActiveNoticeConfig> configs = this.getNoticeConfig(activeId, ActiveNoticeType.BEGIN_ACTIVE);
		for(ActiveNoticeConfig config : configs){
			long startTime = active.getStartTime();  //活动开始时间
			long surplus = Math.abs(currentTime - startTime);  //剩余时间
			if(surplus > 0 && surplus <= 3000){
				int noticeId = config.getNoticeId();
				Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
				resultMap.put(NoticeRule.name, active.getTitle());
				NoticePushHelper.pushNotice(noticeId, NoticeType.HONOR, resultMap, 1);// 推送世界荣誉公告
			}
		}
	}
	
	/**
	 * 活动开启之前公告
	 * @param active   运营活动
	 */
	private void beforNotice(OperatorActive active){
		if(active == null){
			return;
		}
		
		if(active.isOpened()){ //已经开启的活动无需再迭代
			return;
		}
		
		long currentTime = System.currentTimeMillis();//当前时间
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return;
		}
		
		int activeId = active.getActiveBaseId(); //活动基础ID
		List<ActiveNoticeConfig> configs = this.getNoticeConfig(activeId, ActiveNoticeType.BEFORE_ACTIVE);
		for(ActiveNoticeConfig config : configs){
			int broadTime = config.getTime() * 1000; //转换成毫秒
			long startTime = active.getStartTime();  //活动开始时间
			long surplus = currentTime - startTime;  //剩余时间
			if(surplus < 0){
				continue;
			}
			
			long time = surplus - broadTime;//广播时间
			if(time > 0 && time <= 2500){
				int noticeId = config.getNoticeId();
				Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
				resultMap.put(NoticeRule.name, active.getTitle());
				NoticePushHelper.pushNotice(noticeId, NoticeType.HONOR, resultMap, 1);// 推送世界荣誉公告
			}
		}
		
	}
	
	
	/**
	 * 获取运营活动公告配置
	 * @param activeId   活动ID
	 * @param type       公告类型
	 * @return {@link    }
	 */
	private List<ActiveNoticeConfig> getNoticeConfig(int activeId,int type){
		return resourceService.listByIndex(IndexName.ACTIVE_OPERATOR_NOTICE, ActiveNoticeConfig.class, activeId , type);
	}

}
