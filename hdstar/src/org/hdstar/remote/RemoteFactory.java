package org.hdstar.remote;

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

}
