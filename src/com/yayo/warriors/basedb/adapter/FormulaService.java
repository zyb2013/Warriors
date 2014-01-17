package com.yayo.warriors.basedb.adapter;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.FormulaConfig;
import com.yayo.warriors.common.helper.FormulaHelper;

/**
 * 公式接口
 * 
 * @author Hyint
 */
@Component
public class FormulaService extends ResourceAdapter {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void initialize() {
		final Number[] params = new Number[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		Collection<FormulaConfig> formulaConfigs = resourceService.listAll(FormulaConfig.class);
		if(formulaConfigs != null && !formulaConfigs.isEmpty()) {
			for (FormulaConfig formulaConfig : formulaConfigs) {
				int formulaId = formulaConfig.getId();
				String expression = formulaConfig.getExpression();
				Number result = FormulaHelper.invoke(formulaId, params);
				if(logger.isDebugEnabled()) {
					logger.debug("公式编号: [{}], 表达式: [{}], 参数: [{}], 计算结果: [{}]", new Object[]{ formulaId, expression, params, result });
				}
			}
		}
	}
	
}
