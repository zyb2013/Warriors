package com.yayo.warriors.module.mail.model;

import java.util.Date;

import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 领取条件
 * 
 * @author huachaoping
 */
public enum ReceiveCondition {
	
	/** 人物等级 */
	USER_LEVEL(0, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			PlayerBattle battle = (PlayerBattle) entity;
			if (cons[1].equals(ConditionType.ABOVE)) {
				return battle.getLevel() > Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.EQUAL)) {
				return battle.getLevel() == Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.LESS)) {
				return battle.getLevel() < Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.ABOVE_OR_EQUAL)) {
				return battle.getLevel() >= Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.LESS_OR_EQUAL)) {
				return battle.getLevel() <= Integer.valueOf(cons[2]);
			}
			return false;
		}
	}),
	
	/** 最后充值时间 */
	LAST_CHARGE_TIME(1, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			return false;
		}
	}),
	
	/** 帮派名称 */
	ALLIANCE_NAME(2, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			PlayerAlliance playerAlliance = (PlayerAlliance) entity;
			return playerAlliance.getAllianceName().equals(cons[2]);
		}
	}),
	
	/** 帮派等级 */
	ALLIANCE_LEVEL(3, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			Alliance alliance = (Alliance) entity;
			if (cons[1].equals(ConditionType.ABOVE)) {
				return alliance.getLevel() > Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.EQUAL)) {
				return alliance.getLevel() == Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.LESS)) {
				return alliance.getLevel() < Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.ABOVE_OR_EQUAL)) {
				return alliance.getLevel() >= Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.LESS_OR_EQUAL)) {
				return alliance.getLevel() <= Integer.valueOf(cons[2]);
			}
			return false;
		}
	}),
	
	/** 注册时间 */
	REGISTER_TIME(4, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			Player player = (Player) entity;
			if (cons[1].equals(ConditionType.ABOVE)) {
				return player.getCreateTime().after(new Date(Long.valueOf(cons[2])));
			} else if(cons[1].equals(ConditionType.LESS)) {
				return player.getCreateTime().before(new Date(Long.valueOf(cons[2])));
			}
			return false;
		}
	}),
	
	/** 最后登录时间 */
	LAST_LOGIN_TIME(5, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			Player player = (Player) entity;
			if (cons[1].equals(ConditionType.ABOVE)) {
				return player.getLoginTime().after(new Date(Long.valueOf(cons[2])));
			} else if(cons[1].equals(ConditionType.LESS)) {
				return player.getLoginTime().before(new Date(Long.valueOf(cons[2])));
			}
			return false;
		}
	}),
	
	/** 登录天数 */
	LOGIN_DAY(6, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			Player player = (Player) entity;
			if (cons[1].equals(ConditionType.ABOVE)) {
				return player.getLoginDays() > Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.ABOVE_OR_EQUAL)) {
				return player.getLoginDays() >= Integer.valueOf(cons[2]);
			} 
			return false;
		}
	}),
	
	
	/** 金币数 */
	USER_GOLDEN(7, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			Player player = (Player) entity;
			if (cons[1].equals(ConditionType.ABOVE)) {
				return player.getGolden() > Integer.valueOf(cons[2]);
			} else if (cons[1].equals(ConditionType.ABOVE_OR_EQUAL)) {
				return player.getSilver() >= Integer.valueOf(cons[2]);
			} 
			return false;
		}
	}),
	
	/** 铜币数 */
	USER_SILVER(8, new ConditionCtrl() {
		
		public boolean checkCondition(Object entity, String[] cons) {
			Player player = (Player) entity;
			if (cons[1].equals(ConditionType.ABOVE)) {
				return player.getSilver() > Integer.valueOf(cons[2]);
			}  else if (cons[1].equals(ConditionType.ABOVE_OR_EQUAL)) {
				return player.getSilver() >= Integer.valueOf(cons[2]);
			} 
			return false;
		}
	}),
	
	;
	
	/** 条件ID */
	private int id;
	/** 条件控制器 */
	private ConditionCtrl conCtrl;
	

	private ReceiveCondition(int id, ConditionCtrl conCtrl) {
		this.id = id;
		this.conCtrl = conCtrl;
	}
	
	public int getId() {
		return id;
	}

	public boolean isReachCondition(Object entity, String[] cons) {
		return conCtrl.checkCondition(entity, cons);
	}
	
	public static ReceiveCondition getElementEnumById(int id){
		for (ReceiveCondition element : ReceiveCondition.values()) {
			if (element.getId() == id) {
				return element;
			}
		}
		return null ;
	}
	
	
	interface ConditionCtrl {
		
		/**
		 * 判断是否达成条件
		 * @param con          自身条件
		 * @param cons         达成条件
		 * @return {@link Boolean}
		 */
		boolean checkCondition(Object entity, String[] cons);
	}

	
}
