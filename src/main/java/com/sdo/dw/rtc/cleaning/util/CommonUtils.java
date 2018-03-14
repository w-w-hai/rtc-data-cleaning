package com.sdo.dw.rtc.cleaning.util;

import static com.sdo.dw.rtc.cleaning.Constants.GROK_PATTERNS;
import static com.sdo.dw.rtc.cleaning.Constants.GROK_PATTERNS_FILE;
import static com.sdo.dw.rtc.cleaning.Constants.RES_FAILURE;
import static com.sdo.dw.rtc.cleaning.Constants.RES_SOURCE;
import static com.sdo.dw.rtc.cleaning.Constants.RES_THROWABLE;

import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.sdo.dw.rtc.cleaning.decoder.DecoderType;
import com.sdo.dw.rtc.cleaning.exception.InvalidParameterException;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

import io.thekraken.grok.api.Grok;

/**
 * @author xiejing.kane
 *
 */
public class CommonUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

	public static JSONObject generateFailure(boolean recordFailure, Throwable throwable, Object source) {
		JSONObject result = new JSONObject();
		if (recordFailure) {
			JSONObject failure = new JSONObject();
			failure.put(RES_THROWABLE, throwable);
			failure.put(RES_SOURCE, source);
			result.put(RES_FAILURE, failure);
		}
		return result;
	}

	/**
	 * 驼峰转下划线
	 *
	 * @param camel
	 * @return
	 */
	public static String camel2Underline(String camel) {
		Pattern pattern = Pattern.compile("[A-Z]");
		Matcher matcher = pattern.matcher(camel);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String w = matcher.group().trim();
			matcher.appendReplacement(sb, "_" + w.toLowerCase());
		}
		matcher.appendTail(sb);
		if (sb.charAt(0) == '_') {
			sb.delete(0, 1);
		}
		return sb.toString();
	}

	public static Map<String, String> getAnnotatedFilters() throws Exception {
		Map<String, String> annotatedFilters = Maps.newHashMap();
		for (Entry<Annotation, String> entry : getAnnotatedClasses(FilterType.class).entrySet()) {
			annotatedFilters.put(((FilterType) entry.getKey()).value(), entry.getValue());
		}
		return annotatedFilters;
	}

	public static Map<String, String> getAnnotatedDecoders() throws Exception {
		Map<String, String> annotatedDecoders = Maps.newHashMap();
		for (Entry<Annotation, String> entry : getAnnotatedClasses(DecoderType.class).entrySet()) {
			annotatedDecoders.put(((DecoderType) entry.getKey()).value(), entry.getValue());
		}
		return annotatedDecoders;
	}

	private static Map<Annotation, String> getAnnotatedClasses(Class<? extends Annotation> annotationClass)
			throws Exception {
		Map<Annotation, String> annotatedClasses = Maps.newHashMap();
		Reflections reflections = new Reflections(annotationClass.getPackage().getName() + ".impl");
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(annotationClass);
		for (Class<?> clazz : annotated) {
			Annotation annotation = clazz.getAnnotation(annotationClass);
			if (annotatedClasses.containsKey(annotation)) {
				throw new Exception(
						MessageFormat.format("Duplicated declaration of Annotation: {0}, classes = [{1}, {2}]",
								annotation, annotatedClasses.get(annotation), clazz.getName()));
			}
			annotatedClasses.put(annotation, clazz.getName());
		}
		return annotatedClasses;
	}

	public static Grok initGrok(String name, JSONObject patterns, String patternFile) throws Exception {
		Grok grok = new Grok();
		// load default patterns, which will be overriden by custom patterns
		grok.addPatternFromReader(
				new InputStreamReader(CommonUtils.class.getClassLoader().getResourceAsStream("default_patterns")));
		if (patternFile != null) {
			grok.addPatternFromFile(patternFile);
		}
		// grok_patterns优先级高于grok_patterns_file
		if (patterns != null) {
			for (Entry<String, Object> entry : patterns.entrySet()) {
				grok.addPattern(entry.getKey(), entry.getValue().toString());
			}
		}

		if (grok.getPatterns().isEmpty()) {
			throw new InvalidParameterException(
					MessageFormat.format("Either {0} or {1} is required!", GROK_PATTERNS_FILE, GROK_PATTERNS));
		}
		LOGGER.info("init Grok " + name + ", patterns = " + grok.getPatterns());
		grok.compile("%{" + name + "}", true);
		return grok;
	}

	public static JSONObject extract(JSONObject source, String field, boolean discardExisting, boolean preserveExisting,
			boolean appendPrefix, ExtractCallable callable) throws Exception {
		JSONObject result = discardExisting ? new JSONObject() : source;
		if (!source.containsKey(field)) {
			return result;
		}
		callable.setSource(source.getString(field));
		JSONObject value = callable.call();
		for (Entry<String, Object> entry : value.entrySet()) {
			if (preserveExisting && result.containsKey(entry.getKey())) {
				continue;
			} else {
				if (appendPrefix) {
					result.put(field + "." + entry.getKey(), entry.getValue());
				} else {
					result.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return result;
	}
}
