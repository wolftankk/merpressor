package com.pptv.merpressor;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/*
 * @Name: FileListParser.java
 * 
 * @Author: Fdream
 * 
 * @Email: fdream@live.com
 * 
 * @Website: http://fdream.net
 * 
 * @Version:
 * 
 * @Creation: 2010-8-3 下午04:59:44
 */

/**
 * @author Administrator
 * 
 */
public class FileListParser
{
	private String path = "";

	public FileListParser()
	{
		// do nothing
	}

	public FileListParser(String path)
	{
		this.path = path;
	}

	public FileList Parse(String path)
	{
		this.path = path;
		return DoParse();
	}

	public FileList Parse()
	{
		return DoParse();
	}

	private FileList DoParse()
	{
		FileList list = new FileList();

		File inputXml = new File(this.path);
		SAXReader saxReader = new SAXReader();
		try
		{
			Document doc = saxReader.read(inputXml);
			Element root = doc.getRootElement();
			list.encoding = root.attributeValue("encoding");
			for (Iterator<Element> item = root.elementIterator(); item.hasNext();)
			{
				list.files.add(item.next().getText());
			}
		}
		catch (Exception e)
		{
			list = null;
			//fileList.add(e.toString());
		}

		return list;
	}
}
