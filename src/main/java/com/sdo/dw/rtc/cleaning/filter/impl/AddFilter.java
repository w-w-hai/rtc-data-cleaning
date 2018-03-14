package com.sdo.dw.rtc.cleaning.filter.impl;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

/**
 * @author xiejing.kane
 *
 */
@FilterType("add")
public class AddFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(AddFilter.class);
	private JSONObject fields;
	private boolean preserveExisting = true;

	@Override
	public void init(JSONObject config) {
		fields = config.getJSONObject("fields");
		preserveExisting = config.getBooleanValue("preserve_existing");
		LOGGER.info("add fields = " + fields + ", preserveExisting = " + preserveExisting);
	}

	@Override
	public JSONObject filter(JSONObject source) {
		for (Entry<String, Object> entry : fields.entrySet()) {
			if (source.containsKey(entry.getKey()) && preserveExisting) {
				continue;
			}
			source.put(entry.getKey(), entry.getValue());
		}
		return source;
	}
}
