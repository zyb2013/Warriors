package com.yayo.warriors.module.meridian.type;

/**
 * 经脉类型枚举
 * 
 * @author huachaopoing
 */
public enum MeridianType {

	/** 0 阳维脉 */
	YANGWEI_MERIDIAN("阳维脉"),                  

	/** 1 阴维脉*/
	YINWEI_MERIDIAN(" 阴维脉"),                   

	/** 2 阳跷脉*/
	YANGQIAO_MERIDIAN("阳跷脉"),                 
	
	/** 3 阴跷脉*/
	YINQIAO_MERIDIAN("阴跷脉"),                   

	/** 4 带脉*/
	DAI_MERIDIAN("带脉"),                       

	/** 5 冲脉*/
	CHONG_MERIDIAN("冲脉"),                     

	/** 6 任脉*/
	JEN_MERIDIAN("任脉"),                       

	/** 7 督脉*/
	GOVERNOR_MERIDIAN("督脉"),                  
	
	// -------------- 以下大周天 -----------------
	
	/** 8 - 精-阳纬脉 */
	BIG_YANGWEI("精-阳纬脉"),                        
	
	/** 9 - 精-阴维脉 */
	BIG_YINWEI("精-阴维脉"),
	
	/** 10 - 精-阳跷脉 */
	BIG_YANGQIAO("精-阳跷脉"),
	
	/** 11 - 精-阴跷脉 */
	BIG_YINQIAO("精-阴跷脉"),
	
	/** 12 - 精-带脉 */
	BIG_DAI("精-带脉"),
	
	/** 13 - 精-冲脉 */
	BIG_CHONG("精-冲脉"),
	
	/** 14 - 精-任脉 */
	BIG_JEN("精-任脉"),
	
	/** 15 - 精-督脉 */
	BIG_GOVERNOR("精-督脉")
	
	;
	
	String name;
	
	
	private MeridianType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
