package com.github.quickquarantine.entity;

import java.io.Serializable;

public class OperationResult implements Serializable {
	
	/** QuickQuarantine统一标准错误码  */ 
	public static final String ERROR = "error";
	
	/** QuickQuarantine统一标准成功码  */
	public static final String SUCCESS = "success";
	
	/** 具体业务 */
	private String operation;
	
	/** 返回原因 */
	private String errorReason;
	
	/** 返回码 */
	private String resultCode;
	
	/** 返回对象*/
	private Object result;
	
	private static final long serialVersionUID = 736190871922642627L;

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void error(String errorReason) {
		this.resultCode = ERROR;
		this.errorReason = errorReason;
	}
	
	public void success() {
		this.resultCode = SUCCESS;
	}
	
	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public OperationResult() {
		super();
	}

	@Override
	public String toString() {
		return String.format("OperationResult [operation=%s, errorReason=%s, resultCode=%s, result=%s ]", operation, errorReason, resultCode, result);
	}
	
}