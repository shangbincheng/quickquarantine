package com.github.quickquarantine.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.github.quickquarantine.Factory;
import com.github.quickquarantine.FactoryBuilder;
import com.github.quickquarantine.QuickQuarantine;
import com.github.quickquarantine.entity.OperationInput;
import com.github.quickquarantine.entity.OperationResult;
import com.github.quickquarantine.impl.IQuickQuarantine;

public class IQuickQuarantineTest {

	@Test
	public void testDoAllOfMinimumOperation() throws Exception {
		// FactoryBuilder.setPath("node1", "quickquarantine.properties");
		FactoryBuilder.setPath("node1/node2", "list.properties");
		Class.forName("com.github.quickquarantine.Factory");
		if (Factory.isBuilder) {
			QuickQuarantine qq = new IQuickQuarantine();
			List<String> allMinimumOperation = new ArrayList<String>();
			allMinimumOperation.add("operation2_sub1");
			allMinimumOperation.add("operation2_sub2");
			String ofMaximumOperation = "operation2";
			List<OperationInput> input = new ArrayList<OperationInput>();
			OperationInput sub1_input = OperationInput.initOperationInput("");
			OperationInput sub2_input = OperationInput.initOperationInput(new ArrayList<Object>());
			input.add(sub1_input);
			input.add(sub2_input);
			System.out.println(input);
			List<OperationResult> list = qq.doAllOfMinimumOperation(allMinimumOperation, ofMaximumOperation, input);
			// 成功业务列表
			List<String> doOperation = new ArrayList<String>();
			for (OperationResult or : list) {
				if (OperationResult.SUCCESS.equals(or.getResultCode())) {
					doOperation.add(or.getOperation());
				}
			}
			System.out.println("已经处理的业务:" + doOperation);
		}
	}

	@Test
	public void testDoMaximumOperation() throws Exception {
		FactoryBuilder.setPath("node1", "quickquarantine.properties");
		// FactoryBuilder.setPath("node1/node2", "list.properties");
		Class.forName("com.github.quickquarantine.Factory");
		if (Factory.isBuilder) {
			QuickQuarantine qq = new IQuickQuarantine();
			
			String maximumOperation = "operation2";
			List<OperationInput> input = new ArrayList<OperationInput>();
			OperationInput sub1_input = OperationInput.initOperationInput("");
			OperationInput sub2_input = OperationInput.initOperationInput(new ArrayList<Object>());
			OperationInput sub3_input = OperationInput.initOperationInput(new HashMap<String, Object>());
			input.add(sub1_input);
			input.add(sub2_input);
			input.add(sub3_input);
			System.out.println(input);
			List<OperationResult> list = qq.doMaximumOperation(maximumOperation, input);
			// 获取成功业务列表
			List<String> doOperation = new ArrayList<String>();
			for (OperationResult or : list) {
				if (OperationResult.SUCCESS.equals(or.getResultCode())) {
					doOperation.add(or.getOperation());
				}
			}
			System.out.println("当前处理成功的业务:" + doOperation);
		}
	}

	@Test
	public void testDoMinimumOperation() throws Exception {
		// FactoryBuilder.setPath("node1", "quickquarantine.properties");
		// FactoryBuilder.setPath("node1/node2", "list.properties");
		Class.forName("com.github.quickquarantine.Factory");
		if (Factory.isBuilder) {
			QuickQuarantine qq = new IQuickQuarantine();
			
			String maximumOperation = "operation2";
			List<Object> inputList = new ArrayList<Object>();
			inputList.add("a");
			inputList.add("b");
			inputList.add(1);
			OperationInput sub1_input = OperationInput.initOperationInput(inputList);
			System.out.println(sub1_input);
			OperationResult doOperation = qq.doMinimumOperation("operation1_sub1", "com.github.quickquarantine.test.Operation1.sub1", maximumOperation, sub1_input);
			
			System.out.println("当前的业务:" + doOperation);
		}
	}
	
}
