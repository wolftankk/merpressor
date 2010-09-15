/**
 * 
 */
package com.pptv.merpressor;

import java.util.NoSuchElementException;

/*
 * @Name: FileListException.java
 * @Author: Fdream
 * @Email: fdream@live.com
 * @Website: http://fdream.net
 * @Version: 
 * @Creation: 2010-9-15 下午02:18:45
 */

/**
 * @author Administrator
 *
 */
public class FileListException extends NoSuchElementException
{
	public String path = "";
	
	public FileListException(String path){
		super();
		this.path = path;
	}
}
