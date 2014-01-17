package com.yayo.warriors.module.onlines.manager.impl;

import static java.util.Calendar.SECOND;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.logger.log.RegStatiLogger;
import com.yayo.warriors.module.onlines.dao.OnlineStatisticDao;
import com.yayo.warriors.module.onlines.entity.CampOnline;
import com.yayo.warriors.module.onlines.entity.OnlineStatistic;
import com.yayo.warriors.module.onlines.entity.RegisterDetailStatistic;
import com.yayo.warriors.module.onlines.entity.RegisterStatistic;
import com.yayo.warriors.module.onlines.entity.UserLevelStatistic;
import com.yayo.warriors.module.onlines.manager.OnlineStatisticManager;
import com.yayo.warriors.module.onlines.model.UserLevelRecord;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Job;

/**
 * 在线统计管理接口
 * 
 * @author Hyint
 */
@Component
public class OnlineStatisticManagerImpl extends CachedServiceAdpter implements OnlineStatisticManager {

	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private OnlineStatisticDao onlineStatisticDao;
	@Autowired
	private UserManager userManager;
	
	/** 开服至今最大在线 */
	private int sinceMaxCount = 0;
	
	/** 总人数 */
	private AtomicInteger totalReg = new AtomicInteger(0);
	
	/** 豪杰人数 */
	private AtomicInteger knifeTotal = new AtomicInteger(0);
	
	/** 侠客人数 */
	private AtomicInteger swordTotal = new AtomicInteger(0);
	
	/** 注册记录map */
	ConcurrentHashMap<Integer, Integer> regStatMap = new ConcurrentHashMap<Integer, Integer>(7);
	
	/** 阵营在线 */
	AtomicInteger[] campOnlineArray = null;
	
	
	public OnlineStatistic getOnlineStatistic(long statisticId) {
		return get(statisticId, OnlineStatistic.class);
	}

	//统计模块NODE
	private static final String PREFIX = "STATISTIC_NODE_";
	private static final String RECORD_DATE = "_RECORD_DATE_";
	private static final String RECORD_TIME = "_RECORD_TIME_";
	
	/** 注册统计前缀 */
	private static final String PREFIX_REG = "REGISTER_STAT_NODE_";
	/** 阵营在线统计前缀 */
	private static final String PREFIX_CAMP_ONLINE = "CAMP_ONLINE_STAT_NODE_";
	
	/** 日志 */
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 定时统计在线玩家等级分布信息
	 * 数据每半小时一次
	 */
	protected void calculatePlayerLevelStatistic(){
		Calendar calendar = Calendar.getInstance();
		int minute = calendar.get(Calendar.MINUTE);
		if(minute % 30 == 0){
			UserLevelStatistic userLevelStatistic = this.actualUserLevelStatistic();
			if(userLevelStatistic != null){
				try {
					this.commonDao.save(userLevelStatistic);//保存当日数据
				} catch (Exception e) {
					logger.error("创建'定时统计在线玩家等级分布信息'保存异常:{}",e);
				}
			}
		}
	}
	
	
	/**
	 * 注册明细统计 每15分钟统计
	 */
	protected void registerDetailStatistic(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(SECOND, 0);//取整日历时间
		Date endDate = new Date(calendar.getTimeInMillis() - 1000); //结束时间
		long decreaseTime = calendar.getTimeInMillis() - 900000;//反算出之前的15分钟为开始时间
		Date startDate = new Date(decreaseTime);
		
		long loginCount = onlineStatisticDao.loadTotleLoginCount();
		int userCount = onlineStatisticDao.loadLoginCount4Time(startDate, endDate);
		int createCount = onlineStatisticDao.loadCreateCount4Time(startDate, endDate);
		RegisterDetailStatistic register = RegisterDetailStatistic.valueOf(loginCount,userCount,createCount,startDate, endDate);
		try {
			this.commonDao.save(register);
		} catch (Exception e) {
			logger.error("注册明细统计入库异常:{}",e);
		}
	}
	
	
	
	
	/**
	 * 定时计算在线统计
	 */
	@Scheduled(name = "定时统计在线信息", value = "0 0/15 * * * ?")
	protected void schedulerCalculateOnlineStatistic() {
		//在线统计
		int minCount = sessionManager.getMinOnlineCount();
		int maxCount = sessionManager.getMaxOnlineCount();
		if(maxCount > this.sinceMaxCount){
			this.sinceMaxCount = maxCount;
		}
		sessionManager.resetOnlineUserCount();
		
		//注册统计
		RegisterStatistic rs = null;
		synchronized (this.regStatMap) {
			rs = RegisterStatistic.valueOf(this.regStatMap);
			regStatMap.clear();
		}
		
		//阵营在线统计
		boolean isHour = Calendar.getInstance().get(Calendar.MINUTE) < 4;	//设置有4分钟的误差
		CampOnline campOnline = null;
		if(isHour){
			synchronized (this.campOnlineArray) {
				campOnline = CampOnline.valueOf(knifeTotal.get(), swordTotal.get(), campOnlineArray);
			}
		}
		
		//入库
		try {
			onlineStatisticDao.save(OnlineStatistic.valueOf(minCount, maxCount));
			cachedService.removeFromCommonCache(PREFIX);
		} catch (Exception e) {
			logger.error("保存在线统计出错：{}", e);
		}
		
		try {
			RegStatiLogger.regStati(rs);
			onlineStatisticDao.save(rs);
			cachedService.removeFromCommonCache(PREFIX_REG);
		} catch (Exception e) {
			logger.error("保存注册统计出错：{}", e);
		}
		
		if(campOnline != null){
			try {
				onlineStatisticDao.save(campOnline);
				cachedService.removeFromCommonCache(PREFIX_CAMP_ONLINE);
			} catch (Exception e) {
				logger.error("保存阵营在线统计出错：{}", e);
			}
		}
		
		
		try {
			registerDetailStatistic();//注册明细,每15分钟统计
		} catch (Exception e) {
			logger.error("注册明细统计 每15分钟统计：{}", e);
		}
		
		try {
			calculatePlayerLevelStatistic(); //计算玩家等级分布,每隔半小时一次
		} catch (Exception e) {
			logger.error("计算玩家等级分布,每隔半小时一次：{}", e);
		}
		
	}
	
	@SuppressWarnings("unused")
	@PostConstruct
	private void load(){
		//加载历史最大在线
		int maxOnline = onlineStatisticDao.getSinceMaxCount();
		if( maxOnline > this.sinceMaxCount ){
			this.sinceMaxCount = maxOnline;
		}
		
		//注册人数
		List<Object> registerCounts = onlineStatisticDao.loadRegisterCount();
		if(registerCounts != null){
			int total = 0;
			for(Object obj : registerCounts ){
				Object[] values = (Object[]) obj;
				int camp = Integer.valueOf(values[0].toString());
				int count = Integer.valueOf(values[1].toString());
				if(camp == Camp.KNIFE_CAMP.ordinal()){			//豪杰人数
					knifeTotal.set(count);
				} else if(camp == Camp.SWORD_CAMP.ordinal()){	//侠客人数
					swordTotal.set(count);
				}
				total += count;
			}
			totalReg.set(total);	//总人数 
		}
		
		//初始化阵营在线统计array
		getCampOnlineArray(Camp.NONE);
	}
	
	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  recordDate				统计的日期. 格式: 年-月-日
	 * @param  recordTime				统计的时间. 格式: 时:分
	 * @return {@link List}				在线统计对象列表
	 */
	
	public List<OnlineStatistic> listOnlineStatistic(String recordDate, String recordTime) {
		List<Long> idList = listOnlineStatisticId(recordDate, recordTime);
		return this.getEntityFromIdList(idList, OnlineStatistic.class);
	}

	/**
	 * 获得缓存SubKey
	 * 
	 * @param  recordDate		统计的日期
	 * @param  recordTime		统计的时间
	 * @return {@link String}	SubKey
	 */
	private String getSubKey(String recordDate, String recordTime) {
		StringBuilder builder = new StringBuilder();
		builder.append(RECORD_DATE);
		if(StringUtils.isNotBlank(recordDate)) {
			builder.append(recordDate);
		}
		builder.append(RECORD_TIME);
		if(StringUtils.isNotBlank(recordTime)) {
			builder.append(recordTime);
		}
		return builder.toString();
	}
	
	/**
	 * 列出在线统计信息
	 * 
	 * @param  recordDate		统计的日期
	 * @param  recordTime		统计的时间
	 * @return {@link String}	SubKey
	 */
	@SuppressWarnings("unchecked")
	private List<Long> listOnlineStatisticId(String recordDate, String recordTime) {
		String subKey = getSubKey(recordDate, recordTime);
		List<Long> list = (List<Long>) cachedService.getFromCommonCache(PREFIX, subKey);
		if(list == null) {
			list = onlineStatisticDao.listOnlineStatisticId(recordDate, recordTime);
			cachedService.put2CommonHashCache(PREFIX, subKey, list);
		}
		return list;
	}
	
	/**
	 * 列出在线统计信息
	 * 
	 * @param  recordDate		统计的日期
	 * @param  recordTime		统计的时间
	 * @return {@link String}	SubKey
	 */
	@SuppressWarnings("unchecked")
	private List<Long> listCampOnlineIds(String recordDate, String recordTime) {
		String subKey = getSubKey(recordDate, recordTime);
		List<Long> list = (List<Long>) cachedService.getFromCommonCache(PREFIX_CAMP_ONLINE, subKey);
		if(list == null) {
			list = onlineStatisticDao.listCampOnlineId(recordDate, recordTime);
			cachedService.put2CommonHashCache(PREFIX_CAMP_ONLINE, subKey, list);
		}
		return list;
	}
	
	/**
	 * 列出在线统计信息
	 * 
	 * @param  recordDate		统计的日期
	 * @param  recordTime		统计的时间
	 * @return {@link String}	SubKey
	 */
	@SuppressWarnings("unchecked")
	private List<Long> listRegisterStatisticId(String recordDate, String recordTime) {
		String subKey = getSubKey(recordDate, recordTime);
		List<Long> list = (List<Long>) cachedService.getFromCommonCache(PREFIX_REG, subKey);
		if(list == null) {
			list = onlineStatisticDao.listRegisterStatisticId(recordDate, recordTime);
			cachedService.put2CommonHashCache(PREFIX_REG, subKey, list);
		}
		return list;
	}

	
	public List<OnlineStatistic> listOnlineStatistic(List<String[]> dateTimes) {
		List<OnlineStatistic> list = new ArrayList<OnlineStatistic>();
		if(dateTimes != null){
			for(String[] dt : dateTimes){
				if(dt.length >= 2 ){
					List<OnlineStatistic> onlineStats = this.listOnlineStatistic(dt[0], dt[1]);
					list.addAll(onlineStats);
				}
			}
		}
		return list;
	}

	
	public List<OnlineStatistic> listOnlineStatisticByDate(String recordDate) {
		if(recordDate != null){
			List<Long> ids = listOnlineStatisticId(recordDate, "");
			return this.getEntityFromIdList(ids, OnlineStatistic.class);
		}
		return Collections.emptyList();
	}

	
	public List<CampOnline> listCampOnlineByDate(String recordDate) {
		if(recordDate != null){
			List<Long> ids = listCampOnlineIds(recordDate, "");
			return this.getEntityFromIdList(ids, CampOnline.class);
		}
		return Collections.emptyList();
	}

	
	public List<RegisterStatistic> listRegisterStatisticByDate(String recordDate) {
		if(recordDate != null){
			List<Long> ids = listRegisterStatisticId(recordDate, "");
			return this.getEntityFromIdList(ids, RegisterStatistic.class);
		}
		return Collections.emptyList();
	}

	
	public int getSinceMaxCount() {
		return this.sinceMaxCount;
	}
	
	
	public void addCampRegisterRecord(Camp camp) {
		synchronized (this.regStatMap) {
			Integer num = this.regStatMap.get(camp.ordinal());
			this.regStatMap.put(camp.ordinal(), num != null ? num + 1 : 1);
			if (camp == Camp.KNIFE_CAMP) { //加入其他阵营, 则无阵营人数需要减少
				knifeTotal.incrementAndGet();
				Integer noneNum = this.regStatMap.get(Camp.NONE.ordinal());
				noneNum = Math.max(0, noneNum != null ? noneNum - 1 : 0);
				this.regStatMap.put(Camp.NONE.ordinal(), noneNum);
			} else if (camp == Camp.SWORD_CAMP) { //加入其他阵营, 则无阵营人数需要减少
				swordTotal.incrementAndGet();
				Integer noneNum = this.regStatMap.get(Camp.NONE.ordinal());
				noneNum = Math.max(0, noneNum != null ? noneNum - 1 : 0);
				this.regStatMap.put(Camp.NONE.ordinal(), noneNum);
			}
			totalReg.incrementAndGet();
		}
	}

	
	public void addJobRegisterRecord(Job job) {
		int index = job.ordinal() + 10;
		synchronized (this.regStatMap) {
			Integer num = this.regStatMap.get(index);
			this.regStatMap.put(index, num != null ? num + 1 : 1);
		}
	}

	
	public void addCampOnline(Camp camp) {
		getCampOnlineArray(camp).incrementAndGet();
	}

	
	public void subCampOnline(Camp camp) {
		getCampOnlineArray(camp).decrementAndGet();
	}
	
	/**
	 * 取得阵营在线数组
	 * @param camp
	 * @return
	 */
	private AtomicInteger getCampOnlineArray(Camp camp){
		if(this.campOnlineArray == null){
			synchronized (this) {
				if(this.campOnlineArray == null){
					Camp[] camps = Camp.values();
					this.campOnlineArray = new AtomicInteger[ camps.length ];
					for(Camp cp : camps){
						this.campOnlineArray[ cp.ordinal() ] = new AtomicInteger(0);
					}
				}
			}
		}
		return this.campOnlineArray[camp.ordinal()];
	}

	
	public int getTotalRegCount() {
		return this.totalReg.get();
	}

	
	public int getCampPlayerCount(Camp camp) {
		synchronized (this.regStatMap){
			switch (camp) {
			case KNIFE_CAMP:	return this.knifeTotal.get();
			case SWORD_CAMP:	return this.swordTotal.get();
			case NONE:			return Math.min(0, totalReg.get() - this.knifeTotal.get() - this.swordTotal.get() );
			}
		}
		return 0;
	}

	
	public void onLoginEvent(UserDomain userDomain, int branching) {
		addCampOnline(userDomain.getPlayer().getCamp());
	}

	
	public void onLogoutEvent(UserDomain userDomain) {
		if(userDomain != null) {
			subCampOnline(userDomain.getPlayer().getCamp());
		}
	}

	
	public UserLevelStatistic getUserLevelStatistics(String date) {
		if(date == null || date.isEmpty()){
			return null;
		}
	    
		return this.get(date, UserLevelStatistic.class);
	}
	
	/**
	 * 实时统计当前等级在线信息
	 * @return {@link 统计结果}
	 */
	private UserLevelStatistic actualUserLevelStatistic() {
		UserLevelStatistic levelStatistic = new UserLevelStatistic();
		List<Integer> playerLevels = onlineStatisticDao.getAllPlayerLevel();
		if(playerLevels == null || playerLevels.isEmpty()){
			levelStatistic.setId(levelStatistic.builRecordDate());
			levelStatistic.setData("");
			return levelStatistic;
		}
		
		HashMap<Integer, UserLevelRecord> userLevels= new HashMap<Integer, UserLevelRecord>();
		for(int level = 1 ; level <= 100 ; level++){
			userLevels.put(level, UserLevelRecord.valueOf(level));
		}
		
		for(int level : playerLevels) {
			UserLevelRecord record = userLevels.get(level);
			if(record == null){
				record = UserLevelRecord.valueOf(level);
				userLevels.put(level, record);
				record = userLevels.get(level);
			}
			
			int onlineCount = this.onlineUserLevelStatistic(level); //该等级在线玩家的人数
			record.setOnlineCount(onlineCount);
			record.addCount();
		}
		
		List<UserLevelRecord> comparableList = new ArrayList<UserLevelRecord>(userLevels.values());
		Collections.sort(comparableList); //排序
		
		StringBuilder builder = new StringBuilder();
		for(UserLevelRecord record : comparableList){
			builder.append(record.toString()).append(Splitable.ELEMENT_DELIMITER);
		}
		
    	if(builder.length() > 0) {
    		builder.deleteCharAt(builder.length() - 1);
    	}
    	
    	levelStatistic.setId(levelStatistic.builRecordDate());//ID是时间
		levelStatistic.setData(builder.toString());//构造时间
		return levelStatistic;
	}
	
	/**
	 * 统计该等级在线玩家人数
	 * @param level 玩家等级 
	 * @return {@link Integer} 总人数
	 */
	private int onlineUserLevelStatistic(int level){
		int result = 0;//返回结果
		Set<Long> onlinePlayerIds = sessionManager.getOnlinePlayerIdList();
		if(onlinePlayerIds == null || onlinePlayerIds.isEmpty()){
			return result;
		}
		
		Iterator<Long> it = onlinePlayerIds.iterator();
		while(it.hasNext()){
			long playerId = it.next();
			UserDomain userDomain = userManager.getUserDomain(playerId);
			if(userDomain == null){
				continue;
			}
			
			PlayerBattle battle = userDomain.getBattle();
			if(level == battle.getLevel()){
				result += 1;
			}
		}
		
		return result;
	}


	
	public List<RegisterDetailStatistic> loadRegisterDetailStatistics(
			Date startDate, Date endDate) {
		return onlineStatisticDao.loadRegisterDetailStatistics(startDate, endDate);
	}
	
	
	
	
}
