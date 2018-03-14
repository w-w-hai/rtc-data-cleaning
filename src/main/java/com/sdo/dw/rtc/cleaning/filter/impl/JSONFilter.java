package com.sdo.dw.rtc.cleaning.filter.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;
import com.sdo.dw.rtc.cleaning.util.CommonUtils;
import com.sdo.dw.rtc.cleaning.util.ExtractCallable;

/**
 * @author xiejing.kane
 *
 */
@FilterType("json")
public class JSONFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(JSONFilter.class);
	private static final boolean DEFAULT_DISCARD_EXISTING = false;
	private static final boolean DEFAULT_PRESERVE_EXISTING = true;
	private static final boolean DEFAULT_APPEND_PREFIX = false;

	private String field;
	private boolean discardExisting;
	private boolean preserveExisting;

	/**
	 * 注意：设置appendPrefix=true的话，后续的filter使用字段时要带上前缀，否则将会找不到field
	 */
	private boolean appendPrefix;

	@Override
	public void init(JSONObject config) {
		field = config.getString("field");
		discardExisting = (boolean) config.getOrDefault("discard_existing", DEFAULT_DISCARD_EXISTING);
		preserveExisting = (boolean) config.getOrDefault("preserve_existing", DEFAULT_PRESERVE_EXISTING);
		appendPrefix = (boolean) config.getOrDefault("append_prefix", DEFAULT_APPEND_PREFIX);
		LOGGER.info(
				MessageFormat.format("field = {0}, discardExisting = {1}, preserveExisting = {2}, appendPrefix = {3}",
						field, discardExisting, preserveExisting, appendPrefix));
	}

	@Override
	public JSONObject filter(JSONObject source) throws Exception {
		return CommonUtils.extract(source, field, discardExisting, preserveExisting, appendPrefix,
				new ExtractCallable() {
					@Override
					public JSONObject call() throws Exception {
						return JSON.parseObject(getSource());
					}
				});
	}
}
