package com.sdo.dw.rtc.cleaning.filter;

import com.alibaba.fastjson.JSONObject;

/**
 * @author xiejing.kane
 *
 */
public interface Filter {

	void init(JSONObject config) throws Exception;

	JSONObject filter(JSONObject source) throws Exception;
}
