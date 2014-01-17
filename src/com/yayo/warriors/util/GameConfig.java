package com.yayo.warriors.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.utility.NumberUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.syscfg.entity.SystemConfig;
import com.yayo.warriors.module.syscfg.manager.SystemConfigManager;
import com.yayo.warriors.module.syscfg.type.ConfigType;

/**
 * 游戏配置对象
 * 
 * @author Hyint
 */
@Component
public class GameConfig {

	/** 是否可以使用GM命令 */
	@Autowired(required=false)
	@Qualifier("server.command.enableGM")
	private Boolean enableGM = false;

	/** 防沉迷开关 */
	@Autowired(required=false)
	@Qualifier("server.command.indulgeEnable")
	private Boolean indulgeEnable = false;
	
	/** 帐号创建开关. true-可以创建, false-不可以创建 */
	@Autowired(required=false)
	@Qualifier("server.command.createEnable")
	private String createRegular = "";
	
	/** 限制地图玩家最多人数 */
	@Autowired(required=false)
	@Qualifier("LIMIT_MAP_PLAYER_COUNTS")
	private String LIMIT_MAP_PLAYER_COUNTS = "101_100_11|104_100_20";
	
	/** 阵营战持续时间  */
	@Autowired(required=false)
	@Qualifier("CAMP_BATTLE_TIMEOUT")
	private Integer CAMP_BATTLE_TIMEOUT = 60;
	
	@Autowired(required=true)
	@Qualifier("GAME_SERVER_FIRST_OPEN")
	private String firstServerOpenTime = "";
	
	/** 最大分线数量 */
	@Autowired(required=false)
	@Qualifier("MAX_BRANCHING")
	private Integer MAX_BRANCHING = 1;
	
	/** 单条分线最大的成员数量 */
	@Autowired(required=false)
	@Qualifier("MAX_BRANCHING_MEMBERS")
	private Integer MAX_BRANCHING_MEMBERS = 3000;
	
	/** 服务器访问权限开放状态 */
	private boolean serverVistOpened;
	
	/** 服务器开启访问格式数组 */
	private Pattern[] serverVistOpenedRegex = null;
	
	/** 日志	*/
	private static final Logger LOGGER = LoggerFactory.getLogger(GameConfig.class);

	@Autowired
	private SystemConfigManager configManager;
	@Autowired
	private ChannelFacade channelFacade;
	
	/** 软引用对象 ---- 超平增加 */
	private static ObjectReference<GameConfig> ref = new ObjectReference<GameConfig>();
	/** 限制地图角色人数上限的地图map */
	private ConcurrentMap<Integer, Integer[]> limitPlayerMap = new ConcurrentHashMap<Integer, Integer[]>(2);

	private static GameConfig getIntance() {
		return ref.get();
	}
	
	/**
	 * 是否开启防沉迷
	 *  
	 * @return boolean
	 */
	public static boolean isIndulgeEnble() {
		return getIntance().indulgeEnable;
	}
	
	/**
	 * 是否开启GM
	 * 
	 * @return boolean
	 */
	public static boolean isEnableGM() {
		return getIntance().enableGM;
	}
	
	
	public static String getFirstServerOpenTime() {
		return getIntance().firstServerOpenTime;
	}
	
	/** 
	 * 是否可以创建帐号.
	 * 
	 * @return {@link Boolean}	true-可以创建, false-不可以创建
	 */
	public static boolean canCreateCharacter(String userName) {
		if(!StringUtils.isBlank(getIntance().createRegular) && !StringUtils.isBlank(userName)) { //创建帐号正则表达式不存在, 则直接表示可以创建的
			return userName.trim().toLowerCase().matches(getIntance().createRegular); 
		}
		return true;
	}
	
	/** 
	 * 初始化方法
	 */
	@PostConstruct
	protected void init() {
		ref.set(this);
		getIntance().initIndulgeStatus();
		LOGGER.error("NOT ERROR 初始化是否开启GM命令: [{}], 是否允许外网访问: [{}] 防沉迷开启:[{}] 创号规则:[{}] ", new Object[] { isEnableGM(), isServerVistOpened(), isIndulgeEnble(), createRegular } );
		
		//限制人数地图
		SystemConfig systemConfig = configManager.getSystemConfig(ConfigType.LIMIT_PLAYER_MAP);
		String info = systemConfig != null && StringUtils.isNotBlank(systemConfig.getInfo()) ? systemConfig.getInfo() : this.LIMIT_MAP_PLAYER_COUNTS;
		if(StringUtils.isNotBlank(info)){
			Map<Integer, Integer[]> map = NumberUtil.delimiterString2Map(info, Integer.class, Integer.class);
			for(Entry<Integer, Integer[]> entry : map.entrySet() ){
				limitPlayerMap.put(entry.getKey(), entry.getValue());
			}
			LOGGER.error("NOT ERROR 地图开房间设置:[{}] ", new Object[] {info} );
		}
		
		//分线最大人数
		systemConfig = configManager.getSystemConfig(ConfigType.MAX_BRANCHING_MEMBERS);
		if(systemConfig != null){
			info = systemConfig.getInfo();
			if(StringUtils.isNotBlank(info)){
				MAX_BRANCHING_MEMBERS = Integer.valueOf(info);
			}
		}
		
		//分线最大人数
		MAX_BRANCHING = channelFacade.getCurrentBranching().size();
	}
	
	/**
	 * 初始化防沉迷状态
	 */
	private void initIndulgeStatus() {
		SystemConfig systemConfig = configManager.getSystemConfig(ConfigType.INDULGE_OPEN);
		if(systemConfig == null) {
			return;
		}
		
		//没有任何数据, 则直接返回
		List<String[]> array = Tools.delimiterString2Array(systemConfig.getInfo());
		if(array == null || array.isEmpty()) {
			return;
		}

		String[] elements = array.get(0);
		if(elements != null && elements.length > 0) { //数据库有信息
			indulgeEnable = Integer.valueOf(elements[0]) == 1;
		}
	}
	
	/**
	 * 设置开服标记
	 * 
	 * @param open 		是否开放
	 * @param allowIp 	开放IP,为空表示只设置开服状态
	 */
	public void setOpenIp(boolean open, String allowIp) {
		this.serverVistOpened = open;
		if(StringUtils.isNotBlank(allowIp)) {	// 开放IP
			String[] s = allowIp.split(Splitable.BETWEEN_ITEMS);
			Pattern[] openedRegex = new Pattern[s.length];
			for(int i=0;i<s.length;i++){
				String str = s[i].replace(".", "[.]").replace("*", "[0-9]*");
				openedRegex[i] = Pattern.compile(str);
			}
			synchronized (serverVistOpenedRegex) {
				serverVistOpenedRegex = openedRegex;
			}
			
			try {
				configManager.updateSystemConfig(ConfigType.ALLOW_ACCESS_IP, allowIp);
			} catch (Exception e) {
				LOGGER.error("设置开服{}", e);
			}
		}
		
	}
	
	/**
	 * 判断是否开放开放访问
	 * @return	boolean		true:开放访问		false:未开放访问
	 */
	public boolean isServerVistOpened(){
		return this.serverVistOpened ;
	}
	
	
	/**
	 * 检查IP是否允许访问
	 * 
	 * @param  ip 					IP地址
	 * @return {@link Boolean}		是否允许
	 */
	public boolean isOpenIp(String ip) {
		if(serverVistOpened) {
			return true;
		}
		
		// 启动时不需要读取配置，永远都是关服状态，等待管理后台开启
		if(serverVistOpenedRegex == null){
			SystemConfig openIp = configManager.getSystemConfig(ConfigType.ALLOW_ACCESS_IP);
			if(openIp == null || StringUtils.isBlank(openIp.getInfo())) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("未设置未开服允许登录IP配置 - [{}]...", ConfigType.ALLOW_ACCESS_IP);
				}
				return false;
			}
			
			String[] s = openIp.getInfo().split(",");
			serverVistOpenedRegex = new Pattern[s.length];
			for(int i=0;i<s.length;i++){
				String str = s[i].replace(".", "[.]").replace("*", "[0-9]*");
				serverVistOpenedRegex[i] = Pattern.compile(str);
			}
		}
		
		if(ip == null) {
			return false;
		}
		
		for(Pattern pattern : serverVistOpenedRegex){
			if(pattern != null && pattern.matcher(ip).matches()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 更新防沉迷状态
	 * 
	 * @param state 
	 */
	public void updateIndulgeState(boolean indulgeState) {
		try {
			indulgeEnable = indulgeState;
			String indulegeInfo = String.valueOf(indulgeState ? 0 : 1);
			configManager.updateSystemConfig(ConfigType.INDULGE_OPEN, indulegeInfo);
		} catch (Exception e) {
			LOGGER.error("设置防沉迷开关, 出现异常: {}", e);
		}
	}
	
	/**
	 * 更新新手村地图人数
	 * 
	 * @param state 
	 */
	public boolean updateMaxBranchingConfig(ConfigType configType, String paramValue) {
		try {
			int count = Integer.valueOf(paramValue);
			if(count > 0){
				switch (configType) {
				case MAX_BRANCHING:				//最大分线数量
					MAX_BRANCHING = count;
					channelFacade.addBranching(count);
					break;
					
				case MAX_BRANCHING_MEMBERS:		//单条分线最大人数
					MAX_BRANCHING_MEMBERS = count;
					configManager.updateSystemConfig(configType, String.valueOf(count) );
					break;
				
				default:
					return false;
				}
				return true;
				
			} else {
				LOGGER.error("设置新手村地图人数出错，参数:{}", count);
			}
			
		} catch (Exception e) {
			LOGGER.error("设置新手村地图人数 出现异常: {}", e);
		}
		return false;
	}
	
	/**
	 * 更新地图限制人数的配置
	 * 
	 * @param state 
	 */
	public boolean updateLimitPlayerMap(String paramValue) {
		try {
			int count = Integer.valueOf(paramValue);
			if(count > 0){
				Map<Integer, Integer[]> map = NumberUtil.delimiterString2Map(paramValue, Integer.class, Integer.class);
				for(Entry<Integer, Integer[]> entry : map.entrySet()){
					Integer[] value = entry.getValue();
					if(value == null || value.length < 2 || value[0] <= 0 || value[0] <= 0){
						continue;
					}
					limitPlayerMap.put(entry.getKey(), entry.getValue());
				}
				
				StringBuilder sb = new StringBuilder();
				for(Entry<Integer, Integer[]> entry : limitPlayerMap.entrySet()){
					Integer[] value = entry.getValue();
					sb.append(Splitable.ELEMENT_DELIMITER).append(entry.getKey()).append(Splitable.ATTRIBUTE_SPLIT).append(value[0]).append(Splitable.ATTRIBUTE_SPLIT).append(value[1]);
				}
				if(sb.length() > 0){
					sb.deleteCharAt(0);
				}
				configManager.updateSystemConfig(ConfigType.LIMIT_PLAYER_MAP, sb.toString() );
				
				return true;
			} else {
				LOGGER.error("设置新手村地图人数出错，参数:{}", count);
			}
			
		} catch (Exception e) {
			LOGGER.error("设置地图人数限制 出现异常: {}", e);
		}
		return false;
	}
	
	public static Integer[] getLimitPlayerMap(int mapId){
		Integer[] values = ref.get().limitPlayerMap.get(mapId);
//		if(values == null){
//			values = new Integer[]{100, 1};
//		}
		return values;
	}

	public static Integer getCampBattleTimeout() {
		return ref.get().CAMP_BATTLE_TIMEOUT;
	}

	public static Integer getMaxBranching() {
		return ref.get().MAX_BRANCHING;
	}

	public static Integer getMaxBranchingMembers() {
		return ref.get().MAX_BRANCHING_MEMBERS;
	}
	
}
