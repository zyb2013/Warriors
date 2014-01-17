package com.yayo.warriors.type;

/**
 * 公式ID定义类
 * 
 * @author Hyint
 */
public interface FormulaKey {
	
	/** 
	 * 角色基础物攻计算. 
	 * 
	 * <pre>
	 * Math.floor(50+n1*0+n2*0.6). 
	 * n1:等级, n2:力量 
	 */
	int PHYSICAL_ATTACK_FORMULA = 101;
	
	/** 
	 * 角色基础物暴计算. 
	 * 
	 * <pre>
	 * Math.floor(50+n1*0+n2*0.3). 
	 * n1:等级;n2:敏捷 
	 */
	int PHYSICAL_CRITICAL_FORMULA = 102;

	/** 
	 * 角色基础闪避计算. 
	 * 
	 * <pre>
	 * Math.floor(30+n1*0+n2*0.4). 
	 * n1:等级;n2:敏捷 
	 */
	int DODGE_FORMULA = 103;
	
	/** 
	 * 角色HP计算公式. 
	 * 
	 * <pre>
	 * Math.floor(56+n1*0+n2*8).    
	 * n1:等级;n2:体质 
	 */
	int HPMAX_FORMULA = 104;
	
	/** 
	 * 角色基础法攻计算. 
	 * 
	 * <pre>
	 * Math.floor(56+n1*0+n2*8).   
	 * n1:等级;n2:体质 
	 */
	int THEURGY_ATTACK_FORMULA = 105;
	
	/** 
	 * 角色基础法暴计算. 
	 * 
	 * <pre>
	 * Math.floor(60+n1*0+n2*0.25). 
	 * n1:等级;n2:智力 
	 */
	int THEURGY_CRITICAL_FORMULA = 106;
	
	/** 
	 * 角色基础MP计算公式. 
	 * 
	 * <pre>
	 * Math.floor(36+n1*0+n2*3.5).
	 * n1:等级;n2:智力  
	 */
	int MPMAX_FORMULA = 107;
	
	/**
	 * 明教物防计算	
	 * Math.floor(60+(5+n1)*2.1)	
	 * n1:等级
	 */
	int MINGJIAO_PHYSICAL_DEFENSE = 108;
	
	/** 
	 * 唐门物防计算	
	 * Math.floor(60+(5+n1)*2.1)	
	 * n1:等级
	 */
	int TANGMEN_PHYSICAL_DEFENSE = 109;
	
	/** 
	 * 翠烟物防计算	
	 * Math.floor(60+(5+n1)*2.1)	
	 * n1:等级
	 */
	int CUIYAN_PHYSICAL_DEFENSE = 110;
	
	/**	
	 * 丐帮物防计算	
	 * Math.floor(60+(5+n1)*2.1)	
	 * n1:等级
	 */
	int GAIBANG_PHYSICAL_DEFENSE = 111;
	
	/** 
	 * 明教法防计算	
	 * Math.floor(60+(5+n1)*1.8)	
	 * n1:等级
	 */
	int MINGJIAO_THEURGY_DEFENSE = 112;
	
	/**
	 * 唐门法防计算	
	 * Math.floor(60+(5+n1)*1.8)	
	 * n1:等级
	 */
	int TANGMEN_THEURGY_DEFENSE = 113;
	
	/**
	 * 翠烟法防计算	
	 * Math.floor(60+(5+n1)*1.8)	
	 * n1:等级
	 */
	int CUIYAN_THEURGY_DEFENSE = 114;
	
	/**
	 * 丐帮法防计算	
	 * Math.floor(60+(5+n1)*1.8)	
	 * n1:等级
	 */
	int GAIBANG_THEURGY_DEFENSE = 115;

	//----------战斗中计算--------------------------------------------
	
	/** 
	 * 命中率计算. 
	 * <pre>
	 * Math.floor(n2/(n2+n1*40+500)*1000). 
	 * n1:等级; n2:命中 
	 * </pre>
	 */
	int HIT_RATE_FORMULA = 201;
	
	/** 
	 * 闪避率计算. 
	 * <pre>
	 * Math.floor(n2/(n2+n1*40+500)*1000). 
	 * n1:等级;n2:闪避
	 * </pre>
	 */
	int DODGE_RATE_FORMULA = 202;
	
	/** 
	 * 物理暴击率计算.
	 * <pre> 
	 * Math.floor(n2/(n2+n1*40+500)*1000). 
	 * n1:等级;n2:物理暴击 
	 * </pre>
	 */
	int PHYSICAL_CRITICAL_RATE_FORMULA = 203;
	
	/**
	 * 法术暴击计算
	 * <pre>
	 * Math.floor(n2/(n2+n1*40+500)*1000)
	 * n1:等级;	n2:法术暴击
	 * </pre>
	 */
	int THEURGY_CRITICAL_RATE_RATE = 204;
	
	/** 
	 * 战斗命中判断. 
	 * <pre>
	 * Math.floor((1000+n1-n2+(n3-n4)*0)). 
	 * n1:攻击方命中率;n2:防御方闪避率;n3:攻击方等级;n4:防御方等级 
	 * </pre>
	 */
	int FIGHT_HIT_FORMULA = 205;
	
	/** 
	 * 战斗物理暴击判断. 
	 * <pre>
	 * Math.floor(n1+(n2-n3)*0). 
	 * n1:攻击方暴击率;n3:攻击方等级;n4:防御方等级
	 * </pre>
	 */
	int FIGHT_PHYSICAL_FORMULA = 206;

	/** 
	 * 战斗法术暴击判断. 
	 * <pre>
	 * Math.floor(n1+(n2-n3)*0). 
	 * n1:攻击方法术暴击率;n3:攻击方等级;n4:防御方等级
	 * </pre>
	 */
	int FIGHT_THEURGY_CRITICAL_FORMULA = 207;
	
	/** 
	 * 物攻技能普通伤害. 
	 * <pre>
	 * Math.floor(Math.max(n1-n2,n1*0.1)*Math.max(1+(n3-n4)*0,0))
	 * n1:物理攻击力;n2:物理防御力;n3:攻击方等级;n4:防御方等级 
	 * </pre>
	 */
	int PHYSICAL_ATTACK_DAMAGE_FORMULA = 208;

	/** 
	 * 法攻技能普通伤害
	 * <pre>
	 * Math.floor(Math.max(n1-n2,n1*0.1)*Math.max(1+(n3-n4)*0,0))
	 * n1:法术攻击力;n2:法术防御力;n3:攻击方等级;n4:防御方等级
	 * </pre>
	 */
	int THEURGY_ATTACK_DAMAGE_FORMULA = 209;
	
	/**
	 * 物攻技能暴击伤害
	 * <pre>
	 * Math.floor(Math.max(n1-n2,n1*0.1)*Math.max(1+(n3-n4)*0,0))*2
	 * n1:物理攻击力;n2:物理防御力;n3:攻击方等级;n4:防御方等级
	 * </pre>
	 */
	int PHYSICAL_CRITICAL_DAMAGE_FORMULA = 210;
	
	/**
	 * 法攻技能暴击伤害
	 * <pre>
	 * Math.floor(Math.max(n1-n2,n1*0.1)*Math.max(1+(n3-n4)*0,0))*2
	 * n1:法术攻击力;n2:法术防御力;n3:攻击方等级;n4:防御方等级
	 * </pre>
	 */
	int THEURGY_CRITICAL_DAMAGE_FORMULA = 211;
	
	/** 
	 * 213 - 战斗眩晕判断
	 * n1:(技能组合表)效果触发几率 ； n2:防御方抗眩晕属性值
	 */
	int IMMOBILIZE_DEFENSE_FORMULA = 213;
	//---------------------------------------------
	
	/**
	 * 家将升级公式(家将30级之前所需要的升级经验值)
	 * <pre>
	 * Math.floor(2*Math.pow(n1,3.5)+n1*50+10)
	 * n1:等级
	 * </pre>
	 */
	int PET_LEVEL_UP_FORMULA_BEFORE = 301;
	
	/**
	 * 家将品质转换属性公式
	 * <pre>
	 * Math.floor(n1+n1*n2)
	 * n1:家将类型基础属性系数
	 * n2:家将基础资质
	 * </pre>
	 */
	int PET_QUALITY_ATTRIBUTE_FORMULA = 302;
	
	/**
	 * 家将等级增加一级属性
	 * <pre>
	 * Math.floor((n2-1)*(n1/8*(n3*0.01)))
	 * n1:家将类型成长属性系数
     * n2:家将等级
	 * </pre>
	 */
	int PET_LEVEL_ATTRIBUTE_FORMULA = 303;
	
	/**
	 * 家将成长增加一级属性（原来是家将成长，现在废弃）
	 * <pre>
	 * Math.floor((n2-1)*n2)
	 * n1:家将类型成长属性系数
	 * n2:家将成长等级
	 * </pre>
	 */
	@Deprecated
	int PET_GROW_ATTRIBUTE_FORMULA = 304;
	
	/**
	 * 家将成长增加二级属性
	 * <pre>
	 * Math.floor(n1*n2)
     * n1:家将类型悟性属性系数
     * n2:家将悟性等级
	 * </pre>
	 */
	int PET_SAVVY_ATTRIUTE_FORMULA = 305;
	
	/**
	 * 打坐经验奖励
	 * <pre>
	 * Math.floor(n1*n2)
	 * n1:玩家角色等级
	 * n2:打坐时间(秒)
	 * </pre>
	 */
	int TRAIN_EXP_FORMULA = 306;
	
	/**
	 * 打坐真气奖励
	 * <pre>
	 * Math.floor(n1/5*1)
	 * n1:打坐时间(秒)
	 * </pre>
	 */
	int TRAIN_GAS_FORMULA = 307;
	
	/** 
	 * 角色组队自己杀怪经验获得	
	 * 308 - Math.floor(n1*(1+0.05*(n2-1)))	 
	 * n1:怪物经验；n2：队友人数 
	 */
	int TEAM_PLAYER_ATTACK_EXP = 308;

	/** 
	 * 角色组队队友杀怪经验获得	
	 * 309 - Math.floor(n1*(1+0*(n2-1))*0.4)	
	 * n1:怪物经验；n2：队友人数 
	 */
	int TEAM_PLAYER_SHAREEXP = 309;
	
	/**
	 * 玩家自身点脉经验奖励
	 * <pre>
	 * Math.floor((n1^n1*12+n1*500)*10)
	 * n1:玩家等级
	 * </pre>
	 */
	int MERIDIAN_GET_EXP_FORMULA = 310;
	
	/**
	 * 经脉突破玩家获得经验
	 * <pre>
	 * Math.floor((n1^n1*12+n1*500)*1)
	 * n1:玩家等级
	 * </pre>
	 */
	int PLAYER_ADD_EXP_FORMULA = 311;
	
	
	/**
	 * VIP祝福经验奖励
	 * <pre>
	 * Math.floor((n1^n1*2+n1*50)*1)
	 * n1:玩家等级
	 * </pre>
	 */
	int PLAYER_VIP_BLESS_FORMULA = 312;
	
	/**
	 * 遣散家将获得的家将历练经验值
	 * <pre>
	 * Math.floor(n1*100+n2)
	 * n1: 家将等级
	 * n2：家将资质
	 * </pre>
	 */
	int PET_FREE_EXP = 313;
	
	/**
	 * 家将继承后等级计算
	 * 314 - Math.floor((n1-n2)/3+n2)
	 * n1:被继承家将的等级
	 * n2:继承家将的等级
	 */
	int PET_EXTEND_LEVEL = 314;
	
	/**
	 * 家将继承后的资质计算
	 * 315 - Math.max(n1,n2)
	 * n1:被继承家将的资质
	 * n2:继承家将的资质
	 */
	int PET_EXTEND_QUALITY = 315;
	
	/**
	 * 家将继承后成长计算
	 * 316 - Math.floor((n1-n2)/3+n2)
	 * n1:被继承家将的成长
	 * n2:继承家将的成长
	 */
	int PET_EXTEND_SAVVY   = 316;
	
	/**
	 * 家将修炼历练值获得计算(家将30级前的历练值修炼公式不包含30级)
	 * 317 - Math.floor((2*Math.pow(n1,3.6)+n1*50+48)/(5*24*60)*n2)
	 * n1:角色等级
	 * n2:修炼时间(分钟)
	 */
	int PET_TRAING_EXP_BEFORE = 317;
	
	/**
	 * 家将升级公式(家将30级之后所需要的升级经验值)
	 * <pre>
	 * Math.floor(2*Math.pow(n1,3.5)+n1*50+10)
	 * n1:等级
	 * </pre>
	 */
	int PET_LEVEL_UP_FORMULA_AFTER = 318;
	
	
	/**
	 * 家将修炼历练值获得计算(家将30级后的历练值修炼公式包含30级)
	 * 319 - Math.floor((2*Math.pow(n1,3.6)+n1*50+48)/(5*24*60)*n2)
	 * n1:角色等级
	 * n2:修炼时间(分钟)
	 */
	int PET_TRAING_EXP_AFTER = 319;

	/**
	 * 角色攻击怪物, 等级压制公式
	 * 
	 * 320 - Math.max((1+(n1-n2)/20),0.5)
	 * n1:角色等级 ; n2：怪物等级
	 */
	int PLAYER_FIGHT_EXP_RATE = 320;
	
	/**
	 * 好友饮酒经验奖励
	 * 
	 * 321 - Math.floor(Math.pow(n1,2.5)+10*n2+100)"
	 * n1:角色等级, n2:酒量
	 */
	int FRIEND_WINE_EXP_REWARD = 321;
	
	/**
	 * 好友敬酒经验奖励
	 * 
	 * 322 - Math.floor(Math.pow(n1,2)
	 * n1:角色等级
	 */
	int GREET_FRIENDS_EXP_REWARD = 322;
	
	/** 
	 * 角色的最大等级
	 * 323 - 直接一个等级
	 */
	int MAX_PLAYER_LEVEL_LIMIT = 323;
	//------------------------------------------------
	
	/** 
	 * 装备升星消耗	
	 * 501	Math.floor(n1*n2)	
	 * n1:装备基础价格(装备基础表里取),n2:升星银币消耗系数(装备强化表里取)
	 */
	int ASCENT_STAR_COSTSILVER = 501;
	
	/** 
	 * 装备洗练消耗
	 * 502	Math.floor(n1*n2)	
	 * n1:装备基础价格(装备基础表里取), n2:洗练银币消耗系数(装备洗练表里取)
	 */
	int EQUIP_POLISH_COSTSILVER = 502;
	
	/** 
	 * 装备镶嵌消耗	
	 * 503	Math.floor(n1*1.32)	
	 * n1:装备基础价格(装备基础表里取) 
	 */
	int EQUIP_ENCHANGE_COSTSILVER = 503;
	
	/** 
	 * 装备精练消耗	
	 * 504 Math.floor(n1*2.2)
	 * n1:装备基础价格(装备基础表里取) 
	 */
	int EQUIP_REFINE_COSTSILVER = 504;
	
	/** 
	 * 装备升级消耗	
	 * 505	Math.floor(n1*n2)	
	 * n1:装备基础价格（装备基础表里取), n2:升级银币消耗系数(装备升级表里取)
	 */
	int EQUIP_ASCENT_RANK_COSTSILVER = 505;
	
	/**
	 * 帮派捐钱(游戏币)能够获得的捐献值
	 * 506 Math.floor(n1/100000)
	 * n1:所捐的钱(游戏币)
	 */
	int ALLIANCE_DONATE_FORMULA = 506;
	
	/**
	 * 装备继承各星级消耗铜币
	 * 508 - Math.floor(n1*n2)	
	 * 
	 * 508 - Math.floor(1000/n1*n2*n3)	
	 * 
	 * n1:升星成功几率（在装备强化表取rate字段;
	 * n2:继承装备基础价格(装备基础表里取)
	 * n3:升星消耗系数(在装备强化表取silver字段)
	 */
	int EQUIP_EXTENDS_LEVEL_FORMULA = 508;
	//------------------------------------------------
	
	
	/**
	 * 背包扩展消耗
	 * <pre>
	 * Math.floor(2*Math.pow(n1,2.33))
	 * n1:当前背包最大页数 
	 * </pre>
	 */
	int BACKPACK_OPEN_FORMULA = 601;
	
	/**
	 * 仓库扩展消耗
	 * <pre>
	 * Math.floor(Math.pow(n1-1,2))
	 * n1:当前仓库最大页数 
	 * </pre>
	 */
	int STORAGE_OPEN_FORMULA = 603;
	
	/**
	 * 家将栏扩展道具消耗数量
	 * 602  Math.floor((n1-1)^2)
	 * n1:当前槽数
	 */
	int PET_OPEN_SOLT_FORMULA = 602;
	
	/** 602	Math.floor((n1-1)^2)	家将栏扩展道具消耗数量	n1:被扩展家将栏数量 */
	
	/** 603	Math.floor((n1-2)^2)	仓库栏扩展道具消耗数量	n1:被扩展仓库栏数量 */
	
	/** 
	 * 装备维修消耗铜币	
	 * 604	Math.floor(3*(1-n1/n2)*n3)	
	 * n1:装备当前耐久, n2：装备耐久, n3：装备价格 
	 */
	int EQUIP_REPAIR_COST_SILVER = 604;
	
	/**
	 * 天龙战斗力计算	
	 * 701 - Math.floor(n1/0.7+n2/0.6+n3/0.3+n4/0.4+n5/0.4+n6/8+n7/0.7+n8/3.5)	
	 * n1:物攻, n2:物防, n3:物暴, n4:命中, n5:闪避, n6:HP最大值, n7:法防, n8:MP最大值
	 */
	int TIANLONG_FIGHT_CAPACITY = 701;
	
	/** 
	 * 天山战斗力计算	
	 * 702 - Math.floor(n1/0.7+n2/0.6+n3/0.3+n4/0.4+n5/0.4+n6/8+n7/0.7+n8/3.5)	
	 * n1:物攻, n2:物防, n3:物暴, n4:命中, n5, 闪避, n6:HP最大值, n7:法防, n8:MP最大值
	 */
	int TIANSHAN_FIGHT_CAPACITY = 702;
	
	/**
	 * 星宿战斗力计算	
	 * 703 - Math.floor(n1/0.8+n2/0.7+n3/0.25+n4/0.4+n5/0.4+n6/8+n7/0.6+n8/3.5)	
	 * n1:法攻, n2:法防, n3:法暴, n4:命中, n5:闪避, n6:HP最大值, n7:物防, n8:MP最大值 
	 */
	int XINGXIU_FIGHT_CAPACITY = 703;
	
	/**
	 * 逍遥战斗力计算	
	 * 704 - Math.floor(n1/0.8+n2/0.7+n3/0.25+n4/0.4+n5/0.4+n6/8+n7/0.6+n8/3.5)	
	 * n1:法攻, n2:法防, n3:法暴, n4:命中, n5:闪避, n6:HP最大值, n7:物防, n8:MP最大值
	 */
	int XIAOYAO_FIGHT_CAPACITY = 704;
	
	/** 
	 * 装备战斗力-附加属性(天龙)	
	 * 705 - n1+n2+n3+n4+n5	
	 * 天龙   n1:力量   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（701）计算出的结果
	 */
	int TIANLONG_EQUIP_ADDITION_CAPACITY = 705;
	
	/** 
	 * 装备战斗力-附加属性(天山)	
	 * 706 - n1+n2+n3+n4+n5	
	 * 天山   n1:力量   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（702）计算出的结果
	 */
	int TIANSHAN_EQUIP_ADDITION_CAPACITY = 706;
	
	/** 
	 * 装备战斗力-附加属性(星宿)	
	 * 707 - n1+n2+n3+n4+n5	
	 * 星宿   n1:精神   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（703）计算出的结果 
	 * */
	int XINGXIU_EQUIP_ADDITION_CAPACITY = 707;
	
	/** 
	 * 装备战斗力-附加属性(逍遥)	
	 * 708 - n1+n2+n3+n4+n5	
	 * 逍遥   n1:精神   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（704）计算出的结果
	 */
	int XIAOYAO_EQUIP_ADDITION_CAPACITY = 708;
	 
	/** 
	 * 装备战斗力-装备基础, 装备基础属性计算出的战斗力  
	 * 711	n1+n2	
	 * n1：一级属性总和  n2:二级属性总和按照公式计算出的值
	 */
	int EQUIP_BASE_CAPACITY = 711;
	/**
	 * 装备战斗力-装备星级	装备强化增加的属性、星级套装属性计算出的战斗力 
	 * 712	n1+n2+n3	
	 * n1：一级属性总和  n2:二级属性总和按照公式计算出的值  
	 * n3：装备强化激活的星级套装属性按公式（701、702、703、704）计算出的战斗力
	 */
	int EQUIP_STAR_CAPACITY = 712;

	/** 
	 * 坐骑战斗力-坐骑星级(天龙)	
	 * 715 - Math.floor(5*(n1/4.2+n2/4.2+n3/3+n4/8+n5/8+n6/20+n7/4.2+n8/25))	
	 * n1:物攻；n2:物防；n3:物暴；n4:命中；n5:闪避；n6:HP上限；n7:法防；n8:MP上限；
	 */
	int TIANLONG_HORSE_CAPACITY = 715;
	
	/**
	 * 坐骑战斗力-坐骑星级(天山)	
	 * 716 - Math.floor(5*(n1/4.2+n2/4.2+n3/3+n4/8+n5/8+n6/20+n7/4.2+n8/25))	
	 * n1:物攻；n2:物防；n3:物暴；n4:命中；n5:闪避；n6:HP上限；n7:法防；n8:MP上限；
	 */
	int TIANSHAN_HORSE_CAPACITY = 716;
	
	/**
	 * 坐骑战斗力-坐骑星级(星宿)	
	 * 717 - Math.floor(5*(n1/4.2+n2/4.2+n3/3+n4/8+n5/8+n6/20+n7/4.2+n8/25))	
	 * n1:法攻；n2:法防；n3:法暴；n4:命中；n5:闪避；n6:HP上限；n7:物防；n8:MP上限；
	 */
	int XINGXIU_HORSE_CAPACITY = 717;
	
	/**
	 * 坐骑战斗力-坐骑星级(逍遥)	
	 * 718 - Math.floor(5*(n1/4.2+n2/4.2+n3/3+n4/8+n5/8+n6/20+n7/4.2+n8/25))	
	 * n1:法攻；n2:法防；n3:法暴；n4:命中；n5:闪避；n6:HP上限；n7:物防；n8:MP上限；
	 */
	int XIAOYAO_HORSE_CAPACITY = 718;

	/**
	 * 天龙战斗力计算	
	 * 720 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))
	 * n1:物攻, n2:物防, n3:物暴, n4:命中, n5:闪避, n6:HP最大值, n7:法防, n8:MP最大值
	 */
	int TIANLONG_RATIO_FIGHT_CAPACITY = 720;
	
	/** 
	 * 天山战斗力计算	
	 * 721 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))
	 * n1:物攻, n2:物防, n3:物暴, n4:命中, n5, 闪避, n6:HP最大值, n7:法防, n8:MP最大值
	 */
	int TIANSHAN_RATIO_FIGHT_CAPACITY = 721;
	
	/**
	 * 星宿战斗力计算	
	 * 722 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))
	 * n1:法攻, n2:法防, n3:法暴, n4:命中, n5:闪避, n6:HP最大值, n7:物防, n8:MP最大值 
	 */
	int XINGXIU_RATIO_FIGHT_CAPACITY = 722;
	
	/**
	 * 逍遥战斗力计算	
	 * 723 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))
	 * n1:法攻, n2:法防, n3:法暴, n4:命中, n5:闪避, n6:HP最大值, n7:物防, n8:MP最大值
	 */
	int XIAOYAO_RATIO_FIGHT_CAPACITY = 723;
	
	/** 
	 * 外功型家将战斗力计算	
	 * 730	Math.floor(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5)	
	 * n1:物攻；n2:物防；n3:物暴；n4:命中；n5:闪避；n6:HP上限；n7:法防；n8:MP上限；
	 */
	int PET_WAIGONG_FIGHTING = 730;
	/** 
	 * 内功型家将战斗力计算
	 * 731 - Math.floor(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5)
	 * n1:法攻；n2:法防；n3:法暴；n4:命中；n5:闪避；n6:HP上限；n7:物防；n8:MP上限；
	 */
	int PET_NEIGONG_FIGHTING = 731;
	
	/** 
	 * 装备战斗力-宝石加成(天龙)	
	 * 740 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))	
	 * n1:物攻；n2:物防；n3:物暴；n4:命中；n5:闪避；n6:HP上限；n7:法防；n8:MP上限；
	 */
	int EQUIP_ENCHANGE_TILONG_CAPACITY = 740;
	
	/** 
	 * 装备战斗力-宝石属性(天山)	
	 * 741 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))	
	 * n1:物攻；n2:物防；n3:物暴；n4:命中；n5:闪避；n6:HP上限；n7:法防；n8:MP上限；
	 */
	int EQUIP_ENCHANGE_TIANSHAN_CAPACITY = 741;
	
	/** 
	 * 装备战斗力-宝石属性(星宿)	
	 * 742 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))	
	 * n1:法攻；n2:法防；n3:法暴；n4:命中；n5:闪避；n6:HP上限；n7:物防；n8:MP上限；
	 */
	int EQUIP_ENCHANGE_XINGXIU_CAPACITY = 742;

	/** 
	 * 装备战斗力-宝石属性(逍遥)	
	 * 743 - Math.floor(5*(n1/4.2+n2/4.2+n3/0.3+n4/0.4+n5/0.4+n6/20+n7/4.2+n8/5))
	 * n1:法攻；n2:法防；n3:法暴；n4:命中；n5:闪避；n6:HP上限；n7:物防；n8:MP上限；
	 */
	int EQUIP_ENCHANGE_XIAOYAO_CAPACITY = 743;

	
	/**
	 * 藏宝开怪者获得经验
	 * 801 - Math.floor(n1/2)
	 * n1:怪物经验
	 */
	int TREASURE_FRESH_MONSTER_EXP = 801;
	
	/**
	 * 藏宝杀怪者获得经验
	 * 802 - Math.floor(n2/n3*n1)
	 * n1:怪物经验  n2:怪物等级 n3:人物等级
	 */
	int TREASURE_KILL_MONSTER_EXP = 802;
	
	/**
	 * 藏宝图刷新铜币消耗
	 * 803 - Math.floor(n1*n1+100)	
	 * n1:人物等级
	 */
	int TREASURE_REFRESH_QUALITY_COST_COIN = 803;
	
	/**
	 * 闭关经验所得
	 * <pre>
	 * 804 - Math.floor((Math.pow(n1,1.2)+5*n1-1)*n2*3)
	 * n1:人物等级    n2:闭关时间(分钟)
	 * </pre>
	 */
	int OFFLINE_TRAINING_EXP = 804;
	
	/**
	 * 闭关真气所得
	 * <pre>
	 * 805 - Math.floor(n1/5*20)
	 * n1:闭关时间(分钟)
	 * </pre>
	 */
	int OFFLINE_TRAINING_GAS = 805;
	
	/**
	 * 闭关消耗(道具数量)
	 * <pre>
	 * 809 - Math.floor(2*(n1-1))
	 * n1:倍数
	 * </pre>
	 */
	int OFFLINE_TRAINING_CONSUME = 809;
	
	/**
	 * 截取押镖任务的经验
	 * Math.floor(n1*0.2)
	 */
	int PLUNDER_ESCORT_EXP = 810;
	
	/**
	 * 押镖玩家用保护令未被截取状态
	 * Math.floor(n1*1.2)
	 */
	int ESCORT_PROTECT_UNPLUNDER = 811;
	
	
	/**
	 * 押镖玩家未用保护令被截取状态
	 * Math.floor(n1*0.8)
	 */
	int ESCORT_UNPROTECT_PLUNDER = 812;
	
	
	/**
	 * 押镖玩家已用保护令被截取状态
	 * Math.floor(n1*1.0)
	 */
	int ESCORT_PROTECT_PLUNDER = 813;
	
	
	/**
	 * 战场击杀敌对玩家积分公式
	 * <pre>
	 * 850 - Math.min(Math.round(n1/10000),100)
	 * n1:击杀人数
	 * </pre>
	 */
	int CAMP_BATTLE_KILL_PLAYER_SCORE = 850;
	
	/**
	 * 战场杀怪个人积分公式
	 * 851 - Math.floor(1*n1)
	 * n1:怪物损失的血量
	 */
	int CAMP_BATTLE_HURT_BOSS_SCORE = 851;
	
	/**
	 * 战场杀怪阵营公式
	 * 852 - Math.round(n1/10000)
	 * n1:对方怪物损失的血量
	 */
	int CAMP_BATTLE_CAMP_HURT_BOSS_SCORE = 852;
	
}
