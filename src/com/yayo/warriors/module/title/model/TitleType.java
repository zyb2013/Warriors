package com.yayo.warriors.module.title.model;

/**
 * 新称号达成条件
 * 
 * @author huachaoping
 */
public enum TitleType {

	//------------------- 等级称号 --------------------
	
	/**
	 * 初涉江湖 角色等级达到10级
	 */
	CHUSHEJIANGHU(1, new TitleCtrl(){

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 10;
		}
	}),
	
	/**
	 * 小试身手 角色等级达到20级
	 */
	XIAOSHISHENSHOU(2, new TitleCtrl(){

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 20;
		}
	}),
	
	/**
	 * 初现锋芒 角色等级达到30级
	 */
	CHUXIANFENGMANG(3, new TitleCtrl(){

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 30;
		}
	}),
	
	/**
	 * 崭露头角	角色等级达到40级
	 */
	ZANLUTOUJIAO(4, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 40;
		}
	}),
	
	/**
	 * 小有名气 角色等级达到50级
	 */
	XIAOYOUMINGQI(5, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 50;
		}
	}),
	
	/**
	 * 仗剑江湖	角色等级达到60级
	 */
	ZHANGJIANJIANGHU(6, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 60;
		}
	}),
	
	/**
	 * 名声鹤起	角色等级达到70级
	 */
	MINGSHENGHEQI(7, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 70;
		}
	}),
	
	/**
	 * 扬名立万	角色等级达到80级
	 */
	YANGMINGLIWAN(8, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 80;
		}
	}),
	
	/**
	 * 威名显赫	角色等级达到90级
	 */
	WEIMINGXIANHE(9, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 90;
		}
	}),
	
	/**
	 * 名传天下	角色等级达到100级
	 */
	MINGCHUANTIANXIA(10, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 100;
		}
	}),
	
	
	//------------------ 击杀BOSS相关称号 ------------------
	
	
	/**
	 * BOSS挑战者	累计击杀BOSS10只
	 */
	BOSSTIAOZHANZHE(11, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 10;
		}
	}),
	
	/**
	 * BOSS猎杀者	累计击杀BOSS50只
	 */
	BOSSLIESHAZHE(12, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 50;
		}
	}),
	
	/**
	 * BOSS灭绝者	累计击杀BOSS100只
	 */
	BOSSMIEJUEZHE(13, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 100;
		}
	}),
	
	/**
	 * BOSS终结者	累计击杀BOSS500只
	 */
	BOSSZHONGJIEZHE(14, new TitleCtrl(){
		
		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 500;
		}
	}),
	
	
	// ----------------- 经脉相关称号 ---------------------
	
	
	/**
	 * 初窥门径   成功贯通一条经脉中的所有穴位
	 */
	CHUKUIMENJING(29, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 1;
		}
	}),
	
	/**
	 * 略知一二   成功贯通两条经脉中的所有穴位
	 */
	LUEZHIYIER(30, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 2;
		}
	}),
	
	/**
	 * 初出茅庐    成功贯通三条经脉中的所有穴位
	 */
	CHUCHUMAOLU(31, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 3;
		}
	}),
	
	/**
	 * 有所小成     成功贯通四条经脉中的所有穴位
	 */
	YOUSUOXIAOCHENG(32, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 4;
		}
	}),
	
	/**
	 * 轻车驾熟      成功贯通五条经脉中的所有穴位
	 */
	QINGCHEJIASHU(33, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 5;
		}
	}),
	
	/**
	 * 登堂入室       成功贯通六条经脉中的所有穴位
	 */
	DENGTANGRUSHI(34, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 6;
		}
	}),
	
	/**
	 * 出类拔萃      成功贯通七条经脉中的所有穴位
	 */
	CHULEIBACUI(35, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 7;
		}
	}),
	
	/**
	 * 有所大成        成功贯通八条经脉中的所有穴位
	 */
	YOUSUODACHENG(36, new TitleCtrl() {

		
		public String alterValue(String value, String param) {
			return param;
		}

		
		public boolean checkObtainTitle(String value) {
			int inValue = Integer.parseInt(value) ;
			return inValue >= 8;
		}
	})
	
	;
	
	int id ;
	TitleCtrl titleCtrl ;
	
	TitleType(int id , TitleCtrl titleCtrl){
		this.id = id ;
		this.titleCtrl = titleCtrl ;
	}
	
	/**
	 * 获取类型编号
	 * @return
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * 修改值
	 * @param value
	 * @param param 
	 * @return
	 */
	public String alterValue(String value, String param) {
		return titleCtrl.alterValue(value, param);
	}

	/**
	 * 检查是否获取称号
	 * @param value
	 * @return
	 */
	public boolean checkObtainTitle(String value) {
		return titleCtrl.checkObtainTitle(value);
	}

	/**
	 * 称号控制器
	 * @author Administrator
	 *
	 */
	interface TitleCtrl{
		/**
		 * 修改值
		 * @param value
		 * @param param 
		 * @return
		 */
		String alterValue(String value, String param) ;
		
		/**
		 * 是否获得称号
		 * @param value
		 * @return
		 */
		boolean checkObtainTitle(String value) ;
	}
}
