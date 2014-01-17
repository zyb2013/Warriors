package com.yayo.warriors.util;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flex.messaging.io.amf.ASObject;

/**
 * 参数工具类
 *  
 * @author Hyint
 */
public class ParamUtils {

	private static Logger log = LoggerFactory.getLogger(ParamUtils.class);
	
	/**
	 * 构建指定类型的对象
	 * 
	 * @param  object		需要构建的对象
	 * @param  clazz		需要构建的类型
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	private static <T> T valueOf(Object object, Class<T> clazz) {
		try{
			if(clazz == Long.class || clazz == long.class) {
				if(object.getClass() == String.class) {
					return valueOf(object.toString(), clazz);
				} else if(object instanceof Number) {
					return (T) Long.valueOf(((Number)object).longValue());
				}
			} else if(clazz == Object[].class) {
				return (T) object;
			} else if(clazz == String.class) {
				return (T) object;
			}
			
			Method valueOfMethod = clazz.getMethod("valueOf", String.class);
			if (valueOfMethod != null) {
				 return (T) valueOfMethod.invoke(clazz, object.toString());
			}
		}catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	/**
	 * 获得参数类型
	 * 
	 * @param  aso		实体对象
	 * @param  key		需要取值的Key
	 * @param  clazz	需要转的类型
	 * @return T
	 */
	public static <T> T getParameter(ASObject aso, String key, Class<T> clazz) {
		return valueOf(aso.get(key), clazz);
	}
}
