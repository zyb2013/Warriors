package com.yayo.warriors.module.rank.facade.impl;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.utility.Tools;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.rank.constant.RankConstant;
import com.yayo.warriors.module.rank.facade.RankFacade;
import com.yayo.warriors.module.rank.manager.RankManager;
import com.yayo.warriors.module.rank.rule.RankRule;
import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.rank.vo.RankInfoVO;
import com.yayo.warriors.module.rank.vo.RankVO;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Job;

@Service
public class RankFacadeImpl implements RankFacade {
	@Autowired
	private RankManager rankManager;
	@Autowired
	private UserManager userManager;
	
	public ResultObject<RankVO> getRankVO(long playerId, RankType rankType, Job job, int pageSize, int pageNow) {
		if(pageSize <= 0 || rankType == null ){
			return ResultObject.ERROR( RankConstant.INPUT_VALUE_INVALID );
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR( RankConstant.PLAYER_NOT_FOUND );
		}
		if(pageNow <= 0){
			pageNow = 1;
		}
		RankVO value = RankVO.valueOf(rankType);
		List<RankInfoVO> values = rankManager.getRankVO(rankType, job);
		if(values != null ){
			value.setPageCount( (values.size() + pageSize - 1) / pageSize );
			if(pageNow > value.getPageCount()){
				pageNow = value.getPageCount();
			}
			int startIndex = pageSize * (pageNow - 1);
			List<RankInfoVO> pageResult = Tools.pageResult(values, startIndex, pageSize);
			final boolean isPlayerRank = ArrayUtils.contains(RankRule.PLAYER_RANK_TYPES, rankType);
			if(values != null && !values.isEmpty() ) { 
				if( isPlayerRank ){
					RankInfoVO fingVO = new RankInfoVO();
					fingVO.setPlayerId(playerId);
					value.setRank(values.indexOf(fingVO) + 1);
				}
				
				if(pageResult != null){
					value.setValues(pageResult.toArray());
				}
			}
		} else {
			pageNow = 0;
		}
		
		value.setPageNow(pageNow);
		
		return ResultObject.SUCCESS(value);
	}

	
	public ResultObject<RankVO> getRankVOByPlayerName(long playerId, String targetPlayerName, RankType rankType, Job job, int pageSize) {
		if( StringUtils.isBlank(targetPlayerName) ) {
			return ResultObject.ERROR( RankConstant.INPUT_VALUE_INVALID );
		}
		if(rankType == null || !ArrayUtils.contains(RankRule.PLAYER_RANK_TYPES, rankType)){
			return ResultObject.ERROR( RankConstant.INPUT_VALUE_INVALID );
		}
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR( RankConstant.PLAYER_NOT_FOUND );
		}
		Player player = userDomain.getPlayer();
		Player targetPlayer = userManager.getPlayer(targetPlayerName);
		if(targetPlayer == null){
			return ResultObject.ERROR( RankConstant.PLAYER_NOT_FOUND );
		}
		
		final String palyerName = player.getName();		
		RankVO value = RankVO.valueOf(rankType);
		value.setTargetPlayerName(targetPlayerName);
		List<RankInfoVO> values = rankManager.getRankVO(rankType, job);
		if(values != null){
			value.setPageCount( (values.size() + pageSize - 1) / pageSize );
			int targetIndex = -1;
			if(values != null && !values.isEmpty() ) { 
				for(int i = 0; i < values.size(); i++ ){
					RankInfoVO vo = values.get(i);
					if( vo.getName().equalsIgnoreCase(palyerName) ){
						value.setRank(i + 1);
					}
					if( vo.getName().equalsIgnoreCase(targetPlayerName) ){
						targetIndex = i;
					}
					if(targetIndex > -1 && value.getRank() > 0){
						break;
					}
				}
				if(targetIndex > -1){
					int startIndex = 0;
					int pageNow = 1;
					for(int i = 0; i < values.size(); i = i + pageSize ){
						if(targetIndex < i + pageSize){
							startIndex = i;
							break;
						}
						++pageNow;
					}
					value.setPageNow(pageNow);
					List<RankInfoVO> pageResult = Tools.pageResult(values, startIndex, pageSize);
					if(pageResult != null) {	
						value.setValues(pageResult.toArray());
					}
				} else {
					return ResultObject.ERROR(RankConstant.TARGET_NOT_IN_RANK);
				}
			}
		}
		return ResultObject.SUCCESS(value);
	}
	
}
