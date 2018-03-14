package com.sdo.dw.rtc.cleaning.filter.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

/**
 * @author xiejing.kane
 *
 */
@FilterType("trim")
public class TrimFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(TrimFilter.class);
	private List<String> fields;

	@Override
	public void init(JSONObject config) {
		fields = Lists.newArrayList(config.getJSONArray("fields").toArray(new String[] {}));
		LOGGER.info("trim fields = " + fields);
	}

	@Override
	public JSONObject filter(JSONObject source) {
		for (String field : fields) {
			if (source.containsKey(field)) {
				source.put(field, source.getString(field).trim());
			}
		}
		return source;
	}
}
