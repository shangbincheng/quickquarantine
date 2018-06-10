package com.github.quickquarantine.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.quickquarantine.QuickQuarantine;
import com.github.quickquarantine.entity.OperationInput;
import com.github.quickquarantine.entity.OperationResult;
import com.github.quickquarantine.thread.OperationThreadPool;




public class IQuickQuarantine implements QuickQuarantine {
	
	private static final Logger LOGGING = LoggerFactory.getLogger(IQuickQuarantine.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public List<OperationResult> doAllOfMinimumOperation(List<String> allMinimumOperation, String ofMaximumOperation, List<OperationInput> input) throws InterruptedException {
		List<OperationResult> operationResult= new Vector<OperationResult>();
	    // 根据一个独立的业务,获得对应的可以共享的线程池
		List<Object> operationList = OperationThreadPool.getOperationList(ofMaximumOperation, true);
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor)operationList.get(0);
		Map<String, String> map = (Map<String, String>)operationList.get(1);
		int threadNum = allMinimumOperation.size();
		CountDownLatch countDownLatch = new CountDownLatch(threadNum);
		for (int i = 0; i < threadNum; i++) {
			String operation = allMinimumOperation.get(i);
			threadPool.execute(new OperationThreadPool.OperationThread(operation, map.get(operation), input.get(i), operationResult, countDownLatch));
		}
		countDownLatch.await();
		LOGGING.info("处理结果:[ {} ].继续执行主线程!", operationResult);
		return operationResult;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<OperationResult> doMaximumOperation(String maximumOperation, List<OperationInput> input) throws Exception {
		List<OperationResult> operationResult= new Vector<OperationResult>();
	    // 根据一个独立的业务,获得对应的可以共享的线程池
		List<Object> operationList = OperationThreadPool.getOperationList(maximumOperation, true);
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor)operationList.get(0);
		Map<String, String> map = (Map<String, String>)operationList.get(1);
		int threadNum = map.size();
		CountDownLatch countDownLatch = new CountDownLatch(threadNum);
		int i = 0;
		for (Entry<String, String> entry : map.entrySet()) {
			threadPool.execute(new OperationThreadPool.OperationThread(entry.getKey(), entry.getValue(), input.get(i), operationResult, countDownLatch));
			i++;
		}
		
		countDownLatch.await();
		LOGGING.info("处理结果:[ {} ].继续执行主线程!", operationResult);
		return operationResult;
	}
	
	@Override
	public OperationResult doMinimumOperation(String minimumOperation, String classMethod, String ofMaximumOperation, OperationInput input) throws Exception {
		List<OperationResult> operationResult= new Vector<OperationResult>();
	    // 根据一个独立的业务,获得对应的可以共享的线程池
		List<Object> operationList = OperationThreadPool.getOperationList(ofMaximumOperation, false);
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor)operationList.get(0);
		CountDownLatch countDownLatch = new CountDownLatch(1);
		threadPool.execute(new OperationThreadPool.OperationThread(minimumOperation, classMethod, input, operationResult, countDownLatch));
		countDownLatch.await();
		LOGGING.info("处理结果:[ {} ].继续执行主线程!", operationResult);
		return operationResult.get(0);
	}
}