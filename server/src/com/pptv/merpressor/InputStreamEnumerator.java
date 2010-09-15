/**
 * 
 */
package com.pptv.merpressor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/*
 * @Name: InputStreamEnumerator.java
 * 
 * @Author: Fdream
 * 
 * @Email: fdream@live.com
 * 
 * @Website: http://fdream.net
 * 
 * @Version:
 * 
 * @Creation: 2010-8-5 上午11:32:50
 */

/**
 * 
 * 
 */
public class InputStreamEnumerator implements Enumeration<FileInputStream>
{
	private Enumeration<String> files;

	public InputStreamEnumerator(Vector<String> files)
	{
		this.files = files.elements();
	}

	public boolean hasMoreElements()
	{
		return files.hasMoreElements();
	}

	public FileInputStream nextElement() throws FileListException
	{
		String path = files.nextElement().toString();
		
		try
		{
			FileInputStream input = new FileInputStream(path);
			return input;
		}
		catch (IOException e)
		{
			throw new FileListException(path);
		}
	}
}
