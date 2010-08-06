package com.pptv.merpressor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

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
		// web root
		String root = request.getSession(true).getServletContext().getRealPath("/");

		// static resources home
		String home = root + request.getParameter("home");

		// config file
		String config = request.getParameter("config");

		// file type and mime-type
		String type = request.getParameter("type");
		String mimeType = "application/x-javascript";
		if (type.equalsIgnoreCase("css"))
		{
			mimeType = "text/css";
		}

		// expires, default expires: 1 hour
		int expires = 3600;
		String expiresStr = request.getParameter("expires");
		if (expiresStr != null && !expiresStr.equalsIgnoreCase(""))
		{
			expires = Integer.parseInt(request.getParameter("expires")) * 60;
		}

		String encoding = "utf-8";
		Vector<String> files = new Vector<String>();
		// a real javascript or css file
		if (config == null || config.equalsIgnoreCase(""))
		{
			String file = root + request.getParameter("path");
			encoding = SimpleCharsetDetector.instance().getFileEncoding(file);
			if(encoding.equalsIgnoreCase("")){
				encoding = "utf-8";
			}
			files.add(file);

			response.setHeader("File-Path", file);
		}
		// a config file
		else
		{
			// parse config file
			FileList list = new FileListParser().Parse(home + config, home);
			// config file not found
			if (list == null)
			{
				response.setHeader("File-Not-Found", config);
				response.setStatus(404);
				return;
			}
			else
			{
				encoding = list.encoding;
				files = list.files;
			}
		}		

		// calculate expires header
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, (int) (expires * Timer.ONE_SECOND));
		Date expTime = cal.getTime();
		Locale local = Locale.US;
		DateFormat fmt = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", new DateFormatSymbols(local));
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		// set header
		response.setHeader("Cache-Control", "max-age=" + Integer.toString(expires, 10));
		response.setHeader("Expires", fmt.format(expTime));
		response.setHeader("Content-Type", mimeType + "; charset=" + encoding);

		// merge and compress
		PrintWriter writer = response.getWriter();

		InputStreamReader inReader = new InputStreamReader(new SequenceInputStream(new InputStreamEnumerator(files)), encoding);
		int linebreakpos = -1;
		boolean verbose = false;
		if (type.equalsIgnoreCase("js"))
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
			try{
			CssCompressor compressor = new CssCompressor(inReader);
			// Close the input stream first, and then open the output stream,
			// in case the output file should override the input file.

			writer.print(compressor.compress(linebreakpos));
			}
			catch(Exception e){
				//
			}
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
