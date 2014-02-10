package org.hdstar.ptadapter;

import org.hdstar.common.PTSiteType;

public class PTFactory {
	
	public static PTAdapter newInstanceByName(String className){
		try {
			return (PTAdapter) Class.forName("org.hdstar.ptadapter." + className)
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
	
	public static PTAdapter newInstanceByType(PTSiteType type){
		return newInstanceByName(type.name());
	}

}
