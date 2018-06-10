package com.github.quickquarantine;

import java.util.List;

import com.github.quickquarantine.entity.OperationInput;
import com.github.quickquarantine.entity.OperationResult;



/**
 * 并行处理业务类
 * 关键词:隔离,异步IO,并行计算
 * @author shangbincheng001
 * @date 2018-06-07
 */
public interface QuickQuarantine {
	/**
	* 指定一个单元子业务列表,并行处理所属顶级非单元业务中的全部或部分单元子业务
	* 单元业务:以为不可分割的,或视为一个整体的
	* @param allMinimumOperation 部分单元子业务列表
	* @param ofMaximumOperation 所属顶级非单元业务
	* @param input 部分单元子业务参数顺序输入
	* @return 部分单元子业务处理结果
	*/
	public List<OperationResult> doAllOfMinimumOperation(List<String> allMinimumOperation, String ofMaximumOperation, List<OperationInput> input) throws Exception;
	
	/**
	* 指定一个顶级业务,并行处理其所有单元子业务
	* 单元业务:以为不可分割的,或视为一个整体的
	* 顶级业务包括:
	* a.顶级非单元业务
	* b.顶级单元业务
	* @param maximumOperation 顶级业务
	* @param input 所有单元子业务参数顺序输入
	* @return 顶级业务的处理结果
	*/
	public List<OperationResult> doMaximumOperation(String maximumOperation, List<OperationInput> input) throws Exception;

	/**
	* 指定一个单元业务,将它动态地,额外地纳入到所属顶级业务或特性相近的其他业务的线程池中
	* 单元业务:以为不可分割的,或视为一个整体的
	* 单元业务包括:
	* a.单元子业务
	* b.顶级单元业务
	* 会增加原有线程池的压力,降低性能
	* @param minimumOperation 单元业务
	* @param input 单元业务参数输入
	* @return 单元业务的处理结果
	*/
	public OperationResult doMinimumOperation(String minimumOperation, String classMethod, String ofMaximumOperation, OperationInput input) throws Exception;
}