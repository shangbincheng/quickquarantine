package com.github.quickquarantine.test;

import com.github.quickquarantine.entity.OperationInput;
import com.github.quickquarantine.entity.OperationResult;

public class Operation2 {
	
	
	public static OperationResult sub1(OperationInput input) {
		System.out.println("**************operation2_sub1**************");
		OperationResult or = new OperationResult();
		or.success();
		return or;		
	}
	
	public static OperationResult sub2(OperationInput input) {
		System.out.println("**************operation2_sub2**************");
		OperationResult or = new OperationResult();
		or.error("通信超时!");
		return or;	
	}
	
	public static OperationResult sub3(OperationInput input) {
		System.out.println("**************operation2_sub1**************");
		OperationResult or = new OperationResult();
		or.success();
		return or;		
	}
}
