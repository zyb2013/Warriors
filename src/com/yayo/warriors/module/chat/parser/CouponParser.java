package com.yayo.warriors.module.chat.parser;

import org.springframework.stereotype.Component;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.model.UserDomain;


@Component
public class CouponParser extends AbstractGMCommandParser {
	
	protected String getCommand() {
		return GmType.COUPON;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		Long addCoupon = Long.valueOf(elements[2].trim());
		String playerName = userDomain.getPlayer().getName();
		return true;
	}

}
