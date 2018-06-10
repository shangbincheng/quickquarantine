package com.github.quickquarantine.test;

import java.util.List;

import com.github.quickquarantine.entity.OperationInput;
import com.github.quickquarantine.entity.OperationResult;

public class Operation1 {
	
	@SuppressWarnings("unchecked")
	public static OperationResult sub1(OperationInput input) {
		System.out.println("**************operation2_sub1**************");
		List<Object> inputList = (List<Object>)input.getInput(OperationInput.LIST_TYPE);
		String a = (String)inputList.get(0);
		String b = (String)inputList.get(1);
		Integer c = (Integer)inputList.get(2);
		System.out.println("**************参数1:[ " + a + " ]************");
		System.out.println("**************参数2:[ " + b + " ]************");
		System.out.println("**************参数3:[ " + c + " ]************");
		OperationResult or = new OperationResult();
		or.setResult(a + b + c);
		or.success();
		return or;
	}
	
	public static OperationResult sub2(OperationInput input) {
		System.out.println("**************operation2_sub1**************");
		OperationResult or = new OperationResult();
		or.error("通信超时!");
		return or;
	}
}
