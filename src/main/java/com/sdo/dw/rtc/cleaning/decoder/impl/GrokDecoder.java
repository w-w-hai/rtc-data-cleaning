package com.sdo.dw.rtc.cleaning.decoder.impl;

import static com.sdo.dw.rtc.cleaning.Constants.GROK_ENTRY;
import static com.sdo.dw.rtc.cleaning.Constants.GROK_PATTERNS;
import static com.sdo.dw.rtc.cleaning.Constants.GROK_PATTERNS_FILE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.Context;
import com.sdo.dw.rtc.cleaning.decoder.Decoder;
import com.sdo.dw.rtc.cleaning.decoder.DecoderType;
import com.sdo.dw.rtc.cleaning.util.CommonUtils;

import io.thekraken.grok.api.Grok;
import io.thekraken.grok.api.Match;

/**
 * @author xiejing.kane
 *
 */
@DecoderType("grok")
public class GrokDecoder implements Decoder {
	private static final Logger LOGGER = LoggerFactory.getLogger(GrokDecoder.class);

	private Context decoderContext;
	private Grok grok;

	@Override
	public void init(Context decoderContext) throws Exception {
		this.decoderContext = decoderContext;
		initPatterns();
	}

	private void initPatterns() throws Exception {
		String name = decoderContext.getString(GROK_ENTRY);
		JSONObject patterns = decoderContext.getJSONObject(GROK_PATTERNS);
		String file = decoderContext.getString(GROK_PATTERNS_FILE);
		grok = CommonUtils.initGrok(name, patterns, file);
	}

	@Override
	public JSONObject decode(String source) {
		Match gm = grok.match(source);
		gm.captures();
		JSONObject json = JSON.parseObject(gm.toJson());
		LOGGER.trace("decode result = " + json);
		return json;
	}
}
