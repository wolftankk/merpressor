/**
 * 
 */
package com.pptv.merpressor;

/*
 * @Name: Cache.java
 * @Author: Fdream
 * @Email: fdream@live.com
 * @Website: http://fdream.net
 * @Version: 
 * @Creation: 2010-8-4 上午10:18:28
 */

/**
 * @author Administrator
 *
 */
public class Cache
{
		
	private static Cache instance = null;
	
	private Cache(){}
	
	public static synchronized Cache getInstance(){
		
		if(instance == null){
			instance = new Cache();
		}
		
		return instance;
	}

}
