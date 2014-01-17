package com.yayo.warriors.common.helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.rhino.RhinoHelper;
import com.yayo.common.utility.NumberUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.basedb.model.FormulaConfig;
import com.yayo.warriors.bo.ObjectReference;

/**
 * 公式帮助类
 * 
 * @author Hyint
 */
@Component
public class FormulaHelper {
	private static final  Logger LOGGER = LoggerFactory.getLogger(FormulaHelper.class);
	private static final ObjectReference<FormulaHelper> ref = new ObjectReference<FormulaHelper>();
	@Autowired
	private ResourceService resourceService;
	
	/** 公式表达式计算缓存结果 { expression_params, 运算结果} */
	private static final ConcurrentLinkedHashMap<String, Number> FORMULA_EXPRESSION_CACHES = new ConcurrentLinkedHashMap.Builder<String, Number>().maximumWeightedCapacity(50000).build();
	
	@PostConstruct
	void init() {
		ref.set(this);
	}

	private static FormulaHelper getInstance() {
		return ref.get();
	}
	/**
	 * 公式计算. 该方法不一定每次都需要执行公式计算, 当公式ID一致, 
	 * 且 公式对象的参数都是不变的情况下, 则只会计算一次.
	 * 
	 * @param  formulaId		公式ID
	 * @param  params			可变参参数
	 * @return {@link Number}	公式计算返回值
	 */
	public static Number invoke(int formulaId, Number...params) {
		FormulaConfig formula = getInstance().resourceService.get(formulaId, FormulaConfig.class);
		return formula == null ? 0 : invoke(formula.getExpression(), params);// 缓存KEY. 并且从缓存中获取
	}
	
	/**
	 * 执行公式表达式
	 * 
	 * @param  expression 		公式表达式
	 * @param  params 			公式参数
	 * @param  resultType 		执行结果类型
	 * @return T 				公式表达式执行结果
	 */
	public static <T> T invoke(int id, Class<T> resultType, Number... params) {
		Number value = invoke(id, params);
		return (T) NumberUtil.valueOf(resultType, value);
	}
	
	/**
	 * 战斗公式计算.  
	 * 
	 * @param  formulaId		公式ID
	 * @param  params			可变参参数
	 * @return {@link Number}	公式计算返回值
	 */
	public static Number invoke(String expression, Number...params) {
		if(StringUtils.isBlank(expression)) {
			return 0;
		}
		
		// 缓存KEY. 并且从缓存中获取
		String paramString = Arrays.toString(params);
		final String CACHE_KEY = toCacheKey(expression, paramString);
		Number result = FORMULA_EXPRESSION_CACHES.get(CACHE_KEY);
		if(result != null) {
			return result;
		}
		
		int len = params.length;
		Map<String, Object> ctx = new HashMap<String, Object>(len);
		for(int i = 0; i < len; i++) {
			ctx.put("n" + ( i + 1), params[i]);
		}
		
		Object resultValue = RhinoHelper.invoke(expression, ctx);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("公式表达式: [{}], 参数: [{}], 计算结果: [{}]", new Object[]{ expression, paramString, resultValue });
		}
		
		// 放置到缓存
		result = resultValue == null ? 0 : (Number) resultValue;
		FORMULA_EXPRESSION_CACHES.remove(CACHE_KEY);
		if(resultValue != null) {
			FORMULA_EXPRESSION_CACHES.put(CACHE_KEY, result);
		}
		
		return result;
	}
	
	/**
	 * 执行公式表达式
	 * 
	 * @param  expression 		公式表达式
	 * @param  resultType 		执行结果类型
	 * @param  params 			公式参数
	 * @return T 				公式表达式执行结果
	 */
	public static <T> T invoke(String expression, Class<T> resultType, Number... params) {
		Number value = invoke(expression, params);
		return (T) NumberUtil.valueOf(resultType, value);
	}
	
	/**
	 * 构建缓存Key
	 * 
	 * @param 	expression		公式ID
	 * @param 	paramString		公式参数字符串
	 * @return {@link String}	返回缓存字符串
	 */
	private static String toCacheKey(String expression, String paramString) {
		return new StringBuffer().append(expression == null ? "" : expression).append(Splitable.ATTRIBUTE_SPLIT).append(paramString).toString();
	}
}
