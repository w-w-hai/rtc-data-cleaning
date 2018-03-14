package com.sdo.dw.rtc.cleaning.decoder.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.Context;
import com.sdo.dw.rtc.cleaning.decoder.Decoder;
import com.sdo.dw.rtc.cleaning.decoder.DecoderType;
import com.sdo.dw.rtc.cleaning.exception.InvalidParameterException;

/**
 * @author xiejing.kane
 *
 */
@DecoderType("json")
public class JSONDecoder implements Decoder {
	private static final Logger LOGGER = LoggerFactory.getLogger(JSONDecoder.class);

	@Override
	public void init(Context decoderContext) throws InvalidParameterException {
	}

	@Override
	public JSONObject decode(String source) {
		JSONObject json = JSONObject.parseObject(source);
		LOGGER.trace("decode result = " + json);
		return json;
	}

}
