package com.yayo.warriors.module.campbattle.vo;

import java.io.Serializable;


/**
 * 战场信息推送vo
 * @author jonsai
 *
 */
public class BattleInfoVO implements Serializable {
	private static final long serialVersionUID = -2789950565469290273L;
	
	/** 阵营战场信息 */
	private CampBattleVO[] campBattleVOs;
	
	/** 玩家自己的 */
	private PlayerBattleVO playerBattleVO;

	
	//-----------------------
	
	public CampBattleVO[] getCampBattleVOs() {
		return campBattleVOs;
	}

	public void setCampBattleVOs(CampBattleVO[] campBattleVOs) {
		this.campBattleVOs = campBattleVOs;
	}

	public PlayerBattleVO getPlayerBattleVO() {
		return playerBattleVO;
	}

	public void setPlayerBattleVO(PlayerBattleVO playerBattleVO) {
		this.playerBattleVO = playerBattleVO;
	}
	
}
