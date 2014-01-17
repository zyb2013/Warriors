package com.yayo.warriors.module.vip.model;

import com.yayo.warriors.basedb.model.VipConfig;


public enum VipFunction {

	MonsterExpPercent(1,"打怪经验额外加成",new ValueConverter(){

		@SuppressWarnings("unchecked")
		
		public Float convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getMonsterExpPercent();
		}
	}),
	
	MeditationExpPercent(2,"打坐经验额外加成",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Float convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getMeditationExpPercent();
		}
	}),
	
	FlyingShoes(3,"每日免费使用飞鞋次数",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Integer convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getDailyUseFlyShoes();
		}
	}),
	
	FlyingShoesRemainTimes(4,"每日免费使用飞鞋剩余次数",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Integer convert(VipDomain vipDomain) {
			int flyingShoesTime = FlyingShoes.getValue(vipDomain, Integer.class) ;
			if(flyingShoesTime == -1) return -1 ;
			int usedTimes = vipDomain.getParameters(FlyingShoes);
			return flyingShoesTime - usedTimes;
		}
	}),
	
	OpenRemoteShop(5,"是否开启远程商店",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Boolean convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getRemoteShop() == 1;
		}
	}),
	
	OpenRemoteStoreHouse(6,"是否开启远程仓库",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Boolean convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getRemoteStorage() == 1;
		}
	}),
	
	MeditationGasPercent(7,"打坐真气加成",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Float convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getMeditationGasPercent();
		}
	}),
	
	DailyTaskExperience(8,"日常任务经验额外加成",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Float convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getDailyTaskExperience();
		}
	}),
	
	EscortTaskExperience(9,"护送任务经验额外加成",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Float convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig() ;
			return vipConfig.getEscortTaskExperience();
		}
	}),
	
//	/** 阵营任务验额外加成*/
//	CampTaskExperience(10,"阵营任务经验额外加成",new ValueConverter() {
//
//		@SuppressWarnings("unchecked")
//		
//		public Float convert(VipDomain vipDomain) {
//			VipConfig vipConfig = vipDomain.getVipConfig() ;
//			return vipConfig.getCampTaskExperience();
//		}
//	}),
	
//	/** 家将培养额外加成*/
//	PetCulturePercent(11,"家将培养额外加成",new ValueConverter() {
//
//		@SuppressWarnings("unchecked")
//		
//		public Float convert(VipDomain vipDomain) {
//			VipConfig vipConfig = vipDomain.getVipConfig() ;
//			return vipConfig.getPetCulturePercent();
//		}
//	}),
	
	DailyHorseReward(12,"每日领取坐骑经验丹数量",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Integer convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig();
			return vipConfig.getDailyHorseReward();
		}
	}),
	
	DailyTaskReward(13,"每日领取日环刷新符数书",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Integer convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig();
			return vipConfig.getDailyTaskReward();
		}
	}),
	
	BlessExperience(14,"是否开启VIP祝福经验",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Boolean convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig();
			return vipConfig.getBlessExperience() == 1;
		}
	}),
	
	RecieveBlessExp(15,"领取VIP祝福经验",new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Boolean convert(VipDomain vipDomain) {
			int received = vipDomain.getParameters(RecieveBlessExp);
			return received == 1;
		}
	}),
	
	ReceiveHorseReward(16, "领取坐骑经验丹奖励", new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Boolean convert(VipDomain vipDomain) {
			int received = vipDomain.getParameters(ReceiveHorseReward);
			return received == 1;
		}
	}),
	
	ReceiveTaskReward(17, "领取日环刷新书奖励", new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Boolean convert(VipDomain vipDomain) {
			int receive = vipDomain.getParameters(ReceiveTaskReward);
			return receive == 1;
		}
	}),
	
	VipOutOfDateTime(18, "VIP有效时间毫秒", new ValueConverter() {
		
		@SuppressWarnings("unchecked")
		
		public Long convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig();
			return vipConfig.getVipOutOfDateMillis();
		}
	}),
	
	PracticeTaskExperience(19, "试炼任务经验加成", new ValueConverter() {
		
		@SuppressWarnings("unchecked")
		
		public Float convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig();
			return vipConfig.getPracticeTaskExperience();
		}
	}),
	
	DailyVipGift(20, "VIP每日礼包", new ValueConverter() {

		@SuppressWarnings("unchecked")
		
		public Integer convert(VipDomain vipDomain) {
			VipConfig vipConfig = vipDomain.getVipConfig();
			return vipConfig.getDailyVipGift();
		}
	}),
	
	ReceiveVipGift(21, "是否已领取VIP礼包", new ValueConverter() {
		
		@SuppressWarnings("unchecked")
		
		public Boolean convert(VipDomain vipDomain) {
			int receive = vipDomain.getParameters(ReceiveVipGift);
			return receive == 1;
		}
	}),
	
	;
	
	int id ;
	String name ;
	ValueConverter valueConverter ;
	
	private VipFunction(int id , String name , ValueConverter valueConverter ) {
		this.id = id ;
		this.name = name ;
		this.valueConverter = valueConverter;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public static VipFunction getElementEnumById(int id){
		for(VipFunction elementEnum : VipFunction.values()){
			if(elementEnum.getId() == id){
				return elementEnum ;
			}
		}
		return null ;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(VipDomain vipDomain, Class<T> clazz) {
		return (T) valueConverter.convert(vipDomain);
	}
	
	interface ValueConverter {
		<T> T convert(VipDomain vipDomain);
	}
}
