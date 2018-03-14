package com.sdo.dw.rtc.cleaning;

/**
 * @author xiejing.kane
 *
 */
public class Constants {
	public static final String DECODER = "decoder";
	public static final String FILTERS = "filters";
	public static final String HANDLER = "handler";
	public static final String TYPE = "type";

	/* grok */
	public static final String GROK_PATTERNS = "grok_patterns";
	public static final String GROK_PATTERNS_FILE = "grok_patterns_file";
	public static final String GROK_ENTRY = "grok_entry";

	/* filter */
	public static final String FILTER_PARAMS = "params";
	public static final String DISCARD_RECORD_ON_ERROR = "discard_record_on_error";
	public static final boolean DEFAULT_DISCARD_RECORD_ON_ERROR = true;

	/* dynamic filter */
	public static final String CODE = "code";
	public static final String CODE_FILE = "code_file";
	public static final String IMPORT = "import";
	public final static String TEMPLATE_CLASS_NAME = "_Template_";
	public final static String TEMPLATE_METHOD_NAME = "_execute_";
	public final static String TEMPLATE_FILE = "dynamic_filter.template";

	/* response */
	public static final String RES_FAILURE = "#FAILURE#";
	public static final String RES_THROWABLE = "throwable";
	public static final String RES_SOURCE = "source";
}
