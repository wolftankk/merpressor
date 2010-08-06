package com.pptv.merpressor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

/*
 * @Name: SimpleCharDetect.java 
 * @Author: Fdream 
 * @Email: fdream@live.com
 * @Website: http://ooboy.net 
 * @Version: 
 * @Creation: Dec 18, 2008 10:30:58 AM
 */

public class SimpleCharsetDetector
{

	private static SimpleCharsetDetector _instance = null;

	/**
	 * 构造函数
	 */
	private SimpleCharsetDetector()
	{
		// do nothing
	}

	public static SimpleCharsetDetector instance()
	{
		if (null == _instance)
		{
			_instance = new SimpleCharsetDetector();
		}
		return _instance;
	}

	/**
	 * 根据文件路径获取编码格式
	 * @param path
	 * 			文件路径
	 * @return 编码格式
	 * @throws java.io.IOException
	 */
	@SuppressWarnings("finally")
	public String getFileEncoding(String path) throws java.io.IOException
	{
		RandomAccessFile raf = null;
		String encode = "";
		try
		{
			raf = new RandomAccessFile(path, "r");
			raf.seek(0);
			int flag1 = 0;
			int flag2 = 0;
			int flag3 = 0;
			if (raf.length() >= 2)
			{
				flag1 = raf.readUnsignedByte();
				flag2 = raf.readUnsignedByte();
			}
			if (raf.length() >= 3)
			{
				flag3 = raf.readUnsignedByte();
			}
			encode = getEncode(flag1, flag2, flag3);
		}
		finally
		{
			if (raf != null)
			{
				raf.close();
			}
			return encode;
		}

	}

	/**
	 * detect charset
	 * @param flag1
	 * 		  the 1st byte
	 * @param flag2
	 * 		  the 2nd byte
	 * @param flag3
	 * 		  the 3rd byte
	 * @return charset
	 */
	private String getEncode(int flag1, int flag2, int flag3)
	{
		String encode = "";
		// txt文件的开头会多出几个字节，分别是FF、FE（Unicode）,

		// FE、FF（Unicode big endian）,EF、BB、BF（UTF-8）
		if (flag1 == 255 && flag2 == 254)
		{
			encode = "Unicode";
		}
		else if (flag1 == 254 && flag2 == 255)
		{
			encode = "UTF-16";
		}
		else if (flag1 == 239 && flag2 == 187 && flag3 == 191)
		{
			encode = "UTF-8";
		}
		return encode;
	}

	/**
	 * 根据文件路径直接返回文件流
	 * 
	 * @param path
	 * 			文件路径
	 * @return 文件流
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public InputStreamReader getInputStreamReader(String path) throws FileNotFoundException, IOException
	{
		InputStreamReader isr = null;
		String encode = getFileEncoding(path);
		if (encode.equals(""))
		{
			isr = new InputStreamReader(new FileInputStream(path));
		}
		else
		{
			isr = new InputStreamReader(new FileInputStream(path), encode);
		}

		return isr;
	}

}
