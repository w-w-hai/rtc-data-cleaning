package com.sdo.dw.rtc.cleaning;

import java.text.MessageFormat;

import com.alibaba.fastjson.JSONObject;

/**
 * @author xiejing.kane
 *
 */
public class Result {
	private JSONObject payload;
	private boolean successful;
	private String source;
	private JSONObject upstream;
	private Throwable throwable;

	/**
	 * Result for success
	 * 
	 * @param payload
	 * @param source
	 */
	public Result(JSONObject payload, String source) {
		this.successful = true;
		this.payload = payload;
		this.source = source;
	}

	/**
	 * Result for filter failure
	 * 
	 * @param source
	 * @param upstream
	 * @param throwable
	 */
	public Result(String source, JSONObject upstream, Throwable throwable) {
		this.successful = false;
		this.source = source;
		this.upstream = upstream;
		this.throwable = throwable;
	}

	/**
	 * Result for decode failure
	 * 
	 * @param source
	 * @param throwable
	 */
	public Result(String source, Throwable throwable) {
		this.successful = false;
		this.source = source;
		this.throwable = throwable;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public JSONObject getPayload() {
		return payload;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public String getSource() {
		return source;
	}

	public JSONObject getUpstream() {
		return upstream;
	}

	@Override
	public String toString() {
		if (successful) {
			return MessageFormat.format("successful = {0}, payload = {1}", successful, payload.toJSONString());
		} else {
			return MessageFormat.format("successful = {0}, throwable = {1}, source = {2}, upstream = {3}", successful,
					throwable.getMessage(), source, upstream);
		}
	}
}
