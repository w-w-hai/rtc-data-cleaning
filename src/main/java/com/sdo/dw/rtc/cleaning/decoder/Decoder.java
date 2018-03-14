package com.sdo.dw.rtc.cleaning.decoder;

import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.Context;

/**
 * @author xiejing.kane
 *
 */
public interface Decoder {
	void init(Context context) throws Exception;

	JSONObject decode(String source) throws Exception;
}
