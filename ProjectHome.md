# Merpressor —— 在线自动合并、压缩JS文件 #

## 目标 ##
让前端开发更智能，更自动化，开发、调试和发布一体化。

把前端开发人员从无聊的、重复的合并和压缩工作中解放出来，解决公网和内网调试时来回切换及换用各种不同工具的痛苦，同时使版本控制、缓存控制更加智能（目标是这样，但是缓存功能目前还不完善，建议暂时不要使用，请使用WEB服务器控制的方式来完成缓存控制），更加容易。

## 工具简介 ##
Mepressor分为两个部分：一部分为公网发布时所用，使用Java编写；另一部分为本地调试时所用，使用轻量和方便的PHP编写。（似乎很绕，简单地讲：Java版本用来公网部署，合并同时压缩代码；PHP版用来本地测试，并不压缩内容。）

在项目中需要合并多个js文件或者css文件时，前端开发者只需修改一个xml格式的配置文件，指定需要合并的文件，通过一定的规则访问，服务器则可自动合并多个js文件，并使用YUI Compressor压缩后返回相应内容，而本地环境下，则会自动导入xml中指定的多个js文件或者css文件，从而使得开发调试和发布一体化，真正做到无缝切换。

**注意：**
由于合并和压缩需要花费较大的资源和时间，因此在响应速度和并发上会表现较差，在实际使用过程中，需要配合CDN（内容分发网络，一般中大型网站都会采用）使用。当然，你也可以只是把这个工具当合并压缩工具使用。

## 使用示例 ##
### 目录结构及配置文件 ###
假设静态资源的域名为 static.mydomain.com，并且根目录下有这样一个目录结构：

![http://merpressor.googlecode.com/files/tree.gif](http://merpressor.googlecode.com/files/tree.gif)

其中local为静态资源根目录（网站根目录），两个build目录为merpressor要用到的配置文件目录。js目录下有jui.js和selector.js，我们要合并这两个js。

首先在js下的build目录中新建一个配置文件名，js的配置文件名必须以.js.xml结尾，在这里我们命名为：test.js.xml。

配置文件结构非常简单，只需要包含一个要合并的js文件列表即可，根节点需要指定文件编码（如不指定，默认使用UTF-8）。

```
<?xml version="1.0" encoding="utf-8"?>
<list encoding="utf-8">
    <file>jui.js</file>
    <file>selector.js</file>
</list>
```

其中js的路径是以build的上一级目录为准的。如果jui.js放在js目录下的test目录下面，那么这里配置文件则要相应改成：

```
<?xml version="1.0" encoding="utf-8"?>
<list encoding="utf-8">
    <file>test/jui.js</file>
    <file>selector.js</file>
</list>
```

### 访问路径 ###
访问路径为xml的访问路径，但是需要去掉后缀.xml，即可以通过如下URL访问合并后的js文件：

```
http://static.mydomain.com/jui/100804/js/build/test.js
```

在server上，改js是合并jui.js和selector.js并压缩后的内容，而在本地环境下，则会使用document.write分别引入jui.js和selector.js，以方便本地调试。

### 过期时间控制 ###
静态资源的过期时间在各个项目之间往往不一样，这里提供了一个接口，可以通过URL来控制该静态资源的缓存时间。你可以在.js之前加入 /e\_数字 来控制过期时间，比如：

```
http://static.mydomain.com/jui/100804/js/build/e_360/test.js
```

这个表示过期时间为360分钟，默认过期时间为一个小时。这样会在http的headers中添加expires和max-age两个字段。

**由于性能及复杂度的问题，非CSS和非JS等静态资源（图片等）的过期时间控制需要交由服务器处理。如果resin前面还有一个nginx代理，则可以交由nginx处理。**

### 版本控制 ###
另外由于部分CDN不会缓存URL中含有?的内容，因此这里也提供了一个接口来更新版本号。你可以在.js之前加入 /v\_数字 来更新版本号，比如：

```
http://static.mydomain.com/jui/100804/js/build/v_1210/test.js
```

后面的数字代表你的版本号，**这里不能包含其他字符，只能是纯数字**。

如果要同时使用过期时间和版本号，过期时间必须在版本号之前，否则会出错。如：

```
http://static.mydomain.com/jui/100804/js/build/e_360/v_1210/test.js
```

## 安装部署 ##
安装部署分为两个部分，一个是生产（发布）环境部署，一个是本地开发调试环境部署。

### 生产（发布）环境部署 ###
#### 软件需求 ####
  * JDK 1.5或以上
  * Java WEB服务器，Resin/Tomcat/JBoss等等（开发时使用的是resin，在源码包有相应的配置文件，可直接使用）

#### 安装步骤 ####
这里以Resin为例，至于在Linux或者Windows上怎么安装Resin，请各位Google之，或者找公司的运维同事解决，这里主要讲Merpressor在服务器上的部署。

  1. 下载merpressor，
  1. 解压后复制server下WebContent文件夹中的内容至静态资源的web目录下，如：/home/user/web
  1. 修改resin.conf，为该目录配置一个WEB应用程序，使得可以用你的静态资源域名访问

由于服务器不一样，可能配置会有变化，请参考请求的执行规则，具体正则表达式在resin-web.xml中：
  1. 对于URL中有/build/目录的js请求和css请求，会转发给名为merpressor的servlet执行
  1. 对于URL中有/build/目录的其他请求，会去除build和过期时间及版本目录，然后交给服务器返回
  1. 对于URL中没有/build/目录的js请求和css请求，会转发给名为merpressor的servlet执行
  1. 其他请求直接由服务器返回

**另外，请把server目录下的lib目录覆盖掉WebContent/WEB-INF/lib目录。**

### 本地开发调试环境部署 ###
#### 软件需求 ####
  * PHP 5.0或以上
  * PHP WEB服务器，推荐nginx + PHP CGI方式，本地使用时占用资源非常少（开发时使用的就是这种方式，在源码包中有相应的配置文件，可以直接使用）

#### 安装步骤 ####
这里以常用的windows环境为例，其他操作系统请Google并把相关配置按照下面的步骤画瓢就可以了。

##### PHP的安装 #####
  1. 下载PHP 5的zip包，http://windows.php.net/download/
  1. 解压至本地目录，这里以 D:\servers\php5 为例，这个目录在后面的配置中会用到
  1. 打开这个目录，复制一份php.ini-recommended，并重命名为php.ini

##### NGINX的安装 #####
  1. 下载Nginx 0.8的windows版本，http://www.nginx.org/en/download.html
  1. 解压至本地目录，这里以 D:\servers\nginx 为例，这个目录在后面的配置中会用到

##### Merpressor的安装 #####
  1. 下载merpressor，
  1. 解压至本地文件夹（其中的JUI目录为测试目录，可删除），复制local文件夹中的内容至静态资源的web目录下，如：D:\webroot
  1. 打开nginx目录下的conf目录
  1. 备份一个nginx.conf，然后打开nginx.conf文件
  1. 找到如下段：
```
   server {
      ...
   }
```
  1. 在这个server段之后插入如下内容（请修改下面的server\_name为你自己的静态资源域名）：
```
   ##########################
   # merpressor config
   server {
       listen       80;
       server_name  static.com;
       
       #charset koi8-r;
       
       #access_log  logs/host.access.log  main;
       
       location / {
           root   D:/webroot;
           index  index.html index.htm index.php;
           rewrite  "^(/[\w/]*?/)build(?:/e_(?:\d+))?(?:/v_(?:\d+))?(/(?:.*?)).((?:js)|(?:css))(?:\?.*)?$" /merpressor.php?home=$1&config=build$2&type=$3;
           rewrite  "^/([\w/]*?/)build(?:/e_(?:\d+))?(?:/v_(?:\d+))?(/.*?.(?:(?!js)|(?!css))(?:\?.*)?$)" /$1$2;
           rewrite  "^/([\w/]*?/)build/(.*?.(?:(?!js)|(?!css))(?:\?.*)?$)" /$1$2;
       }
    
       # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
       #
       location ~ \.php$ {
           root           D:/webroot;
           fastcgi_pass   127.0.0.1:9000;
           fastcgi_index  index.php;
           fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
           include        fastcgi_params;
       }
   }
```

##### 启动服务器 #####
  1. 下载 RunHiddenConsole.exe，http://redmine.lighttpd.net/attachments/660/RunHiddenConsole.zip
  1. 解压其中的 RunHiddenConsole.exe 至 D:\server\nginx
  1. 在 D:\server\nginx 下新建一个restart.bat文件
  1. 修改其内容为：
```
   @echo off
   taskkill /f /im nginx.exe
   taskkill /f /im php-cgi.exe
   
   REM "正在启动PHP FastCGI......"
   start D:\servers\nginx\RunHiddenConsole.exe D:\servers\php5\php-cgi.exe -b 127.0.0.1:9000
   
   REM "正在启动nginx......"
   start D:\servers\nginx\nginx.exe -c D:\servers\nginx\conf\nginx.conf
   
   exit
```

##### 测试是否成功 #####
  1. 双击 restart.bat 文件
  1. 打开浏览器
  1. 输入网址：http://static.com/ （请换成你自己的域名，本地环境还可能需要改hosts），看是否看到welcome页面。如看到，则表示服务器配置成功。