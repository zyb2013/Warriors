package com.yayo.warriors.module.logger.model;

import static com.yayo.common.utility.Splitable.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yayo.warriors.module.logger.LogKey;
import com.yayo.warriors.module.logger.helper.TermHelper;
import com.yayo.warriors.module.logger.type.LogType;


/**
 * 日志记录
 *
 * @author Hyint
 */
public abstract class TraceLog implements LogKey {

	/**
	 * 记录日志
	 * 
	 * @param logType 			日志的类型
	 * @param message 			需要记录的日志信息
	 */
	public static void doLogger(LogType logType, String message) {
		Logger log = LoggerFactory.getLogger(logType.getLogName());
		if (log.isInfoEnabled() && message != null) {
			log.info(message);
		}
	}
	
	/**
	 * 记录日志
	 * 
	 * @param logType 			日志的类型
	 * @param terms 			日志条目 
	 */
	protected static void log(LogType logType, Term... terms) {
		try {
			String message = null;
			if(terms.length > 0) {
				String termString = TermHelper.termToString(terms);
				message = new StringBuilder().append(Term.getTimeTerm()).append(BETWEEN_ITEMS).append(termString).toString();
			}
			doLogger(logType, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 记录日志
	 * 
	 * @param logType 		日志类型
	 * @param terms 		日志条目 
	 */
	protected static void log(LogType logType, LoggerGoods[] infos, Term... terms) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(Term.getTimeTerm()).append(BETWEEN_ITEMS).append(TermHelper.termToString(terms));
			if(infos != null && infos.length > 0) {
				builder.append(BETWEEN_ITEMS);
				builder.append(INFO).append(DELIMITER_ARGS);
				for(LoggerGoods goodsInfo : infos) {
					builder.append(goodsInfo);
				}
				builder.deleteCharAt(builder.length() - 1);
			}
			doLogger(logType, builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
