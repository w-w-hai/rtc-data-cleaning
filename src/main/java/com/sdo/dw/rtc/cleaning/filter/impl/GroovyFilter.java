package com.sdo.dw.rtc.cleaning.filter.impl;

import java.text.MessageFormat;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

import groovy.lang.GroovyShell;

/**
 * @author xiejing.kane
 *
 */
@FilterType("groovy")
public class GroovyFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(GroovyFilter.class);
	private String field;
	private String expr;
	private GroovyShell shell;

	@Override
	public void init(JSONObject config) {
		field = config.getString("field");
		expr = config.getString("expr");
		shell = new GroovyShell();
		LOGGER.info(MessageFormat.format("field = {0}, expr = {1}", field, expr));
	}

	@Override
	public JSONObject filter(JSONObject source) throws ScriptException {
		source.put(field, shell.parse(expr).run());
		return source;
	}
}
