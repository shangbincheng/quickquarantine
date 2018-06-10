package com.github.quickquarantine.entity;

import java.util.List;
import java.util.Map;
/**
 * 单元子业务的输入,有三种不同的收参方式
 * a.列表 
 * b.字典
 * c.一般对象
 * @author shangbincheng001
 *
 */
public class OperationInput {
	
	public static final byte LIST_TYPE = 0;
	
	public static final byte MAP_TYPE = 1;
	
	public static final byte O_TYPE = 2;
	
	private List<Object> list;
	
	private Map<String, Object> map;
	
	private Object o;
	
	private byte form;
	
	private OperationInput() {
		super();
	}
	
	public static OperationInput initOperationInput(List<Object> list) {
		OperationInput oi = new OperationInput();
		oi.setList(list);
		oi.setForm(LIST_TYPE);
		return oi;
	}
	
	public static OperationInput initOperationInput(Map<String, Object> map) {
		OperationInput oi = new OperationInput();
		oi.setMap(map);
		oi.setForm(MAP_TYPE);
		return oi;
	}
	
	public static OperationInput initOperationInput(Object o) {
		OperationInput oi = new OperationInput();
		oi.setO(o);
		oi.setForm(O_TYPE);
		return oi;
	}

	private void setList(List<Object> list) {
		this.list = list;
	}

	private void setMap(Map<String, Object> map) {
		this.map = map;
	}

	private void setO(Object o) {
		this.o = o;
	}

	public void setForm(byte form) {
		this.form = form;
	}
	/**
	 * 根据输入类型,从输入包装类中获取输入
	 * 如果输入类型与封箱时类型不匹配时,返回null
	 * @param type
	 * @return object 需要根据封箱时类型强转引用类型,后才可使用
	 */
	public Object getInput(byte type) {
		if (form != type) {
			return null;
		}
		switch (form) {
			case LIST_TYPE : return list;
			case MAP_TYPE : return map;
			case O_TYPE : return o;
			default : return null;
		}
	}
	
	@Override
	public String toString() {
		switch (form) {
			case LIST_TYPE : return String.format("OperationInput:[ list=%s, form=%s ]", list, form);
			case MAP_TYPE : return String.format("OperationInput:[ map=%s, form=%s ]", map, form);
			case O_TYPE : return String.format("OperationInput:[ o=%s, form=%s ]", o, form);
			default : return "OperationInput:[  ]";
		}
	}
	
	
}