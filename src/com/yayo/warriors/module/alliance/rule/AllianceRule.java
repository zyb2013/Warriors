package com.yayo.warriors.module.alliance.rule;

import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.types.AllianceState;
import com.yayo.warriors.module.alliance.types.Title;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 帮派规则
 * @author liuyuhua
 */
public class AllianceRule {
	
	/** 初始化帮派等级*/
	public static final int INIT_ALLIANCE_LEVEL = 1;
	
	/** 初始化帮派建筑等级*/
	public static final int INIT_BUILD_LEVEL = 0;
	
	/** 帮派最大等级*/
	public static final int MAX_ALLIANCE_LEVEL = 5;
	
	/** 帮派建筑最大等级*/
	public static final int MAX_ALLIANCE_BUILD_LEVEL = 5;
	
	/** 最小帮派贡献货币值*/
	public static final int MIN_DONATE_SILVER = 100000;
	
	/** 每日最大 铜币捐献数量*/
	public static final int MAX_DONATE_SILVER_DAILY_COUNT = 5000000;
	
	/** 每日最大 令牌道具捐献数量*/
	public static final int MAX_DONATE_PROPS_DAILY_COUNT = 10;
	
	/** 申请加入帮派人数上限*/
	public static final int ALLIANCE_APPLY_LIMIT = 50;
	
	/** 创建帮派所需要的道具数量*/
	public static final int CREATE_ALLIANCE_USE_ITEM_COUNT = 1;
	
	/** 帮派公告字数长度*/
	public static final int MAX_ALLIANCE_NOTICE_LENGTH = 70;
	
	/** 创建帮派和加入帮派玩家的最低等级*/
	public static final int CREATE_AND_ADD_ALLIANCE_LEVEL_LIMIT = 20;
	
	/** 最大邀请列表*/
	public static final int MAX_INVITE_NUMBER = 30;
	
	/** 创建帮派的道具ID*/
	public static final int ALLIANCE_CREATE_PORPS_ID = 1501008;
	
	/** 捐献道具可以获得的捐献值*/
	public static final int DONATE_PROPS_REWARD_VALUE = 10; 
	
	/** 帮派占卦(抽奖) 所需要的贡献值*/
	public static final int ALLIANCE_DIVINE_NEED_DONATE = 10;
	
	/** 每日抽奖次数*/
	public static final int ALLIANCE_DIVINE_COUNT = 3;
	
	/** 公告发布权限*/
	public final static Title[] RELEASE_NOTICE = {Title.MASTER,Title.DEPUTYMASTER,
		                                                  Title.ELDER,Title.PROLAW};
	/** 审核帮员*/
	public final static Title[] EXAMINE_APPLY = {Title.MASTER,Title.DEPUTYMASTER,
                                                        Title.ELDER,Title.PROLAW};
	/** 查看申请者*/
	public final static Title[] VIEW_APPLY = {Title.MASTER,Title.DEPUTYMASTER,
                                                   Title.ELDER,Title.PROLAW,Title.MEMBER};
	
	/** 开除帮员*/
	public final static Title[] FIRE_MEMBER = {Title.MASTER,Title.DEPUTYMASTER,
		                                               Title.ELDER};
	/** 邀请帮员*/
	public final static Title[] INVITATION_MEMBER = {Title.MASTER,Title.DEPUTYMASTER,
                                                             Title.ELDER,Title.PROLAW};
	/** 升级帮派建筑*/
	public final static Title[] LEVELUP_BUILDING = {Title.MASTER,Title.DEPUTYMASTER};
	/** 帮派设置*/
	public final static Title[] ALLIANCE_SETTER = {Title.MASTER,Title.DEPUTYMASTER};
	/** 任免职位*/
	public final static Title[] APPOINT_TITLE = {Title.MASTER,Title.DEPUTYMASTER,
		                                                 Title.ELDER}; 
	/** 解散帮派*/
	public final static Title[] DISBAND_ALLIANCE = {Title.MASTER};
	
	/** 开除帮员*/ 
	public final static Title[] DISMISS_MEMBER = {Title.MASTER,Title.DEPUTYMASTER, 
		                                                 Title.ELDER};
	
	
	/**
	 * 职位权限验证
	 * @param Title              玩家的职位
	 * @param verfiyTitle        需要验证的职位
	 * @return true 有权限  false 无权限
	 */
	public static boolean vilidateTitle(Title Title,Title...verfiyTitle) {
		for(Title title : verfiyTitle){
			if(title == Title){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 创建帮派
	 * @param allianceName   帮派名
	 * @param player         玩家的对象
	 * @return {@link Alliance} 帮派对象
	 */
	public static Alliance createAlliance(String allianceName,Player player){
		long playerId = player.getId();
		Alliance alliance = new Alliance();
		alliance.setPlayerId(playerId);
		alliance.setName(allianceName);
		alliance.setLevel(INIT_ALLIANCE_LEVEL);
		alliance.setState(AllianceState.ACTIVE);
		alliance.setArenaLevel(INIT_BUILD_LEVEL);
		alliance.setShopLevel(INIT_BUILD_LEVEL);
		alliance.setDaisLevel(INIT_BUILD_LEVEL);
		alliance.setBooksLevel(INIT_BUILD_LEVEL);
		alliance.setSkills("");
		alliance.setMasterName(player.getName());
		alliance.setCamp(player.getCamp().ordinal());
		alliance.addLevelupRecords(INIT_ALLIANCE_LEVEL);
		return alliance;
	}
	
	/**
	 * 创建玩家帮派对象
	 * @param playerId      玩家对象
	 * @return {@link PlayerAlliance}  玩家帮派对象
	 */
	public static PlayerAlliance createPlayerAlliance(long playerId){
		PlayerAlliance playerAlliance = new PlayerAlliance();
		playerAlliance.setId(playerId);
		playerAlliance.setTitle(Title.NOMAL);
		return playerAlliance;
	}
}
