package com.github.quickquarantine;

import static com.github.quickquarantine.utils.SecurityUtils.VERTICAL_LINE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.quickquarantine.exception.QuickQuarantineException;

public class Factory {
	
	private static final Logger LOGGING = LoggerFactory.getLogger(Factory.class);
	
	/**
	 * 从配置中读取线程池维护线程的最大数量(最大线程数)
	 * 根据实际CPU核心数计算,
	 * a.如果当前业务是计算密集型,比较理想的方案是 最大线程数 = CPU核数+1
	 *   也可以设置成CPU核数*2,这要看JDK版本,以及CPU配置(是否有超线程).
	 *   对于JDK1.8来说,里面增加了一个并行计算,计算密集型的较理想的方案是 最大线程数=CPU核数*2
	 * b.如果当前业务是IO密集型,公式可以是  最大线程数=CPU核数/(1-阻塞系数)
	 *   这个阻塞系数一般为0.8~0.9之间。
	 *   根据公式,对于双核来说,比较理想的最大线程数就是20,八核就是80
	 */
	public static final String MAXIMUM_POOL_SIZE = "maximumPoolSize";
	
	/**
	 * 从配置中读取线程池维护线程的最少数量(核心线程数)
	 * 考虑该种业务是否常用?是否高并发?视实际情况而定,但不能超过线程池维护线程的最大数量
	 * 如果不常用或非高并发,可以设置核心线程数为0
	 * 否则可以取中值,即最大线程数的一半
	 * */
	public static final String CORE_POOL_SIZE = "corePoolSize";
	
	/**
	 * 我们必须使用有界阻塞队列,防止出现OOM
	 * 从配置中读取有界阻塞队列大小
	 * 如何计算阻塞队列大小?
	 * 计算方法1:
	 * a.需统计一段时间内的平均业务量,单笔业务处理成功的平均耗时
	 * b.根据二八原则,即80%的业务量在20%的时间内处理,不断压缩业务量与时间
	 * c.当时间压缩至等于单笔业务处理成功的平均耗时,对应的业务量多少,即为有界阻塞队列大小
	 * 计算方法2:
	 * 统计该种业务的并发数、峰值,取中值等,视实际情况而定.如果过大考虑计算方法1
	 * */
	public static final String WORK_QUEUE_SIZE = "workQueueSize";
	
	/**
	 * 从配置中读取空闲线程超时时间
	 * 根据单笔业务处理时间计算,视实际情况而定.
	 * 当线程空闲时间达到keepAliveTime时,线程会退出,直到线程数量=corePoolSize
     * 如果allowCoreThreadTimeout=true,则会直到线程数量=0
	 */ 
	public static final String KEEP_ALIVE_TIME = "keepAliveTime";
	
	/**
	 * 从配置中读取是否允许核心线程空闲超时,退出
	 * 一般使用不允许,即可
	 */ 
	public static final String ALLOW_CORE_THREAD_TIMEOUT = "allowCoreThreadTimeout";
	
	/**
	 * 从配置中读取,一系列类的静态方法
	 * 通过反射调用指定方法
	 *
	 */ 
	public static final String SERIES_CLASS_STATIC_METHOD_NAME = "seriesClassStaticMethodName";
	
	/**
	 * 从配置中读取,一系列单元子业务
	 * 定制的线程池必须与实际具体某种业务绑定(即不是全部业务,也不是单笔业务),才能发挥最佳作用.
	 */ 
	public static final String SERIES_OPERATION = "seriesOperation";
	
	/**
	 * 定制的线程池参数列表
	 */
	private static Map<String, Map<String, Object>> threadPoolOperationMap = new HashMap<String, Map<String, Object>>();
	
	/**
	 * 默认未加载配置
	 */
	public static boolean isBuilder = false;
	
	static {
		String path = FactoryBuilder.getPath();
		try {
			LOGGING.info("开始加载配置清单...");
			getResourceFromProperties(path);
			LOGGING.info("加载配置上下文:[ {} ],成功!", threadPoolOperationMap);
			isBuilder = true;
		} catch (Exception e) {
			LOGGING.error("加载配置失败!", e);
		}
		
	}
	/**
	 * 根据具体业务,获取相应的线程池初始化参数列表
	 * @param operation
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Object> getThreadPoolOperationMap(String operation) {
		return threadPoolOperationMap.get(operation);
	}
	
	private static void getResourceFromProperties(String path) throws Exception {
		// 获取配置清单文件 ,格式:业务类型 = 映射文件名
		Properties p = (Properties) getProperties(path).get(0);
		Enumeration<?> e = p.propertyNames();  
		String dirName = FactoryBuilder.getDirName();
	    while (e.hasMoreElements()) {
	        String operation = (String) e.nextElement();  
	        String fileName = p.getProperty(operation); 
	        // 根据配置清单文件,获取相关映射文件,映射文件起始于清单文件所在工作目录
	        String filePath = String.format("%s%s%s", dirName, FactoryBuilder.SEPARATOR, fileName);
	        List<Object> list = getProperties(filePath);
	        Properties config = (Properties) list.get(0);
	        String realPath = ((URL)list.get(1)).getFile();
	        LOGGING.info("正在加载配置:[ 业务={}, 文件路径={} ]...", operation, realPath);	  
	        /**
			 *  从配置中读取线程池相关参数 
			 *  a.在数据库中配置,提供想相应的表并至少包含如下字段,所有数据类型可以统一为String
			 *  b.要作为一个独立的框架,使用配置文件的移植性比较好
			 */
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MAXIMUM_POOL_SIZE, Integer.parseInt(checkEmpty(config.getProperty(MAXIMUM_POOL_SIZE))));
			map.put(CORE_POOL_SIZE, Integer.parseInt(checkEmpty(config.getProperty(CORE_POOL_SIZE))));
			map.put(WORK_QUEUE_SIZE, Integer.parseInt(checkEmpty(config.getProperty(WORK_QUEUE_SIZE))));
			map.put(KEEP_ALIVE_TIME, Integer.parseInt(checkEmpty(config.getProperty(KEEP_ALIVE_TIME))));
			if ("0".equals(checkEmpty(config.getProperty(ALLOW_CORE_THREAD_TIMEOUT)))) {
				map.put(ALLOW_CORE_THREAD_TIMEOUT, Boolean.FALSE);
			} else {
				map.put(ALLOW_CORE_THREAD_TIMEOUT, Boolean.TRUE);
			}
			
			// 封装一系列类方法以及对应的单元子业务 
			List<String> seriesClassMethod = Arrays.asList(checkEmpty(config.getProperty(SERIES_CLASS_STATIC_METHOD_NAME)).split(VERTICAL_LINE));
			List<String> seriesOperation = Arrays.asList(checkEmpty(config.getProperty(SERIES_OPERATION)).split(VERTICAL_LINE));
			map.put(SERIES_CLASS_STATIC_METHOD_NAME, seriesClassMethod);
			map.put(SERIES_OPERATION, seriesOperation);
			threadPoolOperationMap.put(operation, map);
			LOGGING.info("加载配置:[ 业务={}, 文件路径={} ],成功!", operation, realPath);
	    }  
	}
	
	private static List<Object> getProperties(String path) throws IOException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		URL url = contextClassLoader.getResource(path);
		InputStream is = contextClassLoader.getResourceAsStream(path);
		if (is == null) {
			throw new QuickQuarantineException(String.format("相对路径:[ %s ],找不到资源!", path));
		}
		Properties p = new Properties();
		p.load(is);
		List<Object> list = new ArrayList<Object>(2);
		list.add(p);
		list.add(url);
		return list;
	}
	
	private static String checkEmpty(String s) {
		if (s == null || s.isEmpty()) {
			throw new QuickQuarantineException("缺少配置项!");
		}
		return s.trim();
	}
}