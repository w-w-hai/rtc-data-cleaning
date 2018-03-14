package com.sdo.dw.rtc.cleaning.filter.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

/**
 * @author xiejing.kane
 *
 */
@FilterType("replaceall")
public class ReplaceAllFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(RenameFilter.class);
	private String field;

	private String regex;
	private String repl;

	@Override
	public void init(JSONObject config) {
		field = config.getString("field");
		regex = config.getString("regex");
		repl = config.getString("repl");
		LOGGER.info(MessageFormat.format("field = {0}, regex = {1}, repl = {2}", field, regex, repl));
	}

	@Override
	public JSONObject filter(JSONObject source) {
		if (source.containsKey(field)) {
			String value = source.getString(field).replaceAll(regex, repl);
			source.put(field, value);
		}
		return source;
	}
}
