package com.sdo.dw.rtc.cleaning.filter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

/**
 * @author xiejing.kane
 *
 */
@FilterType("iptolong")
public class IPToLongFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(IPToLongFilter.class);
	private String field;
	private String newField;

	@Override
	public void init(JSONObject config) {
		field = config.getString("field");
		newField = config.getString("new_field");
		LOGGER.info("IPToLongFilter field = " + field + ", newField = " + newField);
	}

	@Override
	public JSONObject filter(JSONObject source) {
		if (source.containsKey(field)) {
			String ip = source.getString(field);
			if (!ip.isEmpty()) {
				source.put(newField, ipToLong(ip));
			}
		}
		return source;
	}

	private long ipToLong(String ip) {
		String[] addrArray = ip.split("\\.");
		long num = 0;
		for (int i = 0; i < addrArray.length; i++) {
			int power = 3 - i;
			num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
		}
		return num;
	}
}
