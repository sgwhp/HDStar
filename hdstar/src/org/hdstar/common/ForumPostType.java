package org.hdstar.common;

/**
 * 
 * 提交数据时的类型，包括编辑、新建和回复 <br/>
 * 日期: 2014年2月8日 下午5:12:09 <br/>
 * 
 * @author robust
 */
public enum ForumPostType {
	Edit("edit"), New("new"), Reply("reply");
	private final String value;

	ForumPostType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static ForumPostType getByValue(String value) {
		for (ForumPostType type : ForumPostType.values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		return null;
	}

}
