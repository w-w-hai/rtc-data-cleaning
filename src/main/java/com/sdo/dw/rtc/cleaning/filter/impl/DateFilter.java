package com.sdo.dw.rtc.cleaning.filter.impl;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

/**
 * @author xiejing.kane
 *
 */
@FilterType("date")
public class DateFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(RenameFilter.class);
	private String field;

	private ThreadLocal<SimpleDateFormat> sourceSdf;
	private ThreadLocal<SimpleDateFormat> targetSdf;

	@Override
	public void init(JSONObject config) {
		field = config.getString("field");
		String sourceFormat = config.getString("source");
		String targetFormat = config.getString("target");
		sourceSdf = new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat(sourceFormat);
			}
		};
		targetSdf = new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat(targetFormat);
			}
		};
		LOGGER.info(MessageFormat.format("field = {0}, sourceFormat = {1}, targetFormat = {2}", field, sourceFormat,
				targetFormat));
	}

	@Override
	public JSONObject filter(JSONObject source) throws ParseException {
		if (source.containsKey(field)) {
			Date sourceDate = sourceSdf.get().parse(source.getString(field));
			String targetDate = targetSdf.get().format(sourceDate);
			source.put(field, targetDate);
		}
		return source;
	}
}
