package com.sdo.dw.rtc.cleaning.filter.impl;

import static com.sdo.dw.rtc.cleaning.Constants.CODE;
import static com.sdo.dw.rtc.cleaning.Constants.CODE_FILE;
import static com.sdo.dw.rtc.cleaning.Constants.IMPORT;
import static com.sdo.dw.rtc.cleaning.Constants.TEMPLATE_CLASS_NAME;
import static com.sdo.dw.rtc.cleaning.Constants.TEMPLATE_FILE;
import static com.sdo.dw.rtc.cleaning.Constants.TEMPLATE_METHOD_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sdo.dw.rtc.cleaning.exception.InvalidParameterException;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

/**
 * @author xiejing.kane
 *
 */
@FilterType("java")
public class JavaDynamicFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(JavaDynamicFilter.class);

	private Object target;

	private Method targetMethod;

	private JSONObject config;

	@Override
	public void init(JSONObject config) throws Exception {
		this.config = config;
		String code = getCode(config);
		JSONArray imports = config.getJSONArray(IMPORT);
		String sourceCode = renderCode(code, imports);
		initTemplate(sourceCode);
		LOGGER.info(MessageFormat.format("original code = {0}, imports = {1}, rendered code = {2}", code, imports,
				sourceCode));
	}

	@Override
	public JSONObject filter(JSONObject source) throws Exception {
		return (JSONObject) targetMethod.invoke(target, source, config);
	}

	/**
	 * 
	 * 从配置中获取嵌入模板的代码，code直接传入代码，code_file传入代码文件
	 * 
	 * @return
	 * @throws InvalidParameterException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private String getCode(JSONObject config) throws InvalidParameterException, IOException {
		if (config.containsKey(CODE)) {
			return config.getString(CODE);
		} else if (config.containsKey(CODE_FILE)) {
			return readInputStream(new FileInputStream(new File(config.getString(CODE_FILE))));
		} else {
			throw new InvalidParameterException("Either 'code' or 'code_file' should be provided.");
		}
	}

	/**
	 * 编译代码，并实例化模板类_Template_对象
	 * 
	 * @throws Exception
	 */
	private void initTemplate(String sourceCode) throws Exception {
		File classesDir = getClassesDir();

		JavaFileObject file = new JavaFileObjectExt(TEMPLATE_CLASS_NAME, sourceCode);
		Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(file);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(classesDir.getAbsolutePath());
		options.add("-classpath");
		options.add(getClassPath(classesDir));

		JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, options, null, fileObjects);
		if (task.call()) {
			try {
				instantiateTarget(classesDir);
			} catch (Exception e) {
				throw new Exception("find class error", e);
			} finally {
				deleteFile(classesDir);
			}
		} else {
			// 创建_Template_对象失败
			deleteFile(classesDir);
			StringBuilder sb = new StringBuilder();
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				sb.append(diagnostic.getMessage(Locale.CHINA));
			}
			throw new Exception(sb.toString());
		}
	}

	/**
	 * 实例化模板类_Template_对象和_execute_函数
	 * 
	 * @param classesDir
	 * @throws Exception
	 */
	private void instantiateTarget(File classesDir) throws Exception {
		URLClassLoader newLoader = null;
		try {
			URLClassLoader oldLoader = (URLClassLoader) JavaDynamicFilter.class.getClassLoader();
			List<URL> urlList = Lists.newArrayList(oldLoader.getURLs());
			urlList.add(classesDir.toURI().toURL());
			newLoader = new URLClassLoader(urlList.toArray(new URL[] {}), oldLoader);

			String fullClassName = this.getClass().getPackage().getName() + "." + TEMPLATE_CLASS_NAME;
			Class<? extends Object> targetClass = newLoader.loadClass(fullClassName);
			target = targetClass.newInstance();
			targetMethod = targetClass.getDeclaredMethod(TEMPLATE_METHOD_NAME, JSONObject.class, JSONObject.class);
			newLoader.close();
		} finally {
			if (newLoader != null) {
				newLoader.close();
			}
		}
	}

	/**
	 * 创建并返回临时存放classes文件的目录,加载类之后删除
	 * 
	 * @return
	 */
	private File getClassesDir() {
		String userDir = System.getProperty("user.dir");
		File classDir = new File(userDir + File.separator + "plugin" + File.separator + "filter" + File.separator
				+ "classes" + File.separator);
		if (!classDir.exists()) {
			classDir.mkdirs();
		}
		return classDir;
	}

	/**
	 * 从模板中渲染_Template_源码
	 *
	 * @return
	 * @throws Exception
	 */
	private String renderCode(String code, JSONArray imports) throws Exception {
		StringBuilder importString = new StringBuilder();
		if (imports != null) {
			for (Object obj : imports) {
				importString.append("import ").append(obj.toString()).append(";");
			}
		}
		return readTemplate().replace("${CODE}", code).replace("${IMPORT}", importString.toString());
	}

	/**
	 * 读取模板_Template_
	 * 
	 * @return
	 * @throws IOException
	 */
	private String readTemplate() throws IOException {
		InputStream inputStream = JavaDynamicFilter.class.getClassLoader().getResourceAsStream(TEMPLATE_FILE);
		return readInputStream(inputStream);
	}

	private String readInputStream(InputStream inputStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line);
			}
			return stringBuilder.toString();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				LOGGER.error("close template reader error", e);
			}
		}
	}

	/**
	 * 获取当前类的classpath
	 *
	 * @return
	 */
	private String getClassPath(File classPathDir) {
		URLClassLoader currentLoader = (URLClassLoader) getClass().getClassLoader();
		URLClassLoader parentLoader = (URLClassLoader) getClass().getClassLoader().getParent();
		StringBuilder sb = new StringBuilder();
		sb.append(getClassPathURL(parentLoader));
		sb.append(getClassPathURL(currentLoader));
		sb.append(classPathDir.getAbsolutePath());
		return sb.toString();
	}

	/**
	 * 获取当前class loader所加载的jar包路径
	 *
	 * @param classLoader
	 * @return
	 */
	private String getClassPathURL(URLClassLoader classLoader) {
		StringBuilder sb = new StringBuilder();
		for (URL url : classLoader.getURLs()) {
			sb.append(url.getFile()).append(File.pathSeparator);
		}
		return sb.toString();
	}

	/**
	 * 删除文件或目录
	 * 
	 * @param file
	 */
	private void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (int i = 0, len = files.length; i < len; i++) {
					deleteFile(files[i]);
				}
				file.delete();
			}
		}
	}

	private class JavaFileObjectExt extends SimpleJavaFileObject {
		final String code;

		JavaFileObjectExt(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}
}
