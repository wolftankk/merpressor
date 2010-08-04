<%@ page language="java" contentType="text/html; charset=UTF-8"
    import="com.pptv.data.*" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<h1>GOD!</h1>
<%
	FileListParser parser = new FileListParser();
out.print(parser.Parse("D:\\workspaces\\java\\merpressor\\WebContent\\js.xml"));
%>
</body>
</html>