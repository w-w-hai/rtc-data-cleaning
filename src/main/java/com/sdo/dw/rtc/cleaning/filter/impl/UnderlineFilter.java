package com.sdo.dw.rtc.cleaning.filter.impl;

import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;
import com.sdo.dw.rtc.cleaning.util.CommonUtils;

/**
 * 将key由驼峰表达式替换成下划线表达式
 * <p>
 * gameId -> game_id
 * 
 * @author xiejing.kane
 *
 */
@FilterType("underline")
public class UnderlineFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(UnderlineFilter.class);
	private List<String> fields;

	@Override
	public void init(JSONObject config) {
		fields = Lists.newArrayList(config.getJSONArray("fields").toArray(new String[] {}));
		LOGGER.info("underline fields = " + fields);
	}

	@Override
	public JSONObject filter(JSONObject source) {
		for (Entry<String, Object> entry : Sets.newHashSet(source.entrySet())) {
			String key = entry.getKey();
			if (fields.contains(key) || fields.contains("*")) {
				source.put(CommonUtils.camel2Underline(key), source.remove(key));
			}
		}
		return source;
	}
}
