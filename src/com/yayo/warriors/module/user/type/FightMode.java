package com.yayo.warriors.module.user.type;


/**
 * 角色的状态模式
 * 
 * @author Hyint
 */
public enum FightMode {
	
	/** 0 - 和平模式. 指定等级下不能切换模式. 和平模式可以被攻击, 只要不再保护等级之下则可以攻击 */
	PEACE(new ModeValidator() {

		
		public boolean canBeAttack(int playerLevel) {
			return playerLevel > 20;
		}

		
		public boolean canChangeMode(int playerLevel) {
			return playerLevel >= 0;
		}
	}),
	
	/** 1 - 杀戮模式. 可以攻击任何人, 只要对方不处于保护状态既可以攻击.  */
	KILLING(new ModeValidator() {
		
		public boolean canBeAttack(int playerLevel) {
			return playerLevel >= 0;
		}

		
		public boolean canChangeMode(int playerLevel) {
			return playerLevel > 20;
		}
	}),
	
	/** 2 - 阵营模式. 相同阵营的人不能攻击, 只能攻击对立阵营的人 */
	CAMPING(new ModeValidator() {
		
		public boolean canBeAttack(int playerLevel) {
			return playerLevel >= 0;
		}

		
		public boolean canChangeMode(int playerLevel) {
			return playerLevel >= 30;
		}
	});
	
	/** 模式验证器 */
	private ModeValidator modeValidator;
	
	FightMode(ModeValidator modeValidator) {
		this.modeValidator = modeValidator;
	}

	public boolean canBeAttack(int playerLevel) {
		return modeValidator.canBeAttack(playerLevel);
	}

	public boolean canChangeMode(int playerLevel) {
		return modeValidator.canChangeMode(playerLevel);
	}
	
	/**
	 * 模式切换验证器
	 * 
	 * @author Hyint
	 */
	interface ModeValidator {
		/** 
		 * 是否可以被攻击
		 * 
		 * @param  playerLevel		角色的等级
		 * @return {@link Boolean}	true-可以被攻击, false-不可以被攻击
		 */
		boolean canBeAttack(int playerLevel);
		
		/**
		 * 是否可以切换模式
		 * 
		 * @param  playerLevel		角色的等级
		 * @return {@link Boolean}	true-可以切换, false-不可以切换
		 */
		boolean canChangeMode(int playerLevel);
	}
}

