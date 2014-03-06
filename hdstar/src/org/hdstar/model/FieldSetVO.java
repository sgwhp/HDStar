package org.hdstar.model;

/**
 * <fieldset>标签bean 当fieldSet为null时，表示当前为最后一层
 * 
 * @author robust
 * 
 */
public class FieldSetVO {

	public String content;
	public String legend;
	public FieldSetVO fieldSet;
}
