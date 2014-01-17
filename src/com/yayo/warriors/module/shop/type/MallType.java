package com.yayo.warriors.module.shop.type;

import com.yayo.warriors.basedb.model.GoodsParent;
import com.yayo.warriors.module.shop.model.MallPrice;
import com.yayo.warriors.module.vip.model.VipDomain;


public enum MallType {
	
	GEM_PROPS_TYPE(new PriceChecker() {
		
		public MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count) {
			if (vipDomain != null && vipDomain.isVip()) {
				return MallPrice.goldenPrice(goodsParent.getVipPriceByCount(count), false);
			}
			return MallPrice.goldenPrice(goodsParent.getMallPriceByCount(count), false);
		}
	}),
	
	MEDICINE_TYPE(new PriceChecker() {
		
		public MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count) {
			if (vipDomain != null && vipDomain.isVip()) {
				return MallPrice.goldenPrice(goodsParent.getVipPriceByCount(count), false);
			}
			return MallPrice.goldenPrice(goodsParent.getMallPriceByCount(count), false);
		}
	}),
	
	EQUIP_TYPE(new PriceChecker() {
		
		public MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count) {
			if (vipDomain != null && vipDomain.isVip()) {	
				return MallPrice.goldenPrice(goodsParent.getVipPriceByCount(count), false);
			}
			return MallPrice.goldenPrice(goodsParent.getMallPriceByCount(count), false);
		}
	}),
	
	PET_EGG_TYPE(new PriceChecker() {
		
		public MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count) {
			if (vipDomain != null && vipDomain.isVip()) {	
				return MallPrice.goldenPrice(goodsParent.getVipPriceByCount(count), false);
			}
			return MallPrice.goldenPrice(goodsParent.getMallPriceByCount(count), false);
		}
	}),
	
	MORTAL_TYPE(new PriceChecker() {
		
		public MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count) {
			if (vipDomain != null && vipDomain.isVip()) {	
				return MallPrice.goldenPrice(goodsParent.getVipPriceByCount(count), false);
			}
			return MallPrice.goldenPrice(goodsParent.getMallPriceByCount(count), false);
		}
	}),
	
	ACTIVITY_TYPE(new PriceChecker() {
		
		public MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count) {
			if (vipDomain != null && vipDomain.isVip()) {	
				return MallPrice.goldenPrice(goodsParent.getVipPriceByCount(count), false);
			}
			return MallPrice.goldenPrice(goodsParent.getMallPriceByCount(count), false);
		}
	}),
	
	COUPON_TYPE(new PriceChecker() {
		
		public MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count) {
			return MallPrice.couponPrice(goodsParent.getCouponPriceByCount(count), true);
		}
	});
	
	private PriceChecker priceChecker;
	
	MallType(PriceChecker priceChecker) {
		this.priceChecker = priceChecker;
	}
	
	public PriceChecker getPriceChecker() {
		return priceChecker;
	}
	public static interface PriceChecker {
		MallPrice calcPrice(VipDomain vipDomain, GoodsParent goodsParent, int count);
	}
}
