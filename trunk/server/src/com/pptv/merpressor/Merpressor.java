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

		// file type
		String type = request.getParameter("type");

		// expires, default expires: 1 hour
		int expires = 3600;
		if (!request.getParameter("expires").isEmpty())
		{
			expires = Integer.parseInt(request.getParameter("expires")) * 60;
		}

		// parse config file

		FileList list = new FileListParser().Parse(home + config, home);

		// config file not found
		if (list == null)
		{
			response.setHeader("File-Not-Found", config);
			response.setStatus(404);
			return;
		}

		// set mime-type and charset
		String mimeType = "application/x-javascript";
		if (type.equalsIgnoreCase("css"))
		{
			mimeType = "text/css";
		}
		response.setHeader("Content-Type", mimeType + "; charset=" + list.encoding);

		// calculate expires header
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, (int) (expires * Timer.ONE_SECOND));
		Date expTime = cal.getTime();
		Locale local = Locale.US;
		DateFormat fmt = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", new DateFormatSymbols(local));
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		// set expires header
		response.setHeader("Cache-Control", "max-age=" + Integer.toString(expires, 10));
		response.setHeader("Expires", fmt.format(expTime));

		PrintWriter writer = response.getWriter();
		
		InputStreamReader inReader = new InputStreamReader(new SequenceInputStream(new InputStreamEnumerator(list.files)), list.encoding);
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
