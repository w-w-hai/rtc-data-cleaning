package com.sdo.dw.rtc.cleaning.util;

import java.util.concurrent.Callable;

import com.alibaba.fastjson.JSONObject;

/**
 * @author xiejing.kane
 *
 */
public abstract class ExtractCallable implements Callable<JSONObject> {
	private String source;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
