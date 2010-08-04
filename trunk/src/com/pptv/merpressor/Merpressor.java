package com.pptv.merpressor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.timer.Timer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Servlet implementation class Compressor
 */
public class Merpressor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Merpressor()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Pattern pattern = Pattern.compile("^/([\\w/]*?/build/(\\w+?))(?:/e_(\\d+))?(?:/r_(\\d+))?.((?:js)|(?:css))", Pattern.CASE_INSENSITIVE);
		String uri = request.getRequestURI();
		Matcher matcher = pattern.matcher(uri);

		int expires = 3600 * 24; // default expires: 1 day
		String type = null; // default type
		String ext = null; // extension name
		String root = request.getSession(true).getServletContext().getRealPath("/");
		if (matcher.find())
		{
			// 404: not found
			if (matcher.group(2) == null)
			{
				response.setHeader("File-Not-Found", "NO-CONFIG-SPECIFICED");
				response.setStatus(404);
				return;
			}
			// parse expires
			if (matcher.group(3) != null)
			{
				try
				{
					expires = Integer.parseInt(matcher.group(3), 10) * 60;
				}
				catch (Exception e)
				{
					//
				}
			}
			// parse types
			if (matcher.group(5) != null)
			{
				if (matcher.group(5).equalsIgnoreCase("js"))
				{
					type = "application/x-javascript";
					ext = ".js.xml";
				}
				else
				{
					type = "text/css";
					ext = ".css.xml";
				}
			}
		}
		else
		{
			response.setHeader("File-Not-Found", "URL-NOT-MATCHED");
			response.setStatus(404);
			return;
		}

		String path = root + matcher.group(1) + ext;
		FileList list = new FileListParser().Parse(path);
		if (list == null)
		{
			response.setHeader("File-Not-Found", "LIST-IS-EMPTY-OR-NO-MATCHED-CONFIG");
			response.setStatus(404);
			return;
		}

		if (type != null)
		{
			response.setHeader("Content-Type", type + "; charset=utf-8");
		}

		Date expTime = new Date(new Date().getTime() + expires * Timer.ONE_SECOND);
		Locale local = Locale.US;
		DateFormat fmt = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", new DateFormatSymbols(local));
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		response.setHeader("Cache-Control", "max-age=" + Integer.toString(expires, 10));
		response.setHeader("Expires", fmt.format(expTime));

		String folder = path.replaceAll("/build/.*$", "/");
		PrintWriter writer = response.getWriter();
		String output = root + matcher.group(0);

		File f = new File(output.substring(0, output.lastIndexOf("/")));
		if (!f.isDirectory())
		{
			f.mkdirs();
		}
		f = new File(output);
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			}
			catch (Exception e)
			{
				//
			}
		}

		FileChannel out = new FileOutputStream(output).getChannel();
		MappedByteBuffer mbuf;
		FileChannel in;
		for (int i = 0, len = list.files.size(); i < len; i++)
		{
			try
			{
				in = new FileInputStream(folder + list.files.get(i)).getChannel();
				mbuf = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
				out.write(mbuf);
				in.close();
			}
			catch (Exception e)
			{
				response.setHeader("File-Not-Found", list.files.get(i));
				response.setStatus(404);
				return;
			}
		}
		out.close();

		InputStreamReader inReader = new InputStreamReader(new FileInputStream(output), list.encoding);

		int linebreakpos = -1;
		boolean verbose = false;
		if (matcher.group(5).equalsIgnoreCase("js"))
		{
			ErrorReporter logger = new ErrorReporter()
			{
				public void warning(String message, String sourceName, int line, String lineSource, int lineOffset)
				{
				}

				public void error(String message, String sourceName, int line, String lineSource, int lineOffset)
				{
				}

				public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset)
				{
					return new EvaluatorException(message);
				}
			};

			try
			{
				JavaScriptCompressor compressor = new JavaScriptCompressor(inReader, logger);
				// Close the input stream first, and then open the output
				// stream,
				// in case the output file should override the input file.
				boolean munge = true;
				boolean preserveAllSemiColons = false;
				boolean disableOptimizations = false;
				writer.print(compressor.compress(linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations).toString());
			}
			catch (Exception e)
			{
				// e.printStackTrace();
				// Return a special error code used specifically by the web
				// front-end.
				// System.exit(2);
				response.setStatus(500);
				return;
			}
		}
		else if (type.equalsIgnoreCase("css"))
		{
			CssCompressor compressor = new CssCompressor(inReader);
			// Close the input stream first, and then open the output stream,
			// in case the output file should override the input file.

			writer.print(compressor.compress(linebreakpos));
		}
		inReader.close();
		inReader = null;
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// TODO Auto-generated method stub
	}

}
