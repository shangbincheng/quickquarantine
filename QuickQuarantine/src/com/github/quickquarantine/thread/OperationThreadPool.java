package com.github.quickquarantine.thread;

import static com.github.quickquarantine.Factory.ALLOW_CORE_THREAD_TIMEOUT;
import static com.github.quickquarantine.Factory.CORE_POOL_SIZE;
import static com.github.quickquarantine.Factory.KEEP_ALIVE_TIME;
import static com.github.quickquarantine.Factory.MAXIMUM_POOL_SIZE;
import static com.github.quickquarantine.Factory.SERIES_CLASS_STATIC_METHOD_NAME;
import static com.github.quickquarantine.Factory.SERIES_OPERATION;
import static com.github.quickquarantine.Factory.WORK_QUEUE_SIZE;
import static com.github.quickquarantine.Factory.getThreadPoolOperationMap;
import static com.github.quickquarantine.utils.SecurityUtils.PTN_CLASSNAME;
import static com.github.quickquarantine.utils.SecurityUtils.PTN_METHODNAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.quickquarantine.entity.OperationInput;
import com.github.quickquarantine.entity.OperationResult;
import com.github.quickquarantine.exception.QuickQuarantineException;
import com.github.quickquarantine.utils.SecurityUtils;;
/**
 * 通用定制业务连接池
 * 定制的线程池必须与实际具体某种业务绑定(即不是全部业务,也不是单笔业务),才能发挥最佳作用.
 * 注意:这里的业务的粒度,不一定是系统级别的,最好是类、方法级别,因保证属于当前线程池的单个线程业务逻辑完整地执行.
 * 重要:如果主线程要等待子线程所有执行完毕,汇总拢来后,才能执行后续任务。
 *    a.主线程千万不要使用shutdown()或shutdownNow() 等待与这两个方法无关
 *      shutdown(),当然是立即执行,也即是不再接受新任务了,但是它即不会强行终止正在执行的任务,也不会取消已经提交的任务。
 *      shutdownNow()对于尚未执行的任务,全部取消掉;对于正在执行的任务,发出interrupt()。
 *    b.使用awaitTermination()可以实现等待,但awaitTermination会一直等待,直到线程池状态为TERMINATED或者等待的时间到达了指定的时间。不能实现等待
 *    c.使用countDownLatch.await()方法可以非常简单的完成主线程的等待
 *    
 * @author shangbincheng001
 *
 */
public class OperationThreadPool {
	
	private static final Logger LOGGING = LoggerFactory.getLogger(OperationThreadPool.class);
	
	/**
	 * 当提交任务数超过maxmumPoolSize+workQueue之和时,任务会交给RejectedExecutionHandler来处理
	 * 两种情况会拒绝处理任务:
	 * a.线程池里面的线程数量达到 maximumPoolSize且 workQueue队列已满的情况下被尝试添加进来的任务,会拒绝新任务
	 * b.当线程池被调用shutdown()后,会等待线程池里的任务执行完毕.如果在调用shutdown()和线程池真正shutdown之间提交任务,会拒绝新任务
	 * 注意:不要使用shutdown()
     * handler: 线程池对拒绝任务的处理策略。在 ThreadPoolExecutor里面定义了 4 种 handler策略:
     * a.CallerRunsPolicy:这个策略重试添加当前的任务,他会自动重复调用 execute()方法,直到成功。
     * b.AbortPolicy:对拒绝任务抛弃处理,并且抛出异常。 默认
     * c.DiscardPolicy:对拒绝任务直接无声抛弃,没有异常信息。
     * d.DiscardOldestPolicy:对拒绝任务不抛弃，而是抛弃队列里面等待最久的一个线程,然后把拒绝任务加到队列
     * 这几种策略都不是很友好,需要自己实现RejectedExecutionHandler接口
     * 重要:一般进入拒绝机制, 我们把runnable任务拿出来,重新用阻塞操作put,来实现提交阻塞功能,保证不抛弃一个任务
	 */
	private static RejectedExecutionHandler handler = new RejectedExecutionHandler() {
		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			try {  
                // 核心改造点,由BlockingQueue的offer改成put阻塞方法,来实现提交阻塞功能,保证不抛弃一个任务
				executor.getQueue().put(r);  
			} catch (InterruptedException e) {  
				LOGGING.error("线程池饱和策略,提交阻塞失败!", e);
			}  	
		}
	};
	
	/**
	 * 定制的线程池列表
	 */
	private static Map<String, ThreadPoolExecutor>  threadPoolMap = new HashMap<String, ThreadPoolExecutor>();
	
	/**
	 * 根据一个独立的业务,获得对应的可以共享的线程池
	 * a.最大的业务拥有一个独立的线程池
	 * b.最小的,独立的业务共享一个线程池
	 * @param operation 最大的业务类型
	 * @param isMaximum 是否是顶级业务
	 * @return list 定制的,包含当前业务对应的线程池(第一个),当前业务对应执行计划,具体到最小业务单元(第二个);
	 * 	               如果返回null,表示获取线程池失败,调用者自行处理.
	 * 
	 * @author shangbincheng001
	 */
	public static List<Object> getOperationList(String operation, boolean isMaximum) {
		Map<String, Object> threadPoolParam = getThreadPoolOperationMap(operation);
		// 校验业务参数字典中是否存在当前业务
		if (threadPoolParam == null) {
			// 记录日志信息
			throw new QuickQuarantineException("未知或匹配失败的业务!");
		}

		ThreadPoolExecutor  threadPool = null;
		List<Object> operationList = new ArrayList<Object>();
		// 如果已存在线程池
		if (threadPoolMap.containsKey(operation)) {
			threadPool = threadPoolMap.get(operation);
			operationList.add(threadPool);
			if (isMaximum) {
				setOperationList(operationList, threadPoolParam);
			}
			return operationList;
		}
	    // 如果不存在线程池,则先创建线程池
		threadPool = createThreadPool(operation, threadPoolParam);
		operationList.add(threadPool);
		if (isMaximum) {
			setOperationList(operationList, threadPoolParam);
		}
		return operationList;
	}
	/**
	 * 创建非单元子业务对应的线程池
	 * @param operation
	 * @param threadPoolParam
	 */
	private static ThreadPoolExecutor createThreadPool(String operation, Map<String, Object> threadPoolParam) {
		int maximumPoolSize = ((Integer)threadPoolParam.get(MAXIMUM_POOL_SIZE)).intValue();
		int corePoolSize = ((Integer)threadPoolParam.get(CORE_POOL_SIZE)).intValue();
		int workQueueSize = ((Integer)threadPoolParam.get(WORK_QUEUE_SIZE)).intValue();
		int keepAliveTime = ((Integer)threadPoolParam.get(KEEP_ALIVE_TIME)).intValue();
		ThreadPoolExecutor  threadPool = null;
		synchronized(OperationThreadPool.class) {
			boolean allowCoreThreadTimeout = ((Boolean)threadPoolParam.get(ALLOW_CORE_THREAD_TIMEOUT)).booleanValue();
			/**
			 * ThreadFactory新建线程工厂
			 * 一般用来标记线程任务相关信息,方便跟踪日志信息
			 */
			ThreadFactory threadFactory = new OperationThreadFactory();
			threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(workQueueSize),  threadFactory, handler);
			threadPool.allowCoreThreadTimeOut(allowCoreThreadTimeout);
			threadPoolMap.put(operation, threadPool);
		}
		return threadPool;
	}
	
	@SuppressWarnings("unchecked")
	private static void setOperationList(List<Object> operationList, Map<String, Object> threadPoolParam) {
		List<String> seriesClassMethod = (List<String>)threadPoolParam.get(SERIES_CLASS_STATIC_METHOD_NAME);
		List<String> seriesOperation = (List<String>)threadPoolParam.get(SERIES_OPERATION);
		Map<String, String> map = new HashMap<String, String>();
		int size = seriesOperation.size();
		for (int i = 0; i < size; i++) {
			map.put(seriesOperation.get(i), seriesClassMethod.get(i));
		}
		operationList.add(map);
	}
	
	/**
	 * 静态成员内部类,业务线程工厂.对业务线程做一些额外处理:比如标记线程,方便跟踪日志信息
	 * 
	 * @author shangbincheng001
	 *
	 */
	private static class OperationThreadFactory implements ThreadFactory {
		
		private AtomicInteger count = new AtomicInteger(0);
		
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r); 
			// 方便跟踪日志信息
	        String threadName = OperationThreadFactory.class.getSimpleName() + "-" + count.addAndGet(1);  
	        t.setName(threadName);  
	        return t; 
		}

	}
	/**
	 * 静态成员内部类,业务线程
	 * 
	 * @author shangbincheng001
	 *
	 */
	public static class OperationThread implements Runnable {
		
		private static final Logger LOGGING = LoggerFactory.getLogger(OperationThread.class);
		
		/**
		 * 子线程中运行的业务唯一标识
		 */
		private String operation;
		
		/**
		 * 子线程中运行的业务相应的执行计划,一定是一个静态方法
		 */
		private String classMethod;
		
		/**
		 * 输入参数
		 */
		private OperationInput input;
		
		/**
		 * 可能是一个线程安全的结果列表,汇总各子业务的结果
		 */
		private List<OperationResult> operationResult;
		
		/**
		 * 主线程中设置执行线程个数,并调countDownLatch.await()等待;
		 * 子线程中调countDownLatch.countDown()登记完成个数,当全部执行完毕则主线程向下
		 */
		private CountDownLatch countDownLatch;
		/**
		 * 子线程运行中的具体业务
		 * @param operation 具体业务
		 * @param operationResult 各业务返回结果列表
		 * @param countDownLatch 子线程计数器
		 * @param operationParam 具体业务需要提供的参数
		 * 
		 * @author shangbincheng001
		 */
		public OperationThread(String operation, String classMethod, OperationInput input, List<OperationResult> operationResult, CountDownLatch countDownLatch) {
			this.operationResult = operationResult;
			this.countDownLatch = countDownLatch;
			this.operation = operation;
			this.classMethod = classMethod;
			this.input = input;
		}
		
		public void run() {
			OperationResult or = null;
			try {
				// 根据operation运行相应的业务,此处可以用反射调用
				Matcher m = PTN_CLASSNAME.matcher(classMethod);
				String className = m.replaceAll("");
				String methodName = SecurityUtils.getFirstStr(PTN_METHODNAME, classMethod);
				LOGGING.info("开始执行业务:[ 业务={} ,输入={} ,执行计划=类{}的静态方法{} ]...", new Object[]{operation, input, className, methodName});
				Object ro = SecurityUtils.echoStaticMethodByClass(className, methodName, new Class<?>[]{OperationInput.class}, new Object[]{input}, OperationResult.class);
				// 完整性判断
				if (ro == null) {
					throw new QuickQuarantineException(String.format("返回业务:[ 业务=%s, 返回=%s ],返回结果为空!", operation, or));
				} 
			    or = (OperationResult)ro;
			    String resultCode = or.getResultCode();
			    if (resultCode == null) {
					throw new QuickQuarantineException(String.format("返回业务:[ 业务=%s, 返回=%s ],缺少返回码!", operation, or));
				}
			    LOGGING.info("执行业务:[ 业务={}, 输入={}, 返回={} ],成功!", new Object[]{operation, input, or});
			} catch (Exception e) {
				// 记录日志信息
				LOGGING.error("执行业务失败!", e);
				// 转换异常
				or = new OperationResult();
				// 保存案发现场信息
				or.error(String.format("案发现场信息:[ %s ]", e.toString()));
			} finally {
				// 汇总各子业务的结果 operationResult
				or.setOperation(operation);
				operationResult.add(or);
				LOGGING.info("{}线程执行完毕!", Thread.currentThread().getName());
				countDownLatch.countDown();
			}
		}
		
	}
}