package com.yayo.warriors.module.logger.helper;

import static com.yayo.common.utility.Splitable.*;

import com.yayo.warriors.module.logger.model.Term;

public class TermHelper {

	/**
	 * 将若干个{@link Term}信息变成: 名字1:信息1|名字2:信息2|... 的字符串
	 * 
	 * @param  terms			{@link Term}可变参
	 * @return {@link String}	返回构造后的字符串
	 */
	public static String termToString(Term... terms) {
		StringBuilder builder = new StringBuilder();
		for (Term obj : terms) {
			builder.append(obj).append(BETWEEN_ITEMS);
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
}
