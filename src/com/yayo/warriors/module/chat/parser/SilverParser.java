package com.yayo.warriors.module.chat.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.model.UserDomain;


@Component
public class SilverParser extends AbstractGMCommandParser {
	
	
	protected String getCommand() {
		return GmType.SILVER;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		Long addSilver = Long.valueOf(elements[2].trim());
		String playerName = userDomain.getPlayer().getName();
		return true;
	}

}
