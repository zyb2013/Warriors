package com.yayo.warriors.basedb.model;

import java.util.Date;

import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.module.props.type.BindingType;
import com.yayo.warriors.module.props.type.EquipType;
import com.yayo.warriors.module.props.type.ExpirateType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.user.type.Job;

/**
 * 物品信息公用父类
 * 
 * @author Hyint
 */
public class GoodsParent {
	
	public static final String PROPS_LEVEL = "GOODS_PROPS_LEVEL";
	/** 
	 * 基础物品名 
	 */
	private String name;

	/**
	 * 物品的等级
	 */
	@Index(name=PROPS_LEVEL, order=0)
	private int level;
	
	/** 
	 * 物品的主类型.
	 * 
	 * <pre>
	 * 如果子类是装备: {@link EquipType}
	 * 如果子类是道具: {@link PropsType}
	 * </pre>
	 */
	private int propsType;
	
	/** 
	 * 物品的游戏币(银币)价格. 出售的时候使用 
	 */
	private int silverPrice = 0;
	
	/**
	 * 商城价格(商城购买物品)
	 */
	private int mallPrice = 0;
	
	/**
	 * VIP商城价格(VIP购买物品)
	 */
	private int vipPrice = 0;
	
	/**
	 * 商城物品礼金价格. 为0则表示不能购买
	 */
	private int couponPrice = 0;
	
	/** 
	 * 是否可以出售给系统. false-不可出售, true-可出售
	 */
	private boolean canSales = false;
	
	/** 
	 * 背包陈列顺序 
	 */
	private int packSort = 0;

	/** 
	 * 角色的职业 
	 */
	private int job = Job.COMMON.ordinal();
	
	/** 装备的品质 */
	@Index(name=PROPS_LEVEL, order=1)
	private int quality = Quality.WHITE.ordinal();

	/**
	 * 绑定类型
	 * 
	 * <pre>
	 *   0-不绑定
	 *   1-拾取绑定
	 *   2-打造绑定
	 *   3-穿着绑定
	 * </pre>
	 */
	private int bindingType = BindingType.UNBINDING.ordinal();
	
	/** 
	 * 时效类型. 
	 * 
	 * @see ExpirateType 
	 */
	private int expirateType = 0;
	
	/**
	 * 时效时间.
	 * 
	 * <pre>
	 * 0-即刻生效, 即刻起+时长(分钟) 
	 * 1-即刻生效, 固定结束时间(内容: 年月日时分秒) 
	 * 2-使用时生效.即刻起+时效时长(分钟)
	 * </pre>
	 */
	private long expirateTime = 0L;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getCanSales() {
		return canSales;
	}

	public void setCanSales(boolean canSales) {
		this.canSales = canSales;
	}

	public int getJob() {
		return job;
	}
	
	public void setJob(int job) {
		this.job = job;
	}

	public int getSilverPrice() {
		return silverPrice;
	}

	public int getSellSilverPrice() {
		return (int) Math.ceil(silverPrice / 3.0);
	}

	public int getShopSilverPrice(int count) {
		return silverPrice * count;
	}

	public void setSilverPrice(int silverPrice) {
		this.silverPrice = silverPrice;
	}

	public int getMallPrice() {
		return mallPrice;
	}
	
	public int getMallPriceByCount(int count) {
		return this.mallPrice * count;
	}
	
	public int getVipPriceByCount(int count) {
		return this.vipPrice * count;
	}

	public void setMallPrice(int mallPrice) {
		this.mallPrice = mallPrice;
	}

	public int getVipPrice() {
		return vipPrice;
	}

	public void setVipPrice(int vipPrice) {
		this.vipPrice = vipPrice;
	}

	public int getBindingType() {
		return bindingType;
	}

	public void setBindingType(int bindingType) {
		this.bindingType = bindingType;
	}

	public int getPropsType() {
		return propsType;
	}

	public void setPropsType(int propsType) {
		this.propsType = propsType;
	}

	public boolean validatePropsType(int propsType) {
		return this.getPropsType() == propsType;
	}
	
	public int getPackSort() {
		return packSort;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setPackSort(int packSort) {
		this.packSort = packSort;
	}


	public int getExpirateType() {
		return expirateType;
	}

	public void setExpirateType(int expirateType) {
		this.expirateType = expirateType;
	}

	public long getExpirateTime() {
		return expirateTime;
	}

	public void setExpirateTime(long expirateTime) {
		this.expirateTime = expirateTime;
	}

	public boolean isJobType(Job roleJob) {
		return this.job == Job.COMMON.ordinal() || this.job == roleJob.ordinal();
	}
	
	public int getQuality() {
		return quality;
	}

	public Quality getQualityEnum() {
		return EnumUtils.getEnum(Quality.class, quality);
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
	
	public int getCouponPrice() {
		return couponPrice;
	}

	public int getCouponPriceByCount(int count) {
		return couponPrice * count;
	}

	public void setCouponPrice(int couponPrice) {
		this.couponPrice = couponPrice;
	}

	/**
	 * 获得用户道具的有效时间
	 *  
	 *  @param  validateUse		是否验证使用道具
	 * @return	{@link Date}	有时间限制, 则返回{@link Date}, 没有时间限制, 则返回null
	 */
	public Date getExpirateDate(boolean checkUse) {
		if(this.expirateTime <= 0) {
			return null;
		} 

		//当前创建就生效
		if(expirateType == ExpirateType.NOW_EXPIRATE.ordinal()) {
			return new Date(System.currentTimeMillis() + this.expirateTime * TimeConstant.ONE_MINUTE_MILLISECOND);
		}

		//检查使用, 且是使用绑定, 则创建时间返回
		if(checkUse && this.expirateType == ExpirateType.USE_TRIGGER_EXPIRATE.ordinal()) {
			return new Date(System.currentTimeMillis() + this.expirateTime * TimeConstant.ONE_MINUTE_MILLISECOND);
		}
		
		//如果是立即生效, 则马上生效
		if(expirateType == ExpirateType.FIXED_EXPIRATE.ordinal()) {
			try {
				return DateUtil.string2Date(String.valueOf(this.expirateTime), DatePattern.PATTERN_YYYYMMDDHHMMSS);
			} catch (Exception e) {
			}
		} 
		return null;
	}
}
