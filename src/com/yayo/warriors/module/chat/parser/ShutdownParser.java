package com.yayo.warriors.module.chat.parser;
import org.springframework.stereotype.Component;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.model.UserDomain;


@Component
public class ShutdownParser extends AbstractGMCommandParser {
	
	
	protected String getCommand() {
		return GmType.SHUT_DOWN;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		 return true;
	}
}
