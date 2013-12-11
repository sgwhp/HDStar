package org.hdstar.remote;

import org.hdstar.common.RemoteType;

public class RemoteFactory {

	public static RemoteBase newInstanceByName(String className) {
		try {
			return (RemoteBase) Class.forName("org.hdstar.remote." + className)
					.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static RemoteBase newInstanceByType(RemoteType type) {
		return newInstanceByName(type.name());
	}

}
