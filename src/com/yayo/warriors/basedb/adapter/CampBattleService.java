package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.NumberUtil;
import com.yayo.warriors.basedb.model.CampPointConfig;
import com.yayo.warriors.basedb.model.CampScoreRewards;
import com.yayo.warriors.basedb.model.CampTitleRewards;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.type.IndexName;

/**
 * 阵营战基础数据接口
 * @author jonsai
 *
 */
@Component
public class CampBattleService extends ResourceAdapter {
	@Autowired(required = false)
	@Qualifier("CAMP_BATTLE_POINT_MODEL")
	private String pointModels = "0_112|1_113|2_114";
	
	/** 可以加分的据点数量 */
	private int pointScores = 0;
	
	/** 阵营据点复活点列表 */
	private List<CampPointConfig> revivePoints = new ArrayList<CampPointConfig>(1);
	
	/** 阵营据点模型id */
	private Map<Integer, Integer> pointModelMap = null;
	
	/** 阵营据点id */
	@SuppressWarnings("unchecked")
	private	List<Integer>[] campPointIds = new ArrayList[Camp.values().length];
	private Set<Integer> campPointIdSet = new HashSet<Integer>();
	
	@SuppressWarnings("unchecked")
	
	public void initialize() {
		
		campPointIds = new ArrayList[Camp.values().length];
		revivePoints.clear();
		if(pointModelMap == null){
			pointModelMap = NumberUtil.delimiterString2Map2(pointModels, Integer.class, Integer.class);
		}
		
		Collection<CampPointConfig> campPointConfigs = resourceService.listAll(CampPointConfig.class);
		for(CampPointConfig campPointConfig : campPointConfigs){
			int type = campPointConfig.getType();
			if(type != 1){	//不是据点
				continue;
			}
			revivePoints.add(campPointConfig);//是据点复活
			
			int baseId = campPointConfig.getBaseId();
			Camp camp = EnumUtils.getEnum(Camp.class, campPointConfig.getCamp());
			List<Integer> ids = campPointIds[camp.ordinal()];
			if(ids == null){
				ids = new ArrayList<Integer>(1);
				campPointIds[camp.ordinal()] = ids;
			}
			if(baseId > 0){
				ids.add(baseId);
				campPointIdSet.add(baseId);
			}
		}
		
		this.pointScores = campPointIdSet.size() / 2 + 1;
	}
	
	/**
	 * 取得阵营据点模型
	 * @param camp
	 * @return
	 */
	public Integer getPointModule(int camp){
		return this.pointModelMap.get(camp);
	}
	
	/**
	 * 取得最近的复活点
	 * @param camp				所在的阵营
	 * @param ownPointIds		已经占据的据点
	 * @param motionX			角色死亡时的X坐标
	 * @param motionY			角色死亡时的Y坐标
	 * @return
	 */
	public Point getNearestRevivePoint(final Camp camp, final Collection<Integer> ownPointIds, final int motionX, final int motionY){
		CampPointConfig noPointConfig = resourceService.getByUnique(IndexName.CAMP_TYPE_POINT, CampPointConfig.class, 2, 0, camp.ordinal());
		if(ownPointIds == null || ownPointIds.isEmpty()){
			return new Point(noPointConfig.getX(), noPointConfig.getY());
		}
		
		final Set<Integer> myPointIds = new HashSet<Integer>( ownPointIds );
		List<CampPointConfig> list = new ArrayList<CampPointConfig>( revivePoints );
		Collections.sort(list, new Comparator<CampPointConfig>() {
			
			public int compare(CampPointConfig o1, CampPointConfig o2) {
				boolean c1 = myPointIds.contains( o1.getBaseId() );
				boolean c2 = myPointIds.contains( o2.getBaseId() );
				if(c1 && !c2){
					return -1;
				} else if(!c1 && c2){
					return 1;
				} else if(c1 && c2){
					int d1 = MapUtils.calcDistance(motionX, motionY, o1.getX(), o1.getY());
					int d2 = MapUtils.calcDistance(motionX, motionY, o2.getX(), o2.getY());
					if(d1 < d2){
						return -1;
					} else if(d1 > d2){
						return 1;
					} else if(o1.getId() < o2.getId()){
						return -1;
					} else if(o1.getId() > o2.getId()){
						return 1;
					}
					
				} else {
					return 1;
				}
				
				return 0;
			}
		});
		
		for(int i = 0; i < list.size(); i++){
			CampPointConfig campPointConfig = list.get(i);
			if( !myPointIds.contains( campPointConfig.getBaseId() ) ){
				continue ;
			}
			int x = campPointConfig.getX();
			int y = campPointConfig.getY();
			if(x <= 0 && y <= 0){
				continue ;
			}
			return new Point(x, y);
		}
		
		return new Point(noPointConfig.getX(), noPointConfig.getY());
	}
	
	/**
	 * 阵营据点id集合
	 * @return
	 */
	public Set<Integer> getCampPointIds(){
		return campPointIdSet;
	}
	
	/**
	 * 
	 * @param camp
	 * @return
	 */
	public List<Integer> getCampPointIds(Camp camp){
		return campPointIds[camp.ordinal()];
	}
	
	public Camp getInitCampByMonsterConfigId(int monsterConfigId){
		Collection<CampPointConfig> configs = resourceService.listAll(CampPointConfig.class);
		if(configs != null){
			for(CampPointConfig campPointConfig : configs){
				if(campPointConfig.getType() == 1 && campPointConfig.getBaseId() == monsterConfigId){
					return Camp.values()[ campPointConfig.getCamp() ];
				}
			}
		}
		return null;
	}
	

	/**
	 * 可以加分的据点数量
	 * @return
	 */
	public int getCanAddCampPointScorePointCount(){
		return pointScores;
	}

	/**
	 * 取得阵营战得分奖励和胜利奖励
	 * @param playerScore		玩家的得分
	 * @param win				是否是胜利方
	 * @return
	 */
	public CampScoreRewards getCampBattleScoreRewards(int playerScore){
		List<CampScoreRewards> rewards = (List<CampScoreRewards>)resourceService.listAll(CampScoreRewards.class);
		if(rewards != null && rewards.size() > 0){
			CampScoreRewards firstCampScoreRewards = rewards.get(0);
			if(playerScore < firstCampScoreRewards.getMinScore() ){
				return null;
			}
			CampScoreRewards sReward = null;
			CampScoreRewards lastCampScoreRewards = rewards.get(rewards.size() - 1);
			if(playerScore >= lastCampScoreRewards.getMinScore() ){
				sReward = lastCampScoreRewards;
				
			} else {
				for(CampScoreRewards reward : rewards){
					if( playerScore <= reward.getMaxScore() ){
						sReward = reward;
						break;
					}
				}
				
			}
			
			return sReward;
		}
		
		return null;
	}
	
	/**
	 * 取得阵营官衔奖励
	 * @param camp			阵营
	 * @param campTitle		阵营官衔
	 * @return
	 */
	public CampTitleRewards getCampBattleTitleRewards(Camp camp, CampTitle campTitle){
		return resourceService.getByUnique(IndexName.CAMP_TITLE_REWARD, CampTitleRewards.class, camp.ordinal(), campTitle.ordinal() );
	}
}
