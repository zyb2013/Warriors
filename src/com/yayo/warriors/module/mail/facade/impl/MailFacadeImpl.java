package com.yayo.warriors.module.mail.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.mail.constant.MailConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.SessionManager;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.common.helper.MailPushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.gift.model.GiftRewardInfo;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.mail.constant.MailConstant;
import com.yayo.warriors.module.mail.entity.Mail;
import com.yayo.warriors.module.mail.entity.UserMail;
import com.yayo.warriors.module.mail.facade.MailFacade;
import com.yayo.warriors.module.mail.manager.MailManager;
import com.yayo.warriors.module.mail.model.MailState;
import com.yayo.warriors.module.mail.model.MailType;
import com.yayo.warriors.module.mail.model.ReceiveCondition;
import com.yayo.warriors.module.mail.vo.MailVO;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.CreateResult;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.GoodsType;


@Component
public class MailFacadeImpl implements MailFacade {

	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private AllianceManager allianceManager;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	

	
	public boolean sendMail(List<Long> playerIds) {
		return false;
	}
	
	
	
	public List<MailVO> listUserMails(long playerId) {
		UserMail userMail = mailManager.getUserMail(playerId);
		if(userMail == null) {
			return Collections.emptyList();
		}
		
		Set<Long> mailIdSet = userMail.getMailIdSet();               
		if (mailIdSet.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<MailVO> voList = new ArrayList<MailVO>(0);					
		Set<Long> stateSet = userMail.getStateSet();                    
		Set<Long> receivedSet = userMail.getReceivedSet();              
		List<Long> needDel = new ArrayList<Long>(mailIdSet);            
		for (long mailId : mailIdSet) {
			int receiveState = 0;
			int state = MailState.UNREAD.ordinal();
			Mail mail = mailManager.getMail(mailId);
			if (mail == null || mail.getEndTime().before(new Date())) {
				continue;
			}
			
			if (stateSet.contains(mailId)) {
				state = MailState.READED.ordinal();
			} 

			if (receivedSet.contains(mailId)) {
				receiveState = 1;
			}
			
			needDel.remove(mailId);
			voList.add(MailVO.valueOf(mailId, state, receiveState, mail));
		}
		
		if (!needDel.isEmpty()) {
			this.deleteMails(userMail, needDel);                          // 删除邮件
		}
		return voList;
	}

	
	/**
	 * 删除邮件
	 * 
	 * @param mailId               邮件自增ID
	 * @return {@link MailConstant}
	 */
	
	public int deleteMail(long playerId, List<Long> mailIdList) {
		UserMail userMail = mailManager.getUserMail(playerId);
		if(userMail == null) {
			return MAIL_NOT_FOUND;
		}
		
		Set<Long> mailIdSet = userMail.getMailIdSet();     		// 邮件ID列表
		if(mailIdSet == null || mailIdSet.isEmpty()) {
			return SUCCESS;
		}
		
		ChainLock lock = LockUtils.getLock(userMail);
		try {
			lock.lock();
			mailIdSet = userMail.getMailIdSet();                // 邮件ID列表
			if(mailIdSet == null || mailIdSet.isEmpty()) {
				return SUCCESS;
			}
			
			if(mailIdList == null || mailIdList.isEmpty()) { 	//需要删除所有邮件的
				userMail.add2DelMails(mailIdSet);
				userMail.removeAllMail();
			} else {
				userMail.removeMails(mailIdList);
				userMail.add2DelMails(mailIdList);
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userMail);
		return SUCCESS;
	}

	
	
	/**
	 * 领取邮件附件
	 * 
	 * @param playerId             用户自增ID
	 * @param mailId               邮件自增ID
	 * @return {@link Integer}
	 */
	@SuppressWarnings("unchecked")
	
	public int receiveMailReward(long playerId, long mailId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserMail userMail = mailManager.getUserMail(playerId);
		if(userMail == null) {
			return MAIL_NOT_FOUND;
		}
		
		if (!userMail.getMailIdSet().contains(mailId)) {
			return MAIL_NOT_FOUND;
		} else if (userMail.getReceivedSet().contains(mailId)) {
			return MAIL_REWARD_RECEIVED;
		}
		
		Player player = userDomain.getPlayer();
		Mail mail = mailManager.getMail(mailId);
		if(mail == null) {
			return MAIL_NOT_FOUND;
		} else if (mail.isTimeOut()) {
			return OUT_OF_DATE;
		}
		
		// 性能优点问题?
		Map<String, String[]> conditions = mail.getConditions();
		for (Map.Entry<String, String[]> entry : conditions.entrySet()) {
			String key = entry.getKey();
			String[] value = entry.getValue();
			ReceiveCondition receive = ReceiveCondition.getElementEnumById(Integer.valueOf(key));
			if (!checkCondition(userDomain, receive, value)) {
				return CONDITION_NOT_ENOUGH;
			}
		}
		
		List<GiftRewardInfo> infoList = mail.getGiftRewardInfos();
		List<UserEquip> equipList = new ArrayList<UserEquip>(0);
		PropsStackResult stackResult = PropsStackResult.valueOf();
		for (GiftRewardInfo info : infoList) {
			int baseId = info.getBaseId();
			int count  = info.getCount();
			if (info.getGoodsType() == GoodsType.PROPS) {
				PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
				if (propsConfig == null) {
					return ITEM_NOT_FOUND;
				}
				
				PropsStackResult stack = PropsHelper.calcPropsStack(playerId, DEFAULT_BACKPACK, baseId, count, true);
				stackResult.getMergeProps().putAll(stack.getMergeProps());
				stackResult.getNewUserProps().addAll(stack.getNewUserProps());
			} else if (info.getGoodsType() == GoodsType.EQUIP) {
				equipList.addAll(EquipHelper.newUserEquips(playerId, DEFAULT_BACKPACK, baseId, true, count));
			}
		}
		
		CreateResult<UserProps,UserEquip> createResult = null;
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();		// 堆叠的道具
		List<UserProps> newUserProps = stackResult.getNewUserProps();		// 新创建的道具
		int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), userMail);
		try {
			lock.lock();
			if (userMail.getReceivedSet().contains(mailId)) {
				return MAIL_REWARD_RECEIVED;
			}
			
			int needSize = newUserProps.size() + equipList.size();
			if (!player.canAddNew2Backpack(playerBackSize + needSize, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			createResult = propsManager.createUserEquipAndUserProps(newUserProps, equipList);
			if(!createResult.getCollections1().isEmpty()) {
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, createResult.getCollections1());
			}
			if (!createResult.getCollections2().isEmpty()) {
				propsManager.put2UserEquipIdsList(playerId, DEFAULT_BACKPACK, createResult.getCollections2());
			}
			
			userMail.addReceivedMailId(mailId);
			player.increaseGolden(mail.getGoldenRewards());
			player.increaseSilver(mail.getSilverRewards());
			player.increaseCoupon(mail.getCouponRewards());
			dbService.submitUpdate2Queue(userMail, player);
		} catch (Exception e) {
			logger.error("领取礼包附件异常:", e);
			return FAILURE;
		} finally {
			lock.unlock();
		}
		
		List<BackpackEntry> entries = new ArrayList<BackpackEntry>(0);
		List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
		if(!updateUserPropsList.isEmpty()) {
			entries.addAll(updateUserPropsList);
		}
		if(!createResult.getCollections1().isEmpty()) {
			entries.addAll(createResult.getCollections1());
		}
		if(!createResult.getCollections2().isEmpty()) {
			entries.addAll(createResult.getCollections2());
		}
		
		if (mail.getGoldenRewards() > 0) {
			GoldLogger.inCome(Source.RECEIVE_MAIL_REWARDS, mail.getGoldenRewards(), player);
		}
		if (mail.getSilverRewards() > 0) {
			SilverLogger.inCome(Source.RECEIVE_MAIL_REWARDS, mail.getSilverRewards(), player);
		}
		if (mail.getCouponRewards() > 0) {
			CouponLogger.inCome(Source.RECEIVE_MAIL_REWARDS, mail.getCouponRewards(), player);
		}
		if (!infoList.isEmpty()) {
			List<LoggerGoods> goodsInfo = LoggerGoods.loggerProps(Orient.INCOME, mergeProps, newUserProps);
			GoodsLogger.goodsLogger(player, Source.RECEIVE_MAIL_REWARDS, goodsInfo.toArray(new LoggerGoods[goodsInfo.size()]));
		}
		
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnitId = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnitId, GOLDEN, SILVER, COUPON);
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, entries);
		Collection<GoodsVO> goodsVO = GoodsVO.valuleOf(newUserProps, updateUserPropsList, mergeProps, equipList);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);            // 推送物品变化
		return SUCCESS;
	}


	/**
	 * 读取邮件
	 * 
	 * @param playerId
	 * @param mailId
	 * @return {@link CommonConstant}
	 */
	
	public int readMail(long playerId, long mailId) {
		UserMail userMail = mailManager.getUserMail(playerId);
		if(userMail == null) {
			return MAIL_NOT_FOUND;
		}
		
		if (!userMail.getMailIdSet().contains(mailId)) {
			return MAIL_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(userMail);
		try {
			lock.lock();
			if (!userMail.getMailIdSet().contains(mailId)) {
				return MAIL_NOT_FOUND;
			}
			userMail.addReadedMailId(mailId);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userMail);
		return SUCCESS;
	}
	
	
	/**
	 * 删除过期邮件
	 * 
	 * @param userMail    
	 * @param needDel
	 */
	private void deleteMails(UserMail userMail, List<Long> needDel) {
		if (userMail == null || needDel == null || needDel.isEmpty()) {
			return;
		}
		
		ChainLock lock = LockUtils.getLock(userMail);
		try {
			lock.lock();
			userMail.removeMails(needDel);
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(userMail);
	}
	
	
	/**
	 * 验证条件
	 * 
	 * @param userDomain
	 * @param condition
	 * @param format
	 * @return {@link Boolean}
	 */ 
	private boolean checkCondition(UserDomain userDomain, ReceiveCondition condition, String[] format) {
		if (condition == null) return false; 
			
		switch (condition) {
			case USER_LEVEL:  		return condition.isReachCondition(userDomain.getBattle(), format);
			case LOGIN_DAY: 		return condition.isReachCondition(userDomain.getPlayer(), format);
			case LAST_LOGIN_TIME: 	return condition.isReachCondition(userDomain.getPlayer(), format);
			case USER_GOLDEN: 		return condition.isReachCondition(userDomain.getPlayer(), format);
			case USER_SILVER:		return condition.isReachCondition(userDomain.getPlayer(), format);
			case REGISTER_TIME:		return condition.isReachCondition(userDomain.getPlayer(), format);
			case ALLIANCE_LEVEL: 	Alliance alliance = allianceManager.getAlliance4PlayerId(userDomain.getId());
									return condition.isReachCondition(alliance, format);
			case ALLIANCE_NAME: 	PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(userDomain.getBattle());
									return playerAlliance != null && condition.isReachCondition(playerAlliance, format);
			case LAST_CHARGE_TIME: 	break;
		}
		return false;
	}
}
