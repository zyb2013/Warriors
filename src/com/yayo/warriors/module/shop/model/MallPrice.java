package com.yayo.warriors.module.shop.model;

import com.yayo.warriors.type.Currency;

public class MallPrice {

	
	private long mallPrice;
	private Currency currency;
	private boolean binding = false;
	
	public long getMallPrice() {
		return mallPrice;
	}

	public void setMallPrice(long mallPrice) {
		this.mallPrice = mallPrice;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	@Override
	public String toString() {
		return "MallPrice [mallPrice=" + mallPrice + ", currency=" + currency + ", binding="
				+ binding + "]";
	}
	
	public static MallPrice goldenPrice(long goldenPrice, boolean binding) {
		MallPrice mallPrice = new MallPrice();
		mallPrice.binding = binding;
		mallPrice.mallPrice = goldenPrice;
		mallPrice.currency = Currency.GOLDEN;
		return mallPrice;
	}
	
	public static MallPrice couponPrice(long couponPrice, boolean binding) {
		MallPrice mallPrice = new MallPrice();
		mallPrice.binding = binding;
		mallPrice.mallPrice = couponPrice;
		mallPrice.currency = Currency.COUPON;
		return mallPrice;
	}
}
