package com.yayo.warriors.common.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.utility.BeanUtil;
import com.yayo.warriors.basedb.adapter.SkillService;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.facade.AllianceFacade;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.buffer.vo.BufferVO;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.cooltime.model.CoolTime;
import com.yayo.warriors.module.drop.model.LootWrapper;
import com.yayo.warriors.module.drop.vo.DropRewardVO;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.UserEquipVO;
import com.yayo.warriors.module.skill.model.SkillVO;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.team.model.MemberVO;
import com.yayo.warriors.module.team.model.QueryMemberVO;
import com.yayo.warriors.module.team.model.QueryTeamVO;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.team.model.TeamVO;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.vo.LoginVO;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.GoodsType;

/**
 * VO创建工厂
 * 
 * @author Hyint
 */
@Component
public class VOFactory {
	
	@Autowired
	private TeamFacade teamFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SkillService skillService;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private AllianceFacade allianceFacade;
	@Autowired
	private CoolTimeManager coolTimeManager;
	
	/**
	 * 构建 LoginVO
	 * 
	 * @param  playerId			角色ID
	 * @return {@link LoginVO}	登录VO对象 
	 */
	public LoginVO getLoginVO(long playerId) {
		return LoginVO.valueOf(userManager.getUserDomain(playerId));
	}
	
	/**
	 * 获得LoginVO列表
	 * 
	 * @param  playerIds		角色ID列表
	 * @return {@link List}		角色登录VO对象
	 */
	public List<LoginVO> getLoginVOList(List<Long> playerIds) {
		if(playerIds == null || playerIds.isEmpty()) {
			return null;
		}
		
		List<LoginVO> voList = new ArrayList<LoginVO>();
		for (Long playerId : playerIds) {
			LoginVO loginVO = this.getLoginVO(playerId);
			if(loginVO != null && !voList.contains(loginVO)) {
				voList.add(loginVO);
			}
		}
		return voList;
	}
	
	/**
	 * 玩家路径
	 * @return
	 */
	public Map<String,Object> getPlayerPathVo(UserDomain userDomain, Object[] path){
		Map<String,Object> result = new HashMap<String , Object>(2);
		result.put(ResponseKey.PATH, path);
		result.put(ResponseKey.UNITID, userDomain.getUnitId());
		return result;
	}
	
	/**
	 * 怪物路径
	 * @return
	 */
	public Map<String,Object> getMonsterPathVo(MonsterDomain monsterDomain, Object[] path){
		Map<String,Object> result = new HashMap<String , Object>(2);
		result.put(ResponseKey.PATH, path);
		result.put(ResponseKey.UNITID, monsterDomain.getUnitId());
		return result;
	}
	
	
	/**
	 * 列出背包原件对象
	 * 
	 * @param  playerId			角色ID
	 * @param  backpack			背包号
	 * @return {@link List}		背包实体对象
	 */
	public List<BackpackEntry> listBackpackEntry(long playerId, int backpack) {
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		List<UserProps> userPropList = propsManager.listUserProps(playerId, backpack);
		List<UserEquip> userEquipList = propsManager.listUserEquip(playerId, backpack);
		if(userPropList != null && !userPropList.isEmpty()) {
			for (UserProps userProps : userPropList) {
				BackpackEntry entry = getUserPropsEntry(userProps);
				if(entry != null) {
					backpackEntries.add(entry);
				}
			}
		}
		
		if(userEquipList != null && !userEquipList.isEmpty()) {
			backpackEntries.addAll( this.getUserEquipEntries(userEquipList) );
		}
		return backpackEntries;
	}

	/**
	 * 获得CD时间对象
	 * 
	 * @param  playerId			角色ID
	 * @param  skillVOList		技能VO对象列表
	 * @return
	 */
	public long[] getCdTime(long playerId, List<SkillVO> skillVOList) {
		if(skillVOList == null || skillVOList.isEmpty()) {
			return new long[0];
		}
		
		UserCoolTime userCoolTime = coolTimeManager.getUserCoolTime(playerId);
		if(userCoolTime == null) {
			return new long[0];
		}
		
		long[] cdTimeArray = new long[skillVOList.size()];
		for (int index = 0; index < skillVOList.size(); index++) {
			SkillVO skillVO = skillVOList.get(index);
			if(skillVO == null) {
				continue;
			}
			
			int skillId = skillVO.getId();
			SkillConfig skillConfig = skillService.getSkillConfig(skillId);
			if(skillConfig == null) {
				continue;
			}
			
			CoolTime coolTime = userCoolTime.getCoolTime(skillConfig.getCdId());
			if(coolTime != null) {
				cdTimeArray[index] = coolTime.getEndTime();
			}
		}
		return cdTimeArray;
	}
	
	/**
	 * 列出背包实体对象
	 * 
	 * @param  playerId			角色ID
	 * @param  backpack			背包号
	 * @return {@link List}		背包实体对象
	 */
	public <T extends BackpackEntry> long[] getCoolTime(long playerId, List<T> backpackEntrys) {
		if(backpackEntrys == null || backpackEntrys.isEmpty()) {
			return new long[0];
		}
		
		UserCoolTime userCoolTime = coolTimeManager.getUserCoolTime(playerId);
		if(userCoolTime == null) {
			return new long[0];
		}
		
		long[] cdTimeArray = new long[backpackEntrys.size()];
		for (int index = 0; index < backpackEntrys.size(); index++) {
			BackpackEntry entry = backpackEntrys.get(index);
			if(entry == null) {
				continue;
			}
			
			if(entry.getGoodsType() == GoodsType.EQUIP) {
				continue;
			}
			
			int baseId = entry.getBaseId();
			PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
			if(propsConfig == null){
				continue;
			}
			
			if(propsConfig.getCdId() <= 0) {
				continue;
			}
			
			CoolTime coolTime = userCoolTime.getCoolTime(propsConfig.getCdId());
			if(coolTime != null) {
				cdTimeArray[index] = coolTime.getEndTime();
			}
		}
		return cdTimeArray;
	}
	
	/**
	 * 查询背包基础参数
	 * 
	 * @param  userPropsId				用户道具ID
	 * @return {@link BackpackEntry}	背包中的道具参数
	 */
	public BackpackEntry getUserPropsEntry(long userPropsId) {
		UserProps userProps = propsManager.getUserProps(userPropsId);
		return getUserPropsEntry(userProps);
	}

	/**
	 * 查询背包装备背包参数
	 * 
	 * @param  userPropsId				用户装备ID
	 * @return {@link BackpackEntry}	背包中的道具参数
	 */
	public BackpackEntry getUserEquipEntry(long userPropsId) {
		UserEquip userEquip = propsManager.getUserEquip(userPropsId);
		return getUserEquipEntry(userEquip);
	}

	/**
	 * 查询背包装备背包参数
	 * 
	 * @param  userEquip				用户装备
	 * @return {@link BackpackEntry}	背包中的道具参数
	 */
	public BackpackEntry getUserEquipEntry(UserEquip userEquip) {
		BackpackEntry target = null;
		if(userEquip != null) {
			target = new BackpackEntry();
			BeanUtil.copyProperties(userEquip, target);
		}
		return target;
	}

	/**
	 * 查询背包装备背包参数
	 * 
	 * @param  userEquips				用户装备数组
	 * @return {@link BackpackEntry}	背包中的道具参数
	 */
	public List<BackpackEntry> getUserEquipEntries(UserEquip...userEquips) {
		List<BackpackEntry> entries = new ArrayList<BackpackEntry>();
		for (UserEquip userEquip : userEquips) {
			BackpackEntry entry = this.getUserEquipEntry(userEquip);
			if(entry != null) {
				entries.add(entry);
			}
		}
		return entries;
	}

	/**
	 * 查询背包装备背包参数
	 * 
	 * @param  userEquips				用户装备数组
	 * @return {@link BackpackEntry}	背包中的道具参数
	 */
	public List<BackpackEntry> getUserEquipEntries(Collection<UserEquip> userEquips) {
		List<BackpackEntry> entries = new ArrayList<BackpackEntry>(userEquips == null ? 0 : userEquips.size());
		if(userEquips == null || userEquips.isEmpty()) {
			return entries;
		}
		
		for (UserEquip userEquip : userEquips) {
			BackpackEntry entry = this.getUserEquipEntry(userEquip);
			if(entry != null) {
				entries.add(entry);
			}
		}
		return entries;
	}

	/**
	 * 查询背包道具背包参数
	 * 
	 * @param  userProp				用户道具
	 * @return {@link BackpackEntry}	背包中的道具参数
	 */
	public BackpackEntry getUserPropsEntry(UserProps userProp) {
		BackpackEntry target = null;
		if(userProp != null) {
			target = new BackpackEntry();
			BeanUtil.copyProperties(userProp, target);
		}
		return target;
	}

	/**
	 * 查询背包道具背包参数
	 * 
	 * @param  userPropArr				用户道具可变参
	 * @return {@link List}				背包中的道具参数
	 */
	public List<BackpackEntry> getUserPropsEntries(UserProps...userPropArr) {
		List<BackpackEntry> entries = new ArrayList<BackpackEntry>();
		if(userPropArr.length > 0) {
			for (UserProps userProps : userPropArr) {
				BackpackEntry userPropsEntry = this.getUserPropsEntry(userProps);
				if(userPropsEntry != null) {
					entries.add(userPropsEntry);
				}
			}
		}
		return entries;
	}

	/**
	 * 查询背包道具背包参数
	 * 
	 * @param  userPropsList				用户道具可变参
	 * @return {@link List}				背包中的道具参数
	 */
	public List<BackpackEntry> getUserPropsEntries(Collection<UserProps> userPropsList) {
		List<BackpackEntry> entries = new ArrayList<BackpackEntry>(userPropsList == null ? 0 : userPropsList.size());
		if(userPropsList != null && !userPropsList.isEmpty()) {
			for (UserProps userProps : userPropsList) {
				entries.add(this.getUserPropsEntry(userProps));
			}
		}
		return entries;
	}
	
	/**
	 * 查询用户装备VO对象
	 * 
	 * @param  userEquipId				用户装备ID
	 * @return {@link UserEquipVO}		用户装备VO
	 */
	public UserEquipVO getUserEquipVO(long userEquipId) {
		UserEquipVO userEquipVO = null;
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip != null) {
			userEquipVO = new UserEquipVO();
			BeanUtil.copyProperties(userEquip, userEquipVO);
		}
		return userEquipVO;
	}
	
	/**
	 * 获得组队成员VO对象
	 * 
	 * @param  playerId			组队对象
	 * @return {@link MemberVO}	组队成员的VO对象
	 */
	public MemberVO getTeamMemberVO(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		Player player = userDomain.getPlayer();
		PlayerMotion motion = userDomain.getMotion();
		if(player == null || motion == null) {
			return null;
		}
		
		boolean online = userManager.isOnline(playerId);
		Object[] attributes = AttributeRule.TEAM_ATTR_PARAMS;
		Object[] attributeValues = userManager.getPlayerAttributes(playerId, attributes);
		
		MemberVO memberVO = new MemberVO();
		memberVO.setPlayerId(playerId);
		memberVO.setOnline(online);
		memberVO.setX(motion.getX());
		memberVO.setY(motion.getY());
		memberVO.setMapId(motion.getMapId());
		memberVO.setAttributes(attributes);
		memberVO.setValues(attributeValues);
		return memberVO;
	}
	
	/**
	 * 查询组队成员VO列表
	 * 
	 * @param  memberIds		角色ID列表
	 * @return {@link List}		成员VO对象列表
	 */
	public List<MemberVO> getTeamMemberVOList(Collection<Long> memberIds) {
		List<MemberVO> memberVOList = new ArrayList<MemberVO>(memberIds == null ? 0 : memberIds.size());
		if(memberIds == null || memberIds.isEmpty()) {
			return memberVOList;
		}
		
		for (Long memberId : memberIds) {
			MemberVO teamMemberVO = getTeamMemberVO(memberId);
			if(teamMemberVO != null) {
				memberVOList.add(teamMemberVO);
			}
		}
		return memberVOList;
	}
	
	/**
	 * 获得组队VO对象
	 * 
	 * @param  team	 			组队对象
	 * @return {@link TeamVO}	组队VO对象
	 */
	public TeamVO getTeamVO(Team team) {
		TeamVO teamVO = null;
		if(team != null) {
			List<MemberVO> memberVOList = getTeamMemberVOList(team.getMembers());
			teamVO = TeamVO.valueOf(team, memberVOList);
		}
		return teamVO;
	}
	
	/**
	 * 查询组队VO列表
	 * 
	 * @param  teamIdList		队伍ID列表
	 * @return {@link List}		用于查询的组队VO列表
	 */
	public List<QueryTeamVO> getQueryTeamVO(long playerId, List<Integer> teamIdList) {
		if(teamIdList == null || teamIdList.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<QueryTeamVO> queryTeamVOList = new ArrayList<QueryTeamVO>();
		for (Integer teamId : teamIdList) {
			QueryTeamVO queryTeamVO = getQueryTeamVO(playerId, teamId);
			if(queryTeamVO != null) {
				queryTeamVOList.add(queryTeamVO);
			}
		}
		return queryTeamVOList;
	}
	
	
	/**
	 * 获得查询组队的VO对象
	 * 
	 * @param  playerId					角色ID
	 * @return {@link QueryMemberVO}	查询成员VO
	 */
	public QueryMemberVO getQueryMemberVO(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		Alliance alliance = allianceFacade.getAlliance(playerId);
		QueryMemberVO memberVO = new QueryMemberVO();
		memberVO.setId(playerId);
		memberVO.setName(player.getName());
		memberVO.setLevel(battle.getLevel());
		memberVO.setServerId(player.getServerId());
		memberVO.setJob(battle.getJob().ordinal());
		memberVO.setCamp(player.getCamp().ordinal());
		memberVO.setAllianceName(alliance == null ? "" : alliance.getName());
		memberVO.setInTeam(player.getTeamId() != 0 ? true : false);
		return memberVO;
	}
	
	/**
	 * 查询组队需要的玩家信息ID
	 * 
	 * @param  memberIdList		角色ID列表
	 * @return {@link List}		用于查询的玩家信息列表
	 */
	public List<QueryMemberVO> getQueryMemberVO(List<Long> memberIdList) {
		List<QueryMemberVO> queryTeamVOList = new ArrayList<QueryMemberVO>();
		if(memberIdList != null && !memberIdList.isEmpty()) {
			for (Long memberId : memberIdList) {
				QueryMemberVO queryMemberVO = getQueryMemberVO(memberId);
				if(queryMemberVO != null) {
					queryTeamVOList.add(queryMemberVO);
				}
			}
		}
		return queryTeamVOList;
	}
	
	/**
	 * 查询组队VO对象
	 * 
	 * @param  teamId				队伍ID
	 * @return {@link QueryTeamVO}	查询的组队对象
	 */
	private QueryTeamVO getQueryTeamVO(long playerId, int teamId) {
		QueryTeamVO queryTeamVO = null;
		Team team = teamFacade.getTeam(teamId);
		UserDomain domain = userManager.getUserDomain(playerId);
		if(domain != null && team != null) {
			long leaderId = team.getLeaderId();
			int teamMethod = team.getTeamMethod();
			int teamSize = team.getMembers().size();
			
			queryTeamVO = new QueryTeamVO();
			queryTeamVO.setId(teamId);
			queryTeamVO.setLeaderId(leaderId);
			queryTeamVO.setTeamMethod(teamMethod);
			queryTeamVO.setCurrentMembers(teamSize);
			
			UserDomain userDomain = userManager.getUserDomain(leaderId);
			if(userDomain != null) {
				Player player = domain.getPlayer();
				Player leader = userDomain.getPlayer();
				if (player.getCamp() != leader.getCamp()) return null;
				queryTeamVO.setLeaderName(userDomain.getPlayer().getName());
			}

			if(teamSize > 0) {
				int totalLevel = getTotalLevel(team.getMembers());
				queryTeamVO.setAverageLevel(totalLevel / teamSize);
			}
		}
		return queryTeamVO;
	}
	
	
	private int getTotalLevel(Collection<Long> memberIdList) {
		int total = 0;
		if(memberIdList != null && !memberIdList.isEmpty()) {
			for (Long playerId : memberIdList) {
				UserDomain userDomain = userManager.getUserDomain(playerId);
				if(userDomain == null) {
					continue;
				}
				
				PlayerBattle battle = userDomain.getBattle();
				if(battle != null) {
					total += battle.getLevel();
				}
			}
		}
		return total;
	}
	 
	/**
	 * 构建掉落奖励对象VO
	 * 
	 * @param  rewardWrapper			掉落奖励对象
	 * @return {@link DropRewardVO}		掉落奖励VO对象
	 */
	public DropRewardVO getDropRewardVO(LootWrapper rewardWrapper) {
		if(rewardWrapper == null) {
			return null;
		}
		
		DropRewardVO rewardVO = new DropRewardVO();
		rewardVO.setX(rewardWrapper.getX());
		rewardVO.setY(rewardWrapper.getY());
		rewardVO.setId(rewardWrapper.getId());
		GameScreen currentScreen = rewardWrapper.getCurrentScreen();
		if(currentScreen != null) {
			rewardVO.setMapId(currentScreen.getGameMap().getMapId());
		}
		rewardVO.setAmount(rewardWrapper.getAmount());
		rewardVO.setBaseId(rewardWrapper.getBaseId());
		rewardVO.setType(rewardWrapper.getGoodsType());
		rewardVO.setEndTime(rewardWrapper.getEndTime());
		rewardVO.setSharePlayers(rewardWrapper.getSharePlayers());
		return rewardVO;
	}
	
	/**
	 * 列出掉落奖励VO列表
	 * 
	 * @param rewardWrappers
	 * @return
	 */
	public List<DropRewardVO> listDropRewardVO(Collection<LootWrapper> rewardWrappers) {
		List<DropRewardVO> voList = new ArrayList<DropRewardVO>();
		if(rewardWrappers != null && !rewardWrappers.isEmpty()) {
			for (LootWrapper rewardWrapper : rewardWrappers) {
				DropRewardVO dropRewardVO = this.getDropRewardVO(rewardWrapper);
				if(dropRewardVO != null) {
					voList.add(dropRewardVO);
				}
			}
		}
		return voList;
	}
	
	/**
	 * 获得所有BUFFVO信息
	 * 
	 * @param  playerId				角色ID
	 * @return {@link BufferVO[]}	BufferVO数组
	 */
	public List<BufferVO> getUserBufferVO(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return new ArrayList<BufferVO>(0);
		}
		
		UserBuffer userBuffer = userDomain.getUserBuffer();
		Map<Integer, Buffer> userBuffers = userBuffer.getAndCopyBufferMap();
		if(userBuffers == null || userBuffers.isEmpty()) {
			return new ArrayList<BufferVO>(0);
		}
		
		List<BufferVO> bufferVOList = new ArrayList<BufferVO>(userBuffers.size());
		for (Buffer buffer : userBuffers.values()) {
			if(buffer != null) {
				bufferVOList.add(BufferVO.valueOf(buffer));
			}
		}
		return bufferVOList;
	}
}
