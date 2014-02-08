package org.hdstar.common;

/**
 * 
 * �ύ����ʱ�����ͣ������༭���½��ͻظ� <br/>
 * ����: 2014��2��8�� ����5:12:09 <br/>
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
