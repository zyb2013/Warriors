package com.yayo.warriors.module.alliance.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.alliance.constant.AllianceConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.FIRST_ACHIEVE;
import static com.yayo.warriors.module.achieve.model.FirstType.FIRST_ADD_ALLIANCE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.AllianceConfig;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.AlliancePushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.alliance.constant.AllianceConstant;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.facade.AllianceFacade;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.alliance.model.Apply;
import com.yayo.warriors.module.alliance.model.Devolve;
import com.yayo.warriors.module.alliance.model.Invite;
import com.yayo.warriors.module.alliance.model.Record;
import com.yayo.warriors.module.alliance.rule.AllianceRule;
import com.yayo.warriors.module.alliance.types.Title;
import com.yayo.warriors.module.alliance.types.VilidaState;
import com.yayo.warriors.module.alliance.vo.AllianceVo;
import com.yayo.warriors.module.alliance.vo.ApplyVo;
import com.yayo.warriors.module.alliance.vo.MemberVo;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.AllianceLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.util.NameUtils;

/**
 * 帮派接口实现类 
 * @author liuyuhua
 */
@Component
public class AllianceFacadeImpl implements AllianceFacade{

	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private AlliancePushHelper alliancePushHelper;
	@Autowired
	private CachedService cachedService;
	@Autowired
	private AchieveFacade achieveFacade;
	
	/** 创建帮派所需要的资金 */
	@Autowired(required=false)
	@Qualifier("CREATE_ALLIANCE_SILVER")
	private Integer CREATE_ALLIANCE_SILVER = 100000;
	
	/** 申请加入帮派者 {帮派ID,申请者}*/
	private final static ConcurrentHashMap<Long, List<Apply>> ALLIANCEAPLLAYS = new ConcurrentHashMap<Long, List<Apply>>(1);
	
	/** 邀请玩家加入帮派 {帮派ID,被邀请者列表}*/
	private final static ConcurrentHashMap<Long, List<Invite>> ALLIANCEINVITE = new ConcurrentHashMap<Long, List<Invite>>(1);
	
	
	
	public PlayerAlliance getPlayerAlliance(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		return playerAlliance;
	}

	
	public Alliance getAlliance(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return null;
		}
		return allianceManager.getAlliance(playerAlliance.getAllianceId());
	}
	
	

	
	public int releaseNotice(long playerId, String content) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		
		if(playerAlliance == null){
			return FAILURE;
		}
		
		if(content.length() > AllianceRule.MAX_ALLIANCE_NOTICE_LENGTH){
			return ALLIANCE_NOTICE_CONTENT;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return PLAYER_EXIST_ALLIANCE;
		}
		
	    if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.RELEASE_NOTICE)){
	    	return PLAYER_NOT_POWER;
	    }
	    
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(alliance);
		try {
			lock.lock();
			if(alliance.getNotice().equals(content)){
				return ALLIANCE_NOTICE_CONTENT_THE_SAME;
			}
			alliance.setNotice(content);
			
		}finally{
			lock.unlock();
		}
		
		return SUCCESS;
	}
	
	
	@SuppressWarnings("unchecked")
	public ResultObject<Alliance> createAlliance(long playerId, String name) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		
		if(player.getCamp() == Camp.NONE){
			return ResultObject.ERROR(PLAYER_NOT_CAMP);
		}
		
		if(player.getSilver() < this.CREATE_ALLIANCE_SILVER){
			return ResultObject.ERROR(SILVER_NOT_ENOUGH);
		}
		
		PlayerAlliance playerAlliance = this.getPlayerAlliance(playerId);
		if(playerAlliance == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(PLAYER_ALLIANCE_EXIST);
		}
		
		name = StringUtils.defaultIfBlank(name, "");
		if(!NameUtils.validAllianceName(name)) {
			return ResultObject.ERROR(ALLIANCE_NAME_TOO_LONG);
		}
		
		Map<String,Long> allianceNames = allianceManager.getAllianceNames();
		if(allianceNames != null){
			if(allianceNames.containsKey(name)){
				return ResultObject.ERROR(ALLIANCE_NAME_EXIST);
			}
		}
		
		if(battle.getLevel() < AllianceRule.CREATE_AND_ADD_ALLIANCE_LEVEL_LIMIT){
			return ResultObject.ERROR(PLAYER_LEVEL_ENOUGH);
		}
		
		Alliance alliance = AllianceRule.createAlliance(name, player); //先创建公会
		ChainLock lock = LockUtils.getLock(player, playerAlliance);
		try {
			lock.lock();
			if(playerAlliance.isExistAlliance()){
				return ResultObject.ERROR(PLAYER_ALLIANCE_EXIST);
			}
			boolean isCreate = allianceManager.createAlliance(playerAlliance, alliance);
	    	if(isCreate){//入库成功
	    		player.decreaseSilver(this.CREATE_ALLIANCE_SILVER);
	    	}else{//入库异常
	    		return ResultObject.ERROR(FAILURE);
	    	}
		} finally {
			lock.unlock();
		}
		
		dbService.updateEntityIntime(player, playerAlliance); //创建公会立即保存
		List<Long> playerIdList = Arrays.asList(playerId);    //推送货币
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.SILVER);
		
		//发送公告
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.CREATE_ALLIANCE, BulletinConfig.class);
		if (config != null) {
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put(NoticeRule.playerId, playerId);
			params.put(NoticeRule.name, alliance.getName());
			params.put(NoticeRule.playerName, player.getName());
			NoticePushHelper.pushNotice(NoticeID.CREATE_ALLIANCE, NoticeType.HONOR, params, config.getPriority());
		}
		
		taskFacade.updateJoinAllianceTask(playerId);
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_ADD_ALLIANCE);   // 第一次加入帮会成就
		return ResultObject.SUCCESS(alliance);
	}

	
	@SuppressWarnings("unchecked")
	public ResultObject<Alliance> createAllianceUseProps(long playerId, long propsId,String name) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		
		if(player.getCamp() == Camp.NONE){
			return ResultObject.ERROR(PLAYER_NOT_CAMP);
		}
		
		PlayerAlliance playerAlliance = this.getPlayerAlliance(playerId);
		if(playerAlliance == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(PLAYER_ALLIANCE_EXIST);
		}
		
		name = StringUtils.defaultIfBlank(name, "");
		if(!NameUtils.validAllianceName(name)) {
			return ResultObject.ERROR(ALLIANCE_NAME_TOO_LONG);
		}
		
		Map<String,Long> allianceNames = allianceManager.getAllianceNames();
		if(allianceNames != null){
			if(allianceNames.containsKey(name)){
				return ResultObject.ERROR(ALLIANCE_NAME_EXIST);
			}
		}
		
		if(battle.getLevel() < AllianceRule.CREATE_AND_ADD_ALLIANCE_LEVEL_LIMIT){
			return ResultObject.ERROR(PLAYER_LEVEL_ENOUGH);
		}
		
		Alliance alliance = AllianceRule.createAlliance(name, player); //先创建公会
		UserProps userProps = propsManager.getUserProps(propsId);
		if (userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}

		int costItemCount = AllianceRule.CREATE_ALLIANCE_USE_ITEM_COUNT;
		if (userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
			return ResultObject.ERROR(BACKPACK_INVALID);
		} else if (userProps.getCount() < costItemCount) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}

		PropsConfig propsConfig = userProps.getPropsConfig();
		if (propsConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		} else if (propsConfig.getChildType() != PropsChildType.ALLIANCE_CREATE_PROPS_TYPE) {
			return ResultObject.ERROR(BELONGS_INVALID);
		}
		
		int baseId = propsConfig.getId();
		ChainLock lock = LockUtils.getLock(player.getPackLock(), player, playerAlliance);
		try {
			lock.lock();
			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_INVALID);
			} else if (userProps.getCount() < costItemCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			if(playerAlliance.isExistAlliance()){
				return ResultObject.ERROR(PLAYER_ALLIANCE_EXIST);
			}
			
			boolean isCreate = allianceManager.createAlliance(playerAlliance, alliance);
	    	if(isCreate){
				userProps.decreaseItemCount(costItemCount);
				propsManager.removeUserPropsIfCountNotEnough(userProps);//清理物品缓存
	    	}else{
	    		return ResultObject.ERROR(FAILURE);
	    	}
		} finally {
			lock.unlock();
		}
	
		LoggerGoods outcomeProps = LoggerGoods.outcomeProps(propsId, baseId, costItemCount);
		AllianceLogger.createAlliance(player, name, 0 , 0 , outcomeProps);		//创建帮派的日志
		GoodsLogger.goodsLogger(player, Source.ALLIANCE_CREATE, outcomeProps);	//记录物品日志
		dbService.updateEntityIntime(userProps, player, playerAlliance);		//创建公会立即保存
		MessagePushHelper.pushUserProps2Client(playerId, userProps.getBackpack(), false, userProps);//推送道具
		
		List<Long> playerIdList = Arrays.asList(playerId); //推送货币
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.SILVER);
		
		//发送公告
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.CREATE_ALLIANCE, BulletinConfig.class);
		if (config != null) {
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put(NoticeRule.playerId, playerId);
			params.put(NoticeRule.name, alliance.getName());
			params.put(NoticeRule.playerName, player.getName());
			NoticePushHelper.pushNotice(NoticeID.CREATE_ALLIANCE, NoticeType.HONOR, params, config.getPriority());
		}
		
		taskFacade.updateJoinAllianceTask(playerId);
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_ADD_ALLIANCE);
		return ResultObject.SUCCESS(alliance);
	}

	
	public List<AllianceVo> sublistAlliances(long playerId, int start, int count) {
		List<AllianceVo> result = new ArrayList<AllianceVo>();
		List<Long> allianceIds = allianceManager.getAllianceIds(); 
		List<Long> ids =  Tools.pageResult(allianceIds,(start * count), count);
		if(ids == null || ids.isEmpty()){
			return result;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return result;
		}
		PlayerBattle battle = userDomain.getBattle();
		
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return result;
		}
		
		for(Long allianceId : ids){
			Alliance alliance = allianceManager.getAlliance(allianceId);
			if(alliance == null || alliance.isDrop()){
				continue;
			}
			
			/***
			 * 有限显示级别判断:
			 * 1.帮派默认开始是可以加入状态
			 * 2.判断帮派是否关闭加入状态
			 * 3.当如果自己已经加入了帮派就变成已经申请
			 * 4.判断帮派是否已经满员
			 * 5.当玩家已经存在帮派,将全部至为已经加入状态
			 */
			int state = AllianceVo.OPEN;
			if(alliance.getVilidaState() == VilidaState.CLOSED){
				state = AllianceVo.CLOSE; //关闭玩家申请
			}
			
			if(this.isApply(playerId, allianceId)){
				state = AllianceVo.APPLY; //玩家已经申请
			}else{
				if(this.sizeApply(allianceId) >= AllianceRule.ALLIANCE_APPLY_LIMIT){
					state = AllianceVo.APPLYFULL;
				}else{
					
					AllianceConfig config = allianceManager.getAllianceConfig(alliance.getLevel());
					if(config != null){
						if(this.sizeMembers4Alliance(allianceId) >= config.getMemberLimit()){
							state = AllianceVo.FULL; //帮派已满员
						}
					}
				}
			}
			
			if(playerAlliance.isExistAlliance()){
				state = AllianceVo.JOINED;
			}
			
			String allianceName = alliance.getName();
			String masterName   = alliance.getMasterName();
			int level           = alliance.getLevel();
			int members         = allianceManager.sizeAllianceMembers(allianceId);
			int camp            = alliance.getCamp();
			long masterId       = alliance.getPlayerId();
			result.add(AllianceVo.valueOf(allianceId, masterId, allianceName, masterName, level, members, state, camp));
		}
		
		return result;
	}
	
	
	public List<MemberVo> sublistMembers(long playerId, int start, int count) {
		List<MemberVo> result = new ArrayList<MemberVo>();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return result;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return result;
		}
		
		List<Long> members = allianceManager.getAllianceMembers(playerAlliance.getAllianceId(),false);
		if(members == null || members.isEmpty()){
			return result;
		}
		
		List<Long> ids =  Tools.pageResult(members, (start * count), count);
		if(ids != null && !ids.isEmpty()){
			for(Long memberId : ids){
				UserDomain memberDomain = userManager.getUserDomain(memberId);
				if(memberDomain == null){
					continue;
				}
				PlayerBattle memberBattle = memberDomain.getBattle();
				PlayerAlliance memberAlliance = allianceManager.getPlayerAlliance(memberBattle);
				if(memberAlliance == null){
					continue;
				}
				
				String name = memberDomain.getPlayer().getName();
				int job = memberDomain.getBattle().getJob().ordinal();
				int level = memberDomain.getBattle().getLevel();
				int donate = memberAlliance.getDonate();
				int hisdonate = memberAlliance.getHisdonate();
				int title = memberAlliance.getTitle().ordinal();
				boolean online = userManager.isOnline(memberId);
				result.add(MemberVo.valueOf(memberId, name, job, level, donate, hisdonate, title, online));
			}
		}
		
		return result;
	}
	
	
	public List<ApplyVo> sublistApplys(long playerId, int start, int count) {
		List<ApplyVo> result = new ArrayList<ApplyVo>();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return result;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return result;
		}
		
		List<Apply> applys = this.getApplys(playerAlliance.getAllianceId());
		List<Apply> ids = Tools.pageResult(applys, (start * count), count);
		if(ids == null || ids.isEmpty()){
			return result;
		}
		
		for(Apply apply : ids){
			UserDomain applyDomain = userManager.getUserDomain(apply.getPlayerId());
			if(applyDomain == null){
				continue;
			}
			
			long applyId = apply.getPlayerId();
			String name  = applyDomain.getPlayer().getName();
			int job      = applyDomain.getBattle().getJob().ordinal();
			int level    = applyDomain.getBattle().getLevel();
			result.add(ApplyVo.valueOf(applyId, name, job, level, apply.getDate()));
		}
		
		return result;
	}

	
	public int sizeAlliances() {
		List<Long> allianceIds = allianceManager.getAllianceIds(); 
		if(allianceIds != null){
			return allianceIds.size();
		}else{
			return 0;
		}
	}
	
	
	public int sizeMembers4Alliance(long allianceId){
		List<Long> members = allianceManager.getAllianceMembers(allianceId,false);
		if(members == null || members.isEmpty()){
			return 0;
		}
		return members.size();
	}
	
	/**
	 * 是否存在申请者列表
	 * @param playerId        玩家的ID
	 * @param allianceId      帮派的ID
	 * @return true 已经申请 false 反之
	 */
	private boolean isApply(long playerId,long allianceId){
		List<Apply> applys = this.getApplys(allianceId);
		if(applys == null || applys.isEmpty()){
			return false;
		}
		
		return applys.contains(Apply.valueOf(playerId));
	}
	
	/**
	 * 获取申请者数量
	 * @param allianceId       帮派的ID
	 * @return {@link Integer} 申请者数量
	 */
	private int sizeApply(long allianceId){
		List<Apply> applys = this.getApplys(allianceId);
		if(applys == null || applys.isEmpty()){
			return 0;
		}
		return applys.size();
	}
	
	/**
	 * 获取申请加入帮派的集合
	 * @param allianceId      帮派的ID
	 * @return {@link Set}    申请加入者集合
	 */
	private List<Apply> getApplys(long allianceId){
		List<Apply> applys = ALLIANCEAPLLAYS.get(allianceId);
		if(applys == null){
			applys = Collections.synchronizedList(new ArrayList<Apply>());
			ALLIANCEAPLLAYS.putIfAbsent(allianceId, applys);
			return ALLIANCEAPLLAYS.get(allianceId);
		}
		
		for(Iterator<Apply> it = applys.iterator();it.hasNext();){
			Apply apply = it.next();
			if(apply.isOverTime()){
				it.remove();
			}
		}
		return applys;
	}
	
	/**
	 * 获取被邀请者列表
	 * @param allianceId    帮派ID
	 * @return {@link List} 被邀请者
	 */
	private List<Invite> getInvites(long allianceId){
		List<Invite> invites = ALLIANCEINVITE.get(allianceId);
		if(invites == null){
			invites = Collections.synchronizedList(new ArrayList<Invite>());
			ALLIANCEINVITE.putIfAbsent(allianceId, invites);
			return ALLIANCEINVITE.get(allianceId);
		}
		
		for(Iterator<Invite> it = invites.iterator();it.hasNext();){
			Invite invite = it.next();
			if(invite.isOverTime()){
				it.remove();
			}
		}
		return invites;
	}
	

	
	public int joinAlliance(long allianceId, long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if(player.getCamp() == Camp.NONE){
			return PLAYER_NOT_CAMP;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(playerAlliance.isExistAlliance()){
			return PLAYER_EXIST_ALLIANCE;
		}
		
		if(userDomain.getBattle().getLevel() < AllianceRule.CREATE_AND_ADD_ALLIANCE_LEVEL_LIMIT){
			return PLAYER_LEVEL_ENOUGH;
		}
		
		if(!playerAlliance.canJoinAlliance()){
			return OVER_ALLIANCE_OPERATE;
		}
		
		Alliance alliance = allianceManager.getAlliance(allianceId);
		if(alliance == null || alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(alliance.getCamp() != player.getCamp().ordinal()){
			return PLAYER_CAMP_NOT_SAME;
		}
		
		AllianceConfig config = allianceManager.getAllianceConfig(alliance.getLevel());
		if(config == null){
			return BASEDATA_NOT_FOUND;
		}
		
		if(this.sizeMembers4Alliance(allianceId) >= config.getMemberLimit()){
			return ALLIANCE_MEMBER_IS_FULL;
		}
		
		if(alliance.getVilidaState() == VilidaState.CLOSED){
			return ALLIANCE_VILIDA_CLOSE;
		}else if(alliance.getVilidaState() == VilidaState.VILIDATE){
			if(this.sizeApply(allianceId) >= AllianceRule.ALLIANCE_APPLY_LIMIT){
				return ALLIANCE_APPLY_FULL;
			}
			
			if(this.isApply(playerId, allianceId)){
				return ALLIANCE_APPLY_OVER;
			}
			
			List<Apply> applys = this.getApplys(allianceId);
			applys.add(Apply.valueOf(playerId));
			
		}else if(alliance.getVilidaState() == VilidaState.NORMAL){
			boolean result = allianceManager.joinAlliance(playerAlliance, alliance);
			if(!result){
				return FAILURE;
			}
			
			alliance.addRecordLog(Record.log4Join(player.getName()));
			List<Long> members = allianceManager.getMembers4Player(playerId);
			alliancePushHelper.pushJoinAlliance(members, playerId, player.getName(),alliance.getMasterName(),alliance.getPlayerId(),members.size()); //通知帮派中其他玩家加入帮派
			alliancePushHelper.pushJoinSuccess(playerId, allianceId); //通知玩家加入帮派成功
			taskFacade.updateJoinAllianceTask(playerId);
		}
		
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_ADD_ALLIANCE);   // 第一次加入帮会成就
		return SUCCESS;
	}

	
	public int dismissMember(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(userDomain == null || targetDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		Player targetPlayer = targetDomain.getPlayer();
		PlayerBattle targetBattle = targetDomain.getBattle();
		
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(playerBattle);
		PlayerAlliance targetAlliance = allianceManager.getPlayerAlliance(targetBattle);
		if(playerAlliance == null || targetAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		boolean result = allianceManager.dismissMember(playerAlliance, targetAlliance,targetBattle);
		if(!result){
			return DISMISS_MEMBER_ERROR;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance != null && !alliance.isDrop()){
			alliance.addRecordLog(Record.log4Quit(targetPlayer.getName()));
		}
		
		alliancePushHelper.pushDismissMember(targetId);
		return SUCCESS;
	}

	
	public int gquitAlliance(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());//获取帮派
		
		boolean result = allianceManager.gquitAlliance(playerAlliance,battle);
		if(!result){
			return GQUIT_ALLIANCE_ERROR;
		}
		
		if(alliance != null && !alliance.isDrop()){//退出以后才增加记录
			alliance.addRecordLog(Record.log4Quit(player.getName()));
		}
		
		return SUCCESS;
	}

	
	public int disbandAlliance(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(playerAlliance.getTitle() != Title.MASTER){
			return PLAYER_NOT_POWER;
		}
		
		List<Long> result = allianceManager.disbandAlliance(playerAlliance);
		if(result == null){
			return ALLIANCE_DISBANDED;
		}
		
		alliancePushHelper.pushDisbandAlliace(result); //通知所有帮派中的成员
		return SUCCESS;
	}

	
	public int vilidaAlliance(long playerId, int state) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ALLIANCE_NOT_FOUND;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.ALLIANCE_SETTER)){
			return PLAYER_NOT_POWER;
		}
		
		if(alliance.getVilidaState() == state){
			return VILIDA_THE_SAME; 
		}
		
		ChainLock lock = LockUtils.getLock(alliance);
		try {
			lock.lock();
			if(alliance.getVilidaState() == state){
				return VILIDA_THE_SAME; 
			}
			
			if(state == VilidaState.NORMAL){
				alliance.setVilidaState(state);
			}else if(state == VilidaState.VILIDATE){
				alliance.setVilidaState(state);
			}else if(state == VilidaState.CLOSED){
				alliance.setVilidaState(state);
			}else{
				return VILIDA_NOT_TYPE;
			}
			
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(alliance);
		return SUCCESS;
	}

	
	public int examineApply(long playerId, long applyId, boolean agree) {
		UserDomain userDomain = userManager.getUserDomain(playerId); //审批者
		UserDomain applyDomain = userManager.getUserDomain(applyId); //申请者
		if(userDomain == null || applyDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		Player applyPlayer = applyDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(playerBattle);
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ALLIANCE_NOT_FOUND;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.EXAMINE_APPLY)){
			return PLAYER_NOT_POWER;
		}
		
		List<Apply> applys = this.getApplys(alliance.getId());
		Apply apply = Apply.valueOf(applyId); //构造申请者
		if(!applys.contains(apply)){
			return ALLIANCE_APPLY_NOT_FOUND;
		}
		applys.remove(apply); //操作都将删除内容
		
		AllianceConfig config = allianceManager.getAllianceConfig(alliance.getLevel());
		if(config == null){
			return BASEDATA_NOT_FOUND;
		}
		
		if(agree){ //同意加入帮派
			PlayerAlliance applyAlliance = this.getPlayerAlliance(applyId);
			if(applyAlliance == null){
				return PLAYER_NOT_POWER;
			}
			if(applyAlliance.isExistAlliance()){
				return PLAYER_EXIST_ALLIANCE;
			}
			
			if(!applyAlliance.canJoinAlliance()){
				return OVER_ALLIANCE_OPERATE;
			}
			
			boolean result = allianceManager.joinAlliance(applyAlliance, alliance);
			if(result){
				alliance.addRecordLog(Record.log4Join(applyPlayer.getName()));
				List<Long> members = allianceManager.getAllianceMembers(alliance.getId(),true);
				alliancePushHelper.pushJoinAlliance(members, applyId , applyDomain.getPlayer().getName(),alliance.getMasterName(),alliance.getPlayerId(),members.size());
				alliancePushHelper.pushJoinSuccess(applyId, alliance.getId());
				taskFacade.updateJoinAllianceTask(applyId);//玩家加入帮派
			}else{
				return FAILURE;
			}
		}
		
		return SUCCESS;
	}

	
	public int devolveMaster(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId); //帮主
		UserDomain devolerDomain = userManager.getUserDomain(targetId); //转移目标
		if(userDomain == null || devolerDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		PlayerBattle devolerBattle = devolerDomain.getBattle();
		PlayerAlliance playerAlliance  = allianceManager.getPlayerAlliance(playerBattle);
		PlayerAlliance devolerAlliance = allianceManager.getPlayerAlliance(devolerBattle); 
		if(playerAlliance == null || devolerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(playerId == targetId){
			return CANT_NOT_SELF;
		}
		
		if(!playerAlliance.isExistAlliance() || !devolerAlliance.isExistAlliance()){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(playerAlliance.getAllianceId() != devolerAlliance.getAllianceId()){
			return ALLIANCE_NOT_IN_SAME;
		}
		
		if(playerAlliance.getTitle() != Title.MASTER){
			return PLAYER_NOT_POWER;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(!userManager.isOnline(targetId)){
			return PLAYER_OFF_LINE;
		}
		
		ChainLock lock = LockUtils.getLock(alliance);
		try {
			lock.lock();
			cachedService.put2EntityCache(this.getDevolveMasterKey(alliance.getId()), Devolve.valueOf(targetId), 300000); //缓存回执单
		}finally{
			lock.unlock();
		}
		
		this.alliancePushHelper.pushDevolveNotice(targetId, devolerDomain.getPlayer().getName());
		return SUCCESS;
	}

	
	@SuppressWarnings("unchecked")
	public int confirmDevolve(long playerId, boolean confirm) {
		UserDomain userDomain = userManager.getUserDomain(playerId); //帮主
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ALLIANCE_NOT_FOUND;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		UserDomain masterDomain = userManager.getUserDomain(alliance.getPlayerId()); //帮主的对象
		if(masterDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerAlliance masterAlliance = allianceManager.getPlayerAlliance(masterDomain.getBattle()); //帮主的对象
		if(masterAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		String oldMasterName = masterDomain.getPlayer().getName(); //原帮主的名字
		String newMasterName = userDomain.getPlayer().getName();   //新帮主的名字
		
		if(confirm){
			ChainLock lock = LockUtils.getLock(alliance,masterAlliance,playerAlliance);
			try {
				lock.lock();
				Devolve devolve = (Devolve) cachedService.getFromEntityCache(getDevolveMasterKey(alliance.getId()));
				if(devolve == null || devolve.getPlayerId() != playerId){
					return DEVOLVE_MASTER_OVER_TIME;
				}
				
				if(alliance.isDrop()){
					return ALLIANCE_NOT_FOUND;
				}
				
				//如果之前已经有职位了,将来更改之前的职位人数
				Title afterTitle = playerAlliance.getTitle();
				if(Title.DEPUTYMASTER == afterTitle){
					alliance.decreaseDeputymaster(1);
				}else if(Title.PROLAW == afterTitle){
					alliance.decreaseProlawNum(1);
				}else if(Title.ELDER == afterTitle){
					alliance.decreaseElderNum(1);
				}

				alliance.setPlayerId(playerId);
				alliance.setMasterName(newMasterName);
				masterAlliance.setTitle(Title.MEMBER);
				playerAlliance.setTitle(Title.MASTER);
				
			}finally{
				lock.unlock();
			}
			
			alliance.addRecordLog(Record.log4Devole(oldMasterName, newMasterName));//新增记录
			dbService.submitUpdate2Queue(alliance,masterAlliance,playerAlliance);
			Collection<Long> playerIds = allianceManager.getAllianceMembers(alliance.getId(), true);
			alliancePushHelper.pushDevolveAccept(playerIds, userDomain.getPlayer().getName(), playerId);
			
		}else{
			alliancePushHelper.pushDevolveReject(alliance.getPlayerId(),userDomain.getPlayer().getName());
		}
		
		cachedService.removeFromEntityCache(getDevolveMasterKey(alliance.getId()));
		return SUCCESS;
	}
	
	
	/**
	 * 获取转移会长的'回执'
	 * @param allianceId        帮派的ID
	 * @return {@link String}   键值
	 */
	private String getDevolveMasterKey(long allianceId) {
		return new StringBuffer().append("ALLIANCE_DEVOLVE_").append(allianceId).toString();
	}

	
	public int sizeApply4Alliance(long allianceId) {
		return this.getApplys(allianceId).size();
	}

	
	public ResultObject<Map<String,Object>> appointTitle(long playerId, long targetId, int title) {
		UserDomain userDomain   = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(userDomain == null || targetDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(userDomain.getBattle());
		PlayerAlliance targetAlliance = allianceManager.getPlayerAlliance(targetDomain.getBattle());
		if(playerAlliance == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		Title titleEnum = EnumUtils.getEnum(Title.class, title);
		if(titleEnum == null){
			return ResultObject.ERROR(TITLE_NOT_FOUND);
		}
		
		boolean result = allianceManager.appointTitle(playerAlliance, targetAlliance, titleEnum);
		if(!result){
			return ResultObject.ERROR(APPOINT_TITLE_FAUIL);
		}
		
		Map<String,Object> resultObject = new HashMap<String, Object>(4);
		resultObject.put(ResponseKey.RESULT, AllianceConstant.SUCCESS);
		resultObject.put(ResponseKey.PROLAW_NUM, alliance.getProlawNum());
		resultObject.put(ResponseKey.ELDER_NUM,  alliance.getElderNum());
		resultObject.put(ResponseKey.DMASTER_NUM, alliance.getDeputymasterNum());
		
		return ResultObject.SUCCESS(resultObject);
	}

	
	public int inviteMember(long playerId, long targetId) {
		UserDomain userDomain   = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(userDomain == null || targetDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(playerId == targetId){
			return CANT_NOT_SELF;
		}
		
		if(userDomain.getPlayer().getCamp() != targetDomain.getPlayer().getCamp()){
			return PLAYER_CAMP_NOT_SAME;
		}
		
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(userDomain.getBattle());
		PlayerAlliance targetAlliance = allianceManager.getPlayerAlliance(targetDomain.getBattle());
		if(playerAlliance == null || targetAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return PLAYER_NOT_POWER;
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.INVITATION_MEMBER)){
			return PLAYER_NOT_POWER;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(targetAlliance.isExistAlliance()){
			return PLAYER_EXIST_ALLIANCE;
		}
		
		if(!targetAlliance.canJoinAlliance()){
			return OVER_ALLIANCE_OPERATE;
		}
		
		List<Invite> invites = this.getInvites(playerAlliance.getAllianceId());
		if(invites.size() >= AllianceRule.MAX_INVITE_NUMBER){
			return INVITE_IS_FULL;
		}
		
		Invite invite = Invite.valueOf(targetId);
		if(invites.contains(invite)){
			return PLAYER_WAS_INVITED; 
		}
		
		invites.add(invite);
		
		alliancePushHelper.pushInviteMember(targetId, playerId, alliance.getId(), alliance.getLevel(), alliance.getName(), userDomain.getPlayer().getName());
		return SUCCESS;
	}

	
	public int confirmInvite(long playerId, long inviterId, long allianceId,boolean confirm) {
		UserDomain userDomain = userManager.getUserDomain(playerId); //帮主
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(userDomain.getBattle());
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(playerAlliance.isExistAlliance()){
			return PLAYER_ALLIANCE_EXIST;
		}
		
		if(!playerAlliance.canJoinAlliance()){
			return OVER_ALLIANCE_OPERATE;
		}
		
		Alliance alliance = allianceManager.getAlliance(allianceId);
		if(alliance == null){
			return ALLIANCE_NOT_FOUND;
		}
		
		if(alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		List<Invite> invites = this.getInvites(allianceId);
		Invite invite = Invite.valueOf(playerId);
		if(!invites.remove(invite)){
			return INVITE_NOT_FOUND;
		}
		
		if(confirm){
			
			AllianceConfig config = allianceManager.getAllianceConfig(alliance.getLevel());
			if(config == null){
				return BASEDATA_NOT_FOUND;
			}
			
			if(this.sizeMembers4Alliance(allianceId) >= config.getMemberLimit()){
				return ALLIANCE_MEMBER_IS_FULL;
			}
			
			boolean result = allianceManager.joinAlliance(playerAlliance, alliance);
			if(!result) {
				return CONFIRM_INVITE_FAUIL;
			}
			
			UserDomain inviterDomain = userManager.getUserDomain(inviterId);
			if(inviterDomain != null){
				Player inviterPlayer = inviterDomain.getPlayer();
				alliance.addRecordLog(Record.log4Join(inviterPlayer.getName()));//增加玩家加入记录
			}
			
			List<Long> member = allianceManager.getAllianceMembers(allianceId,true);
			alliancePushHelper.pushJoinAlliance(member, playerId, userDomain.getPlayer().getName(),alliance.getMasterName(),alliance.getPlayerId(),member.size());
			alliancePushHelper.pushJoinSuccess(playerId, allianceId);
			taskFacade.updateJoinAllianceTask(inviterId);//玩家加入帮派
			
		}else{
			alliancePushHelper.pushInviteReject(inviterId, userDomain.getPlayer().getName());
		}
		
		return SUCCESS;
	}
	
}
